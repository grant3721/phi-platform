"""
Sync Download Endpoint
Returns updated data from server to Android devices
"""
import azure.functions as func
import asyncpg
import json
import logging
from datetime import datetime
from typing import Dict, List, Any

from shared.db import get_db_pool
from shared.auth import require_auth

logger = logging.getLogger(__name__)


async def get_config_updates(conn: asyncpg.Connection, last_sync: int) -> List[Dict]:
    """Get updated configuration thresholds since last sync"""
    thresholds = await conn.fetch("""
        SELECT id, threshold_type, parameter_name, low_threshold, high_threshold,
               unit, description, active, updated_at
        FROM silver.config_thresholds
        WHERE updated_at > $1 AND active = TRUE
        ORDER BY updated_at DESC
    """, last_sync)

    return [dict(t) for t in thresholds]


async def get_referral_updates(conn: asyncpg.Connection, bhw_id: str, last_sync: int) -> List[Dict]:
    """
    Get referral status updates for this BHW's referrals
    Returns referrals that have been updated by BHS/RHU
    """
    referrals = await conn.fetch("""
        SELECT id, patient_id, scan_id, tier, status, priority,
               reason, referred_by, referred_at, due_by,
               resolved_at, resolution_notes, updated_at
        FROM silver.referrals
        WHERE referred_by = $1
          AND updated_at > $2
        ORDER BY updated_at DESC
        LIMIT 100
    """, bhw_id, last_sync)

    return [dict(r) for r in referrals]


async def get_patient_updates(conn: asyncpg.Connection, bhw_id: str, last_sync: int) -> List[Dict]:
    """
    Get patient flag updates (high-risk status changes)
    Returns patients whose risk flags have been updated
    """
    # Get patients that this BHW has scanned
    patients = await conn.fetch("""
        SELECT DISTINCT p.id, p.phone_number, p.full_name,
               p.high_risk_flag, p.high_risk_reasons,
               p.maternal_high_risk, p.updated_at
        FROM silver.patients p
        INNER JOIN silver.scans s ON s.patient_id = p.id
        WHERE s.bhw_id = $1
          AND p.updated_at > $2
          AND (p.high_risk_flag = TRUE OR p.maternal_high_risk = TRUE)
        ORDER BY p.updated_at DESC
        LIMIT 50
    """, bhw_id, last_sync)

    return [dict(p) for p in patients]


async def get_dedup_results(conn: asyncpg.Connection, device_id: str, last_sync: int) -> List[Dict]:
    """
    Get dedup results - patients that were merged/rejected during upload
    Helps device clean up duplicate records
    """
    # Query bronze.patients that were deduplicated
    dedup_info = await conn.fetch("""
        SELECT DISTINCT
            bp.phone_number,
            sp.id as silver_id,
            bp.ingested_at
        FROM bronze.patients bp
        INNER JOIN silver.patients sp ON sp.phone_number = bp.phone_number
        WHERE bp.device_id = $1
          AND bp.ingested_at > $2
          AND bp.id != sp.bronze_source_id
        ORDER BY bp.ingested_at DESC
        LIMIT 20
    """, device_id, last_sync)

    return [dict(d) for d in dedup_info]


