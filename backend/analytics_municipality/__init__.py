"""
Municipality Analytics Endpoint
Returns aggregated health statistics at municipality level
"""
import azure.functions as func
import asyncpg
import json
import logging
from datetime import datetime, timedelta
from typing import Dict, List, Any

from shared.db import get_db_pool
from shared.utils import decimal_to_float

logger = logging.getLogger(__name__)


async def get_municipality_stats(conn: asyncpg.Connection, province: str = None, municipality: str = None) -> List[Dict]:
    """Get municipality-level health statistics"""

    now_ms = int(datetime.utcnow().timestamp() * 1000)
    seven_days_ago = now_ms - (7 * 24 * 3600 * 1000)

    where_clauses = []
    params = []
    param_index = 1

    if province:
        where_clauses.append(f"b.province = ${param_index}")
        params.append(province)
        param_index += 1

    if municipality:
        where_clauses.append(f"b.municipality = ${param_index}")
        params.append(municipality)
        param_index += 1

    where_clause = f"WHERE {' AND '.join(where_clauses)}" if where_clauses else ""

    # Add time window param
    params.append(seven_days_ago)

    query = f"""
        WITH municipality_data AS (
            SELECT
                b.municipality,
                b.province,
                b.region,
                COUNT(DISTINCT b.id) as total_barangays,
                COUNT(DISTINCT p.id) as total_patients,
                COUNT(DISTINCT CASE WHEN p.high_risk_flag THEN p.id END) as high_risk_patients,
                COUNT(DISTINCT CASE WHEN p.is_pregnant THEN p.id END) as pregnant_patients,
                COUNT(DISTINCT s.id) as total_scans,
                COUNT(DISTINCT CASE WHEN s.scanned_at > ${param_index} THEN s.id END) as recent_scans,
                AVG(s.risk_score) as avg_risk_score,
                AVG(s.systolic_bp) as avg_systolic_bp,
                AVG(s.diastolic_bp) as avg_diastolic_bp,
                AVG(s.spo2) as avg_spo2,
                COUNT(DISTINCT r.id) as active_referrals,
                SUM(CASE WHEN bi.status = 'EARNED' THEN bi.amount ELSE 0 END) as total_incentives_paid
            FROM silver.barangays b
            LEFT JOIN silver.patients p ON p.barangay_id = b.id
            LEFT JOIN silver.scans s ON s.patient_id = p.id
            LEFT JOIN silver.referrals r ON r.patient_id = p.id AND r.status IN ('PENDING', 'CONFIRMED')
            LEFT JOIN silver.bhw_incentives bi ON bi.scan_id = s.id
            {where_clause}
            GROUP BY b.municipality, b.province, b.region
        )
        SELECT
            municipality,
            province,
            region,
            total_barangays,
            total_patients,
            high_risk_patients,
            ROUND(100.0 * high_risk_patients / NULLIF(total_patients, 0), 2) as high_risk_percentage,
            pregnant_patients,
            total_scans,
            recent_scans,
            ROUND(avg_risk_score::numeric, 2) as avg_risk_score,
            ROUND(avg_systolic_bp::numeric, 1) as avg_systolic_bp,
            ROUND(avg_diastolic_bp::numeric, 1) as avg_diastolic_bp,
            ROUND(avg_spo2::numeric, 1) as avg_spo2,
            active_referrals,
            ROUND(total_incentives_paid::numeric, 2) as total_incentives_paid,
            ROUND(100.0 * total_scans / NULLIF(total_patients, 0), 1) as coverage_percentage,
            ROUND(1.0 * recent_scans / 7, 1) as scans_per_day_recent
        FROM municipality_data
        ORDER BY total_patients DESC
    """

    results = await conn.fetch(query, *params)
    return [dict(r) for r in results]


async def get_barangay_list(conn: asyncpg.Connection, municipality: str, province: str) -> List[Dict]:
    """Get list of barangays in a municipality with basic stats"""

    query = """
        SELECT
            b.id,
            b.name,
            b.population,
            COUNT(DISTINCT p.id) as registered_patients,
            COUNT(DISTINCT s.id) as total_scans,
            COUNT(DISTINCT CASE WHEN p.high_risk_flag THEN p.id END) as high_risk_count,
            MAX(s.scanned_at) as last_scan_at
        FROM silver.barangays b
        LEFT JOIN silver.patients p ON p.barangay_id = b.id
        LEFT JOIN silver.scans s ON s.patient_id = p.id
        WHERE b.municipality = $1 AND b.province = $2
        GROUP BY b.id, b.name, b.population
        ORDER BY b.name
    """

    results = await conn.fetch(query, municipality, province)
    return [dict(r) for r in results]


async def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    Municipality Analytics Endpoint
    GET /analytics/municipality?province=<name>&municipality=<name>
    """
    logger.info("Municipality analytics request received")

    try:
        # Get query parameters
        province = req.params.get('province')
        municipality = req.params.get('municipality')

        logger.info(f"Getting municipality analytics: province={province}, municipality={municipality}")

        # Fetch analytics data
        pool = await get_db_pool()
        async with pool.acquire() as conn:
            # Get municipality stats
            stats = await get_municipality_stats(conn, province, municipality)

            # Get barangay list if specific municipality requested
            barangays = None
            if municipality and province and stats:
                barangays = await get_barangay_list(conn, municipality, province)

        # Build response
        response = {
            "success": True,
            "timestamp": int(datetime.utcnow().timestamp() * 1000),
            "province": province,
            "municipality": municipality,
            "stats": stats,
            "barangays": barangays
        }

        # Convert Decimal objects to float for JSON serialization
        response = decimal_to_float(response)

        logger.info(f"Municipality analytics complete: {len(stats)} municipalities")

        return func.HttpResponse(
            json.dumps(response),
            mimetype="application/json",
            status_code=200
        )

    except Exception as e:
        logger.error(f"Municipality analytics failed: {e}")
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Internal server error"}),
            mimetype="application/json",
            status_code=500
        )
