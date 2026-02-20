"""
Shared utility functions for PHI Platform backend
"""
from decimal import Decimal
from datetime import date, datetime
from typing import Any, Dict, List, Union


def decimal_to_float(obj: Any) -> Any:
    """
    Recursively convert Decimal and datetime objects for JSON serialization
    Handles dicts, lists, and nested structures
    """
    if isinstance(obj, Decimal):
        return float(obj)
    elif isinstance(obj, datetime):
        # Convert datetime to milliseconds timestamp
        return int(obj.timestamp() * 1000)
    elif isinstance(obj, date):
        # Convert date to ISO format string
        return obj.isoformat()
    elif isinstance(obj, dict):
        return {key: decimal_to_float(value) for key, value in obj.items()}
    elif isinstance(obj, list):
        return [decimal_to_float(item) for item in obj]
    elif isinstance(obj, tuple):
        return tuple(decimal_to_float(item) for item in obj)
    else:
        return obj
