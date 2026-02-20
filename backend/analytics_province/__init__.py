"""
Province Analytics Endpoint
Returns aggregated health statistics at province level
"""
import azure.functions as func
import asyncpg
import json
import logging
from datetime import datetime, timedelta
from typing import Dict, List, Any

from shared.db import get_db_pool
from shared.auth import require_auth
from shared.utils import decimal_to_float

logger = logging.getLogger(__name__)


async def get_province_stats(conn: asyncpg.Connection, province: str = None) -> List[Dict]:
    """Get province-level health statistics"""

    # Time windows
    now_ms = int(datetime.utcnow().timestamp() * 1000)
    seven_days_ago = now_ms - (7 * 24 * 3600 * 1000)
    thirty_days_ago = now_ms - (30 * 24 * 3600 * 1000)

    where_clause = ""
    params = []
    if province:
        where_clause = "WHERE b.province = $1"
        params = [province]

    # Query aggregated stats by province
    query = f"""
        WITH province_data AS (
            SELECT
                b.province,
                b.region,
                COUNT(DISTINCT p.id) as total_patients,
                COUNT(DISTINCT CASE WHEN p.high_risk_flag THEN p.id END) as high_risk_patients,
                COUNT(DISTINCT s.id) as total_scans,
                COUNT(DISTINCT CASE WHEN s.scanned_at > ${'2' if province else '1'} THEN s.id END) as scans_last_7_days,
                COUNT(DISTINCT CASE WHEN s.scanned_at > ${'3' if province else '2'} THEN s.id END) as scans_last_30_days,
                AVG(s.systolic_bp) as avg_systolic_bp,
                AVG(s.diastolic_bp) as avg_diastolic_bp,
                AVG(s.heart_rate_bpm) as avg_heart_rate,
                AVG(s.spo2) as avg_spo2,
                AVG(s.risk_score) as avg_risk_score,
                COUNT(DISTINCT r.id) as active_referrals,
                COUNT(DISTINCT bhw.bhw_id) as active_bhws
            FROM silver.barangays b
            LEFT JOIN silver.patients p ON p.barangay_id = b.id
            LEFT JOIN silver.scans s ON s.patient_id = p.id
            LEFT JOIN silver.referrals r ON r.patient_id = p.id AND r.status IN ('PENDING', 'CONFIRMED')
            LEFT JOIN silver.bhw_incentives bhw ON bhw.bhw_id = s.bhw_id AND bhw.earned_at > ${'3' if province else '2'}
            {where_clause}
            GROUP BY b.province, b.region
        )
        SELECT
            province,
            region,
            total_patients,
            high_risk_patients,
            ROUND(100.0 * high_risk_patients / NULLIF(total_patients, 0), 2) as high_risk_percentage,
            total_scans,
            scans_last_7_days,
            scans_last_30_days,
            ROUND(avg_systolic_bp::numeric, 1) as avg_systolic_bp,
            ROUND(avg_diastolic_bp::numeric, 1) as avg_diastolic_bp,
            ROUND(avg_heart_rate::numeric, 1) as avg_heart_rate,
            ROUND(avg_spo2::numeric, 1) as avg_spo2,
            ROUND(avg_risk_score::numeric, 2) as avg_risk_score,
            active_referrals,
            active_bhws,
            ROUND(1.0 * scans_last_7_days / 7, 1) as scans_per_day_7d,
            ROUND(100.0 * total_scans / NULLIF(total_patients, 0), 1) as scans_per_patient
        FROM province_data
        ORDER BY total_patients DESC
    """

    if province:
        results = await conn.fetch(query, province, seven_days_ago, thirty_days_ago)
    else:
        results = await conn.fetch(query, seven_days_ago, thirty_days_ago)

    return [dict(r) for r in results]


