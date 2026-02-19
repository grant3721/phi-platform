"""
Outbreak Signals Detection Endpoint
Detects potential outbreak patterns from scan data
"""
import azure.functions as func
import asyncpg
import json
import logging
from datetime import datetime, timedelta
from typing import Dict, List, Any

from shared.db import get_db_pool

logger = logging.getLogger(__name__)


async def detect_respiratory_signals(conn: asyncpg.Connection, days: int = 7) -> List[Dict]:
    """Detect respiratory illness outbreak signals"""

    now_ms = int(datetime.utcnow().timestamp() * 1000)
    recent_window = now_ms - (days * 24 * 3600 * 1000)
    baseline_window = now_ms - (30 * 24 * 3600 * 1000)

    query = """
        WITH recent_stats AS (
            SELECT
                b.id as barangay_id,
                b.name as barangay_name,
                b.municipality,
                b.province,
                COUNT(DISTINCT s.id) as recent_scans,
                AVG(s.spo2) as avg_spo2,
                AVG(s.respiratory_rate) as avg_resp_rate,
                COUNT(DISTINCT CASE WHEN s.spo2 < 95 THEN s.id END) as low_spo2_count,
                COUNT(DISTINCT CASE WHEN s.respiratory_rate > 20 THEN s.id END) as high_resp_count
            FROM silver.barangays b
            INNER JOIN silver.patients p ON p.barangay_id = b.id
            INNER JOIN silver.scans s ON s.patient_id = p.id
            WHERE s.scanned_at > $1
            GROUP BY b.id, b.name, b.municipality, b.province
            HAVING COUNT(DISTINCT s.id) >= 5
        ),
        baseline_stats AS (
            SELECT
                b.id as barangay_id,
                AVG(s.spo2) as baseline_spo2,
                AVG(s.respiratory_rate) as baseline_resp_rate
            FROM silver.barangays b
            INNER JOIN silver.patients p ON p.barangay_id = b.id
            INNER JOIN silver.scans s ON s.patient_id = p.id
            WHERE s.scanned_at BETWEEN $2 AND $1
            GROUP BY b.id
        )
        SELECT
            r.barangay_id,
            r.barangay_name,
            r.municipality,
            r.province,
            r.recent_scans,
            ROUND(r.avg_spo2::numeric, 2) as recent_avg_spo2,
            ROUND(b.baseline_spo2::numeric, 2) as baseline_spo2,
            ROUND((r.avg_spo2 - b.baseline_spo2)::numeric, 2) as spo2_deviation,
            ROUND(r.avg_resp_rate::numeric, 2) as recent_avg_resp_rate,
            ROUND(b.baseline_resp_rate::numeric, 2) as baseline_resp_rate,
            ROUND((r.avg_resp_rate - b.baseline_resp_rate)::numeric, 2) as resp_rate_deviation,
            ROUND(100.0 * r.low_spo2_count / r.recent_scans, 1) as low_spo2_percentage,
            ROUND(100.0 * r.high_resp_count / r.recent_scans, 1) as high_resp_percentage
        FROM recent_stats r
        LEFT JOIN baseline_stats b ON b.barangay_id = r.barangay_id
        WHERE (r.avg_spo2 - COALESCE(b.baseline_spo2, r.avg_spo2)) < -2
           OR (r.avg_resp_rate - COALESCE(b.baseline_resp_rate, r.avg_resp_rate)) > 3
           OR (100.0 * r.low_spo2_count / r.recent_scans) > 20
        ORDER BY (r.avg_spo2 - COALESCE(b.baseline_spo2, r.avg_spo2)) ASC
    """

    results = await conn.fetch(query, recent_window, baseline_window)
    return [dict(r) for r in results]


async def detect_hypertension_signals(conn: asyncpg.Connection, days: int = 7) -> List[Dict]:
    """Detect hypertension outbreak signals"""

    now_ms = int(datetime.utcnow().timestamp() * 1000)
    recent_window = now_ms - (days * 24 * 3600 * 1000)

    query = """
        SELECT
            b.id as barangay_id,
            b.name as barangay_name,
            b.municipality,
            b.province,
            COUNT(DISTINCT s.id) as recent_scans,
            ROUND(AVG(s.systolic_bp)::numeric, 1) as avg_systolic_bp,
            ROUND(AVG(s.diastolic_bp)::numeric, 1) as avg_diastolic_bp,
            COUNT(DISTINCT CASE WHEN s.systolic_bp > 140 THEN s.id END) as elevated_systolic_count,
            COUNT(DISTINCT CASE WHEN s.diastolic_bp > 90 THEN s.id END) as elevated_diastolic_count,
            ROUND(100.0 * COUNT(DISTINCT CASE WHEN s.systolic_bp > 140 OR s.diastolic_bp > 90 THEN s.id END) /
                  NULLIF(COUNT(DISTINCT s.id), 0), 1) as elevated_bp_percentage
        FROM silver.barangays b
        INNER JOIN silver.patients p ON p.barangay_id = b.id
        INNER JOIN silver.scans s ON s.patient_id = p.id
        WHERE s.scanned_at > $1
        GROUP BY b.id, b.name, b.municipality, b.province
        HAVING COUNT(DISTINCT s.id) >= 5
           AND (AVG(s.systolic_bp) > 135
                OR AVG(s.diastolic_bp) > 85
                OR 100.0 * COUNT(DISTINCT CASE WHEN s.systolic_bp > 140 OR s.diastolic_bp > 90 THEN s.id END) /
                   NULLIF(COUNT(DISTINCT s.id), 0) > 40)
        ORDER BY avg_systolic_bp DESC
    """

    results = await conn.fetch(query, recent_window)
    return [dict(r) for r in results]


