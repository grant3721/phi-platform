"""
Authentication utilities
JWT token generation and verification
"""
import jwt
from datetime import datetime, timedelta
from typing import Optional, Dict, Any
import logging

from .config import config

logger = logging.getLogger(__name__)


def generate_jwt(user_id: str, role: str = "bhw", **extra_claims) -> str:
    """
    Generate JWT token for authenticated user

    Args:
        user_id: User identifier (phone number or ID)
        role: User role (bhw, bhs, rhu, admin)
        **extra_claims: Additional claims to include

    Returns:
        JWT token string
    """
    now = datetime.utcnow()
    expires = now + timedelta(hours=config.JWT_EXPIRATION_HOURS)

    payload = {
        "user_id": user_id,
        "role": role,
        "iat": now,
        "exp": expires,
        **extra_claims
    }

    token = jwt.encode(
        payload,
        config.JWT_SECRET_KEY,
        algorithm=config.JWT_ALGORITHM
    )

    return token


def verify_jwt(token: str) -> Optional[Dict[str, Any]]:
    """
    Verify and decode JWT token

    Args:
        token: JWT token string

    Returns:
        Decoded payload if valid, None if invalid
    """
    try:
        payload = jwt.decode(
            token,
            config.JWT_SECRET_KEY,
            algorithms=[config.JWT_ALGORITHM]
        )
        return payload
    except jwt.ExpiredSignatureError:
        logger.warning("JWT token expired")
        return None
    except jwt.InvalidTokenError as e:
        logger.warning(f"Invalid JWT token: {e}")
        return None


def extract_bearer_token(auth_header: Optional[str]) -> Optional[str]:
    """
    Extract token from Authorization header

    Args:
        auth_header: Authorization header value (e.g., "Bearer <token>")

    Returns:
        Token string if valid, None otherwise
    """
    if not auth_header:
        return None

    parts = auth_header.split()
    if len(parts) != 2 or parts[0].lower() != "bearer":
        return None

    return parts[1]


def get_user_from_request(req) -> Optional[Dict[str, Any]]:
    """
    Extract and verify user from request Authorization header

    Args:
        req: Azure Functions HttpRequest object

    Returns:
        User payload if authenticated, None otherwise
    """
    auth_header = req.headers.get("Authorization")
    token = extract_bearer_token(auth_header)

    if not token:
        return None

    return verify_jwt(token)


def require_auth(req, required_role: Optional[str] = None) -> tuple[bool, Optional[str], Optional[Dict[str, Any]]]:
    """
    Check if request is authenticated and authorized

    Args:
        req: Azure Functions HttpRequest object
        required_role: Required role (if None, any authenticated user is allowed)

    Returns:
        Tuple of (is_authorized, error_message, user_payload)
    """
    user = get_user_from_request(req)

    if not user:
        return False, "Unauthorized: Invalid or missing token", None

    if required_role and user.get("role") != required_role:
        return False, f"Forbidden: {required_role} role required", None

    return True, None, user