async def get_top_barangays(conn: asyncpg.Connection, province: str, metric: str = 'high_risk') -> List[Dict]:
    """Get top barangays by specified metric within a province"""

    order_by = {
        'high_risk': 'high_risk_percentage DESC',
        'scans': 'total_scans DESC',
        'patients': 'total_patients DESC',
        'risk_score': 'avg_risk_score DESC'
    }.get(metric, 'high_risk_percentage DESC')

    query = f"""
        SELECT
            b.name as barangay_name,
            b.municipality,
            COUNT(DISTINCT p.id) as total_patients,
            COUNT(DISTINCT CASE WHEN p.high_risk_flag THEN p.id END) as high_risk_patients,
            ROUND(100.0 * COUNT(DISTINCT CASE WHEN p.high_risk_flag THEN p.id END) /
                  NULLIF(COUNT(DISTINCT p.id), 0), 2) as high_risk_percentage,
            COUNT(DISTINCT s.id) as total_scans,
            ROUND(AVG(s.risk_score)::numeric, 2) as avg_risk_score,
            COUNT(DISTINCT r.id) as active_referrals
        FROM silver.barangays b
        LEFT JOIN silver.patients p ON p.barangay_id = b.id
        LEFT JOIN silver.scans s ON s.patient_id = p.id
        LEFT JOIN silver.referrals r ON r.patient_id = p.id AND r.status IN ('PENDING', 'CONFIRMED')
        WHERE b.province = $1
        GROUP BY b.id, b.name, b.municipality
        HAVING COUNT(DISTINCT p.id) > 0
        ORDER BY {order_by}
        LIMIT 10
    """

    results = await conn.fetch(query, province)
    return [dict(r) for r in results]


async def get_trends(conn: asyncpg.Connection, province: str, days: int = 30) -> List[Dict]:
    """Get daily trends for a province"""

    now_ms = int(datetime.utcnow().timestamp() * 1000)
    start_ms = now_ms - (days * 24 * 3600 * 1000)

    query = """
        WITH daily_data AS (
            SELECT
                DATE(TO_TIMESTAMP(s.scanned_at / 1000)) as scan_date,
                COUNT(DISTINCT s.id) as scans,
                COUNT(DISTINCT p.id) as unique_patients,
                AVG(s.risk_score) as avg_risk_score,
                COUNT(DISTINCT CASE WHEN s.risk_score > 7 THEN s.id END) as high_risk_scans
            FROM silver.scans s
            INNER JOIN silver.patients p ON p.id = s.patient_id
            INNER JOIN silver.barangays b ON b.id = p.barangay_id
            WHERE b.province = $1
              AND s.scanned_at > $2
            GROUP BY scan_date
        )
        SELECT
            scan_date,
            scans,
            unique_patients,
            ROUND(avg_risk_score::numeric, 2) as avg_risk_score,
            high_risk_scans,
            ROUND(100.0 * high_risk_scans / NULLIF(scans, 0), 1) as high_risk_percentage
        FROM daily_data
        ORDER BY scan_date DESC
    """

    results = await conn.fetch(query, province, start_ms)
    return [dict(r) for r in results]


async def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    Province Analytics Endpoint
    GET /analytics/province?province=<name>&days=<int>
    """
    logger.info("Province analytics request received")

    # Optional authentication (allow public access to aggregated stats)
    # Uncomment to require auth:
    # is_authorized, error_msg, user = require_auth(req)
    # if not is_authorized:
    #     return func.HttpResponse(
    #         json.dumps({"success": False, "error": error_msg}),
    #         mimetype="application/json",
    #         status_code=401
    #     )

    try:
        # Get query parameters
        province = req.params.get('province')
        days = int(req.params.get('days', '30'))
        metric = req.params.get('metric', 'high_risk')

        if days < 1 or days > 365:
            return func.HttpResponse(
                json.dumps({"success": False, "error": "days must be between 1 and 365"}),
                mimetype="application/json",
                status_code=400
            )

        logger.info(f"Getting analytics for province: {province or 'ALL'}, days: {days}")

        # Fetch analytics data
        pool = await get_db_pool()
        async with pool.acquire() as conn:
            # Get province-level stats
            province_stats = await get_province_stats(conn, province)

            # Get additional details if specific province requested
            top_barangays = None
            trends = None
            if province and province_stats:
                top_barangays = await get_top_barangays(conn, province, metric)
                trends = await get_trends(conn, province, days)

        # Build response
        response = {
            "success": True,
            "timestamp": int(datetime.utcnow().timestamp() * 1000),
            "province": province or "ALL",
            "stats": province_stats,
            "top_barangays": top_barangays,
            "trends": trends
        }

        # Convert Decimal objects to float for JSON serialization
        response = decimal_to_float(response)

        logger.info(f"Province analytics complete: {len(province_stats)} provinces")

        return func.HttpResponse(
            json.dumps(response),
            mimetype="application/json",
            status_code=200
        )

    except ValueError as e:
        logger.error(f"Invalid parameters: {e}")
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Invalid parameters"}),
            mimetype="application/json",
            status_code=400
        )

    except Exception as e:
        logger.error(f"Province analytics failed: {e}")
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Internal server error"}),
            mimetype="application/json",
            status_code=500
        )
