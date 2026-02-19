"""
Config Thresholds Get Endpoint
Returns all active clinical thresholds for risk assessment
"""
import azure.functions as func
import logging
import json

from shared import db, records_to_list, require_auth

logger = logging.getLogger(__name__)


async def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    GET /api/config/thresholds

    Returns all active thresholds for client-side risk assessment

    Response:
    {
        "thresholds": [
            {
                "id": "THR001",
                "threshold_type": "VITAL_SIGN",
                "parameter_name": "systolic_bp",
                "low_threshold": 90.0,
                "high_threshold": 140.0,
                "unit": "mmHg",
                "description": "Systolic blood pressure",
                "active": true,
                "updated_at": 1234567890000
            },
            ...
        ],
        "last_updated": 1234567890000
    }
    """
    try:
        # Optional: Require authentication
        # For now, thresholds are public (needed for offline risk assessment)
        # In production, you might want to require authentication
        # is_authorized, error_msg, user = require_auth(req)
        # if not is_authorized:
        #     return func.HttpResponse(
        #         json.dumps({"success": False, "error": error_msg}),
        #         status_code=401,
        #         mimetype="application/json"
        #     )

        # Connect to database
        await db.connect()

        # Fetch all active thresholds
        thresholds = await db.fetch("""
            SELECT
                id,
                threshold_type,
                parameter_name,
                low_threshold,
                high_threshold,
                unit,
                description,
                active,
                updated_at
            FROM silver.config_thresholds
            WHERE active = TRUE
            ORDER BY threshold_type, parameter_name
        """)

        # Get the most recent update timestamp
        latest_update = await db.fetchval("""
            SELECT MAX(updated_at) FROM silver.config_thresholds WHERE active = TRUE
        """)

        logger.info(f"Returning {len(thresholds)} active thresholds")

        return func.HttpResponse(
            json.dumps({
                "thresholds": records_to_list(thresholds),
                "last_updated": latest_update or 0,
                "count": len(thresholds)
            }),
            status_code=200,
            mimetype="application/json"
        )

    except Exception as e:
        logger.error(f"Error fetching thresholds: {e}", exc_info=True)
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Internal server error"}),
            status_code=500,
            mimetype="application/json"
        )