async def detect_diabetes_signals(conn: asyncpg.Connection, days: int = 7) -> List[Dict]:
    """Detect diabetes/metabolic outbreak signals"""

    now_ms = int(datetime.utcnow().timestamp() * 1000)
    recent_window = now_ms - (days * 24 * 3600 * 1000)

    query = """
        SELECT
            b.id as barangay_id,
            b.name as barangay_name,
            b.municipality,
            b.province,
            COUNT(DISTINCT s.id) as recent_scans,
            ROUND(AVG(s.blood_glucose)::numeric, 1) as avg_blood_glucose,
            ROUND(AVG(s.hba1c)::numeric, 2) as avg_hba1c,
            COUNT(DISTINCT CASE WHEN s.blood_glucose > 126 THEN s.id END) as elevated_glucose_count,
            COUNT(DISTINCT CASE WHEN s.hba1c > 6.5 THEN s.id END) as elevated_hba1c_count,
            ROUND(100.0 * COUNT(DISTINCT CASE WHEN s.blood_glucose > 126 OR s.hba1c > 6.5 THEN s.id END) /
                  NULLIF(COUNT(DISTINCT s.id), 0), 1) as elevated_percentage
        FROM silver.barangays b
        INNER JOIN silver.patients p ON p.barangay_id = b.id
        INNER JOIN silver.scans s ON s.patient_id = p.id
        WHERE s.scanned_at > $1
          AND (s.blood_glucose IS NOT NULL OR s.hba1c IS NOT NULL)
        GROUP BY b.id, b.name, b.municipality, b.province
        HAVING COUNT(DISTINCT s.id) >= 5
           AND (AVG(s.blood_glucose) > 110
                OR AVG(s.hba1c) > 6.0
                OR 100.0 * COUNT(DISTINCT CASE WHEN s.blood_glucose > 126 OR s.hba1c > 6.5 THEN s.id END) /
                   NULLIF(COUNT(DISTINCT s.id), 0) > 30)
        ORDER BY avg_blood_glucose DESC
    """

    results = await conn.fetch(query, recent_window)
    return [dict(r) for r in results]


async def store_signal(conn: asyncpg.Connection, signal_data: Dict) -> str:
    """Store detected signal in gold.outbreak_signals"""

    signal_id = f"SIG_{int(datetime.utcnow().timestamp() * 1000)}"

    await conn.execute("""
        INSERT INTO gold.outbreak_signals (
            id, barangay_id, signal_type, severity, status,
            detected_at, description, signal_data
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
    """, signal_id, signal_data['barangay_id'], signal_data['signal_type'],
        signal_data['severity'], 'ACTIVE', int(datetime.utcnow().timestamp() * 1000),
        signal_data['description'], json.dumps(signal_data['data']))

    return signal_id


async def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    Outbreak Signals Detection Endpoint
    GET /outbreak/signals?days=<int>
    """
    logger.info("Outbreak signals detection request received")

    try:
        # Get query parameters
        days = int(req.params.get('days', '7'))

        if days < 1 or days > 30:
            return func.HttpResponse(
                json.dumps({"success": False, "error": "days must be between 1 and 30"}),
                mimetype="application/json",
                status_code=400
            )

        logger.info(f"Detecting outbreak signals for last {days} days")

        # Detect signals
        pool = await get_db_pool()
        async with pool.acquire() as conn:
            respiratory_signals = await detect_respiratory_signals(conn, days)
            hypertension_signals = await detect_hypertension_signals(conn, days)
            diabetes_signals = await detect_diabetes_signals(conn, days)

            # Store new signals (only if significant)
            stored_signals = []
            for signal in respiratory_signals[:5]:  # Top 5 most concerning
                if signal['spo2_deviation'] < -3:
                    signal_data = {
                        'barangay_id': signal['barangay_id'],
                        'signal_type': 'RESPIRATORY',
                        'severity': 'HIGH' if signal['spo2_deviation'] < -5 else 'MEDIUM',
                        'description': f"Respiratory distress signals in {signal['barangay_name']}: SpO2 deviation {signal['spo2_deviation']}%",
                        'data': signal
                    }
                    signal_id = await store_signal(conn, signal_data)
                    stored_signals.append(signal_id)

        # Build response
        response = {
            "success": True,
            "timestamp": int(datetime.utcnow().timestamp() * 1000),
            "days": days,
            "signals": {
                "respiratory": respiratory_signals,
                "hypertension": hypertension_signals,
                "diabetes": diabetes_signals
            },
            "summary": {
                "total_signals": len(respiratory_signals) + len(hypertension_signals) + len(diabetes_signals),
                "respiratory_count": len(respiratory_signals),
                "hypertension_count": len(hypertension_signals),
                "diabetes_count": len(diabetes_signals),
                "stored_signals": stored_signals
            }
        }

        logger.info(f"Outbreak detection complete: {response['summary']['total_signals']} signals detected")

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
        logger.error(f"Outbreak detection failed: {e}")
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Internal server error"}),
            mimetype="application/json",
            status_code=500
        )
