"""
Configuration management for Azure Functions
Loads settings from environment variables or local.settings.json
"""
import os
import json
from pathlib import Path
from typing import Optional


def load_local_settings():
    """Load local.settings.json if it exists"""
    settings_path = Path(__file__).parent.parent / "local.settings.json"
    if settings_path.exists():
        try:
            with open(settings_path, 'r') as f:
                data = json.load(f)
                return data.get("Values", {})
        except Exception:
            pass
    return {}


# Load local settings
_local_settings = load_local_settings()


class Config:
    """Application configuration"""

    # Database
    DATABASE_HOST: str = os.getenv("DATABASE_HOST", _local_settings.get("DATABASE_HOST", "localhost"))
    DATABASE_PORT: int = int(os.getenv("DATABASE_PORT", _local_settings.get("DATABASE_PORT", "5432")))
    DATABASE_NAME: str = os.getenv("DATABASE_NAME", _local_settings.get("DATABASE_NAME", "phi_platform"))
    DATABASE_USER: str = os.getenv("DATABASE_USER", _local_settings.get("DATABASE_USER", "postgres"))
    DATABASE_PASSWORD: str = os.getenv("DATABASE_PASSWORD", _local_settings.get("DATABASE_PASSWORD", ""))

    @property
    def DATABASE_URL(self) -> str:
        """PostgreSQL connection string"""
        return f"postgresql://{self.DATABASE_USER}:{self.DATABASE_PASSWORD}@{self.DATABASE_HOST}:{self.DATABASE_PORT}/{self.DATABASE_NAME}"

    # JWT Authentication
    JWT_SECRET_KEY: str = os.getenv("JWT_SECRET_KEY", "dev-secret-key")
    JWT_ALGORITHM: str = os.getenv("JWT_ALGORITHM", "HS256")
    JWT_EXPIRATION_HOURS: int = int(os.getenv("JWT_EXPIRATION_HOURS", "24"))

    # SMS Gateway (for OTP)
    SMS_GATEWAY_API_KEY: Optional[str] = os.getenv("SMS_GATEWAY_API_KEY")
    SMS_GATEWAY_URL: str = os.getenv(
        "SMS_GATEWAY_URL",
        "https://api.semaphore.co/api/v4/otp"
    )

    # Anthropic API (for outbreak analysis)
    ANTHROPIC_API_KEY: Optional[str] = os.getenv("ANTHROPIC_API_KEY")

    # Business Rules
    RESCAN_INTERVAL_HOURS_ANNUAL: int = 24 * 365  # 12 months
    RESCAN_INTERVAL_HOURS_MATERNAL: int = 24 * 180  # 6 months
    RESCAN_INTERVAL_HOURS_HIGH_RISK: int = 24 * 30  # 1 month
    REFERRAL_DUE_HOURS: int = 48  # 48 hours to respond

    # Incentives
    INCENTIVE_PER_SCAN: float = 3.00  # PHP
    DAILY_INCENTIVE_CAP: float = 150.00  # PHP
    MAX_SCANS_PER_DAY: int = 50


config = Config()
