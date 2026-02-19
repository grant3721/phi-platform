"""
Auth OTP Verify Endpoint
Verifies OTP code and issues JWT token
"""
import azure.functions as func
import logging
import json
from datetime import datetime
from pydantic import ValidationError

from shared import db, config, generate_jwt, OTPVerifyModel, AuthResponseModel

logger = logging.getLogger(__name__)


async def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    POST /api/auth/otp/verify

    Request body:
    {
        "phone_number": "+639171234567",
        "otp_code": "123456"
    }

    Response:
    {
        "token": "eyJ...",
        "user_id": "+639171234567",
        "role": "bhw",
        "expires_in": 86400
    }
    """
    try:
        # Parse request body
        try:
            body = req.get_json()
            request_data = OTPVerifyModel(**body)
        except (ValueError, ValidationError) as e:
            return func.HttpResponse(
                json.dumps({"success": False, "error": f"Invalid request: {str(e)}"}),
                status_code=400,
                mimetype="application/json"
            )

        phone_number = request_data.phone_number
        otp_code = request_data.otp_code

        logger.info(f"OTP verification attempt for: {phone_number}")

        # Connect to database
        await db.connect()

        # Retrieve stored OTP
        stored_otp = await db.fetchrow("""
            SELECT otp_code, expires_at
            FROM otp_codes
            WHERE phone_number = $1
        """, phone_number)

        if not stored_otp:
            return func.HttpResponse(
                json.dumps({"success": False, "error": "Invalid or expired OTP"}),
                status_code=401,
                mimetype="application/json"
            )

        # Check if OTP is expired
        now = int(datetime.utcnow().timestamp() * 1000)
        if stored_otp['expires_at'] < now:
            # Delete expired OTP
            await db.execute("DELETE FROM otp_codes WHERE phone_number = $1", phone_number)
            return func.HttpResponse(
                json.dumps({"success": False, "error": "OTP has expired"}),
                status_code=401,
                mimetype="application/json"
            )

        # Verify OTP code
        if stored_otp['otp_code'] != otp_code:
            return func.HttpResponse(
                json.dumps({"success": False, "error": "Invalid OTP code"}),
                status_code=401,
                mimetype="application/json"
            )

        # OTP is valid - generate JWT token
        # Determine user role (default to 'bhw' for now)
        user_role = await get_user_role(phone_number)

        token = generate_jwt(
            user_id=phone_number,
            role=user_role,
            phone=phone_number
        )

        # Delete used OTP
        await db.execute("DELETE FROM otp_codes WHERE phone_number = $1", phone_number)

        # Update or create user record
        await upsert_user(phone_number, user_role)

        # Prepare response
        response = AuthResponseModel(
            token=token,
            user_id=phone_number,
            role=user_role,
            expires_in=config.JWT_EXPIRATION_HOURS * 3600  # Convert hours to seconds
        )

        logger.info(f"OTP verified successfully for: {phone_number}")

        return func.HttpResponse(
            response.model_dump_json(),
            status_code=200,
            mimetype="application/json"
        )

    except Exception as e:
        logger.error(f"Error in OTP verification: {e}", exc_info=True)
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Internal server error"}),
            status_code=500,
            mimetype="application/json"
        )


async def get_user_role(phone_number: str) -> str:
    """
    Get user role from database or default to 'bhw'

    In production, this would check a users table
    For now, we default everyone to 'bhw' role
    """
    try:
        user = await db.fetchrow("""
            SELECT role FROM users WHERE phone_number = $1
        """, phone_number)

        if user:
            return user['role']
    except Exception:
        pass  # Table might not exist yet

    # Default role
    return "bhw"


async def upsert_user(phone_number: str, role: str):
    """
    Create or update user record in database

    This creates a users table if it doesn't exist and stores basic user info
    """
    try:
        await db.execute("""
            CREATE TABLE IF NOT EXISTS users (
                phone_number TEXT PRIMARY KEY,
                role TEXT NOT NULL DEFAULT 'bhw',
                last_login TIMESTAMP DEFAULT NOW(),
                created_at TIMESTAMP DEFAULT NOW()
            )
        """)

        await db.execute("""
            INSERT INTO users (phone_number, role, last_login)
            VALUES ($1, $2, NOW())
            ON CONFLICT (phone_number)
            DO UPDATE SET last_login = NOW()
        """, phone_number, role)

    except Exception as e:
        logger.error(f"Failed to upsert user: {e}")
