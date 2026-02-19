"""
Config Thresholds Update Endpoint
Allows admin users to update clinical thresholds
"""
import azure.functions as func
import logging
import json
from datetime import datetime

from shared import db, require_auth

logger = logging.getLogger(__name__)


async def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    PUT /api/config/thresholds/{threshold_id}

    Updates a specific threshold (admin only)

    Request body:
    {
        "low_threshold": 90.0,
        "high_threshold": 140.0,
        "unit": "mmHg",
        "description": "Updated description",
        "active": true
    }

    Response:
    {
        "success": true,
        "threshold": {...updated threshold...}
    }
    """
    try:
        # Require admin authentication
        is_authorized, error_msg, user = require_auth(req, required_role="admin")
        if not is_authorized:
            return func.HttpResponse(
                json.dumps({"success": False, "error": error_msg}),
                status_code=401 if "Unauthorized" in error_msg else 403,
                mimetype="application/json"
            )

        # Get threshold ID from route
        threshold_id = req.route_params.get('threshold_id')
        if not threshold_id:
            return func.HttpResponse(
                json.dumps({"success": False, "error": "Threshold ID required"}),
                status_code=400,
                mimetype="application/json"
            )

        # Parse request body
        try:
            body = req.get_json()
        except ValueError:
            return func.HttpResponse(
                json.dumps({"success": False, "error": "Invalid JSON"}),
                status_code=400,
                mimetype="application/json"
            )

        # Connect to database
        await db.connect()

        # Check if threshold exists
        existing = await db.fetchrow("""
            SELECT * FROM silver.config_thresholds WHERE id = $1
        """, threshold_id)

        if not existing:
            return func.HttpResponse(
                json.dumps({"success": False, "error": "Threshold not found"}),
                status_code=404,
                mimetype="application/json"
            )

        # Update threshold
        updated_at = int(datetime.utcnow().timestamp() * 1000)

        update_fields = []
        update_values = []
        param_index = 1

        if "low_threshold" in body:
            update_fields.append(f"low_threshold = ${param_index}")
            update_values.append(body["low_threshold"])
            param_index += 1

        if "high_threshold" in body:
            update_fields.append(f"high_threshold = ${param_index}")
            update_values.append(body["high_threshold"])
            param_index += 1

        if "unit" in body:
            update_fields.append(f"unit = ${param_index}")
            update_values.append(body["unit"])
            param_index += 1

        if "description" in body:
            update_fields.append(f"description = ${param_index}")
            update_values.append(body["description"])
            param_index += 1

        if "active" in body:
            update_fields.append(f"active = ${param_index}")
            update_values.append(body["active"])
            param_index += 1

        # Always update updated_at
        update_fields.append(f"updated_at = ${param_index}")
        update_values.append(updated_at)
        param_index += 1

        # Add threshold_id as last parameter
        update_values.append(threshold_id)

        # Build and execute update query
        update_query = f"""
            UPDATE silver.config_thresholds
            SET {', '.join(update_fields)}
            WHERE id = ${param_index}
            RETURNING *
        """

        updated_threshold = await db.fetchrow(update_query, *update_values)

        logger.info(f"Threshold {threshold_id} updated by {user['user_id']}")

        return func.HttpResponse(
            json.dumps({
                "success": True,
                "threshold": dict(updated_threshold),
                "updated_by": user['user_id']
            }),
            status_code=200,
            mimetype="application/json"
        )

    except Exception as e:
        logger.error(f"Error updating threshold: {e}", exc_info=True)
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Internal server error"}),
            status_code=500,
            mimetype="application/json"
        )
