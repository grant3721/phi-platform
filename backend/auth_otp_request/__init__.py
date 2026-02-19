"""
Auth OTP Request Endpoint
Sends OTP code via SMS for authentication
"""
import azure.functions as func
import logging
import json
import random
import httpx
from datetime import datetime, timedelta
from pydantic import ValidationError

from shared import db, config, OTPRequestModel

logger = logging.getLogger(__name__)


async def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    POST /api/auth/otp/request

    Request body:
    {
        "phone_number": "+639171234567"
    }

    Response:
    {
        "success": true,
        "message": "OTP sent to +639171234567",
        "expires_in": 300
    }
    """
    try:
        # Parse request body
        try:
            body = req.get_json()
            request_data = OTPRequestModel(**body)
        except (ValueError, ValidationError) as e:
            return func.HttpResponse(
                json.dumps({"success": False, "error": f"Invalid request: {str(e)}"}),
                status_code=400,
                mimetype="application/json"
            )

        phone_number = request_data.phone_number
        logger.info(f"OTP request for phone: {phone_number}")

        # Generate 6-digit OTP
        otp_code = str(random.randint(100000, 999999))

        # Set expiration (5 minutes)
        expires_at = int((datetime.utcnow() + timedelta(minutes=5)).timestamp() * 1000)

        # Connect to database
        await db.connect()

        # Store OTP in database
        await db.execute("""
            CREATE TABLE IF NOT EXISTS otp_codes (
                phone_number TEXT PRIMARY KEY,
                otp_code TEXT NOT NULL,
                expires_at BIGINT NOT NULL,
                created_at TIMESTAMP DEFAULT NOW()
            )
        """)

        # Upsert OTP (replace if exists)
        await db.execute("""
            INSERT INTO otp_codes (phone_number, otp_code, expires_at)
            VALUES ($1, $2, $3)
            ON CONFLICT (phone_number)
            DO UPDATE SET otp_code = $2, expires_at = $3, created_at = NOW()
        """, phone_number, otp_code, expires_at)

        # Send OTP via SMS gateway
        sms_sent = await send_otp_sms(phone_number, otp_code)

        if not sms_sent:
            logger.warning(f"SMS gateway failed for {phone_number}, but OTP stored for testing")
            # In development, we continue even if SMS fails
            # In production, you might want to return an error

        return func.HttpResponse(
            json.dumps({
                "success": True,
                "message": f"OTP sent to {phone_number}",
                "expires_in": 300,  # 5 minutes in seconds
                # DEV ONLY: Include OTP in response for testing
                "otp_code": otp_code if not config.SMS_GATEWAY_API_KEY else None
            }),
            status_code=200,
            mimetype="application/json"
        )

    except Exception as e:
        logger.error(f"Error in OTP request: {e}", exc_info=True)
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Internal server error"}),
            status_code=500,
            mimetype="application/json"
        )


async def send_otp_sms(phone_number: str, otp_code: str) -> bool:
    """
    Send OTP via SMS gateway (Semaphore or similar)

    Returns:
        True if SMS sent successfully, False otherwise
    """
    if not config.SMS_GATEWAY_API_KEY:
        logger.warning("SMS_GATEWAY_API_KEY not configured, skipping SMS send")
        return False

    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                config.SMS_GATEWAY_URL,
                json={
                    "apikey": config.SMS_GATEWAY_API_KEY,
                    "number": phone_number,
                    "message": f"Your GO PHI verification code is: {otp_code}. Valid for 5 minutes.",
                    "sendername": "GOPHI"
                },
                timeout=10.0
            )

            if response.status_code == 200:
                logger.info(f"OTP SMS sent successfully to {phone_number}")
                return True
            else:
                logger.error(f"SMS gateway error: {response.status_code} - {response.text}")
                return False

    except Exception as e:
        logger.error(f"Failed to send SMS: {e}")
        return False