async def get_incentive_updates(conn: asyncpg.Connection, bhw_id: str, last_sync: int) -> Dict:
    """
    Get incentive earnings summary for this BHW
    Returns total earned and recent transactions
    """
    # Get total earnings
    total_earned = await conn.fetchval("""
        SELECT COALESCE(SUM(amount), 0)
        FROM silver.bhw_incentives
        WHERE bhw_id = $1 AND status = 'EARNED'
    """, bhw_id)

    # Get earnings since last sync
    recent_earnings = await conn.fetchval("""
        SELECT COALESCE(SUM(amount), 0)
        FROM silver.bhw_incentives
        WHERE bhw_id = $1
          AND status = 'EARNED'
          AND earned_at > $2
    """, bhw_id, last_sync)

    # Get today's earnings count
    today_start = int(datetime.utcnow().replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000)
    today_count = await conn.fetchval("""
        SELECT COUNT(*)
        FROM silver.bhw_incentives
        WHERE bhw_id = $1
          AND status = 'EARNED'
          AND earned_at >= $2
    """, bhw_id, today_start)

    today_amount = await conn.fetchval("""
        SELECT COALESCE(SUM(amount), 0)
        FROM silver.bhw_incentives
        WHERE bhw_id = $1
          AND status = 'EARNED'
          AND earned_at >= $2
    """, bhw_id, today_start)

    return {
        "total_earned": float(total_earned),
        "recent_earned": float(recent_earnings),
        "today": {
            "count": today_count,
            "amount": float(today_amount),
            "cap_reached": today_count >= 50
        }
    }


async def get_outbreak_alerts(conn: asyncpg.Connection, last_sync: int) -> List[Dict]:
    """
    Get active outbreak signals that might affect BHW operations
    """
    alerts = await conn.fetch("""
        SELECT id, barangay_id, signal_type, severity, status,
               detected_at, description, recommended_actions
        FROM gold.outbreak_signals
        WHERE status = 'ACTIVE'
          AND detected_at > $1
        ORDER BY severity DESC, detected_at DESC
        LIMIT 10
    """, last_sync)

    return [dict(a) for a in alerts]


async def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    Sync Download Endpoint
    GET /sync/download?last_sync=<timestamp>
    """
    logger.info("Sync download request received")

    # Require authentication
    is_authorized, error_msg, user = require_auth(req)
    if not is_authorized:
        return func.HttpResponse(
            json.dumps({"success": False, "error": error_msg}),
            mimetype="application/json",
            status_code=401
        )

    bhw_id = user.get('user_id')
    phone = user.get('phone')

    try:
        # Get query parameters
        last_sync = int(req.params.get('last_sync', '0'))
        device_id = req.params.get('device_id')

        if not device_id:
            return func.HttpResponse(
                json.dumps({"success": False, "error": "device_id required"}),
                mimetype="application/json",
                status_code=400
            )

        logger.info(f"Sync download for BHW {bhw_id}, last_sync: {last_sync}")

        # Fetch updates from database
        pool = await get_db_pool()
        async with pool.acquire() as conn:
            # Get all update types
            config_updates = await get_config_updates(conn, last_sync)
            referral_updates = await get_referral_updates(conn, bhw_id, last_sync)
            patient_updates = await get_patient_updates(conn, bhw_id, last_sync)
            dedup_results = await get_dedup_results(conn, device_id, last_sync)
            incentive_summary = await get_incentive_updates(conn, bhw_id, last_sync)
            outbreak_alerts = await get_outbreak_alerts(conn, last_sync)

        # Build response
        response = {
            "success": True,
            "sync_timestamp": int(datetime.utcnow().timestamp() * 1000),
            "updates": {
                "config_thresholds": config_updates,
                "referrals": referral_updates,
                "patients": patient_updates,
                "dedup_info": dedup_results,
                "incentives": incentive_summary,
                "outbreak_alerts": outbreak_alerts
            },
            "summary": {
                "config_updates": len(config_updates),
                "referral_updates": len(referral_updates),
                "patient_updates": len(patient_updates),
                "dedup_count": len(dedup_results),
                "active_alerts": len(outbreak_alerts)
            }
        }

        logger.info(f"Sync download complete: {response['summary']}")

        return func.HttpResponse(
            json.dumps(response),
            mimetype="application/json",
            status_code=200
        )

    except ValueError as e:
        logger.error(f"Invalid request: {e}")
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Invalid parameters"}),
            mimetype="application/json",
            status_code=400
        )

    except Exception as e:
        logger.error(f"Sync download failed: {e}")
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Internal server error"}),
            mimetype="application/json",
            status_code=500
        )
