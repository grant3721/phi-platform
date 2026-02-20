"""
Database connection pool management
"""
import asyncpg
import os
from typing import Optional

from .config import config

# Global connection pool
_pool: Optional[asyncpg.Pool] = None


async def get_db_pool() -> asyncpg.Pool:
    """
    Get or create database connection pool
    Returns a connection pool for asyncpg
    """
    global _pool

    if _pool is None:
        # Connection parameters
        conn_params = {
            "host": config.DATABASE_HOST,
            "port": config.DATABASE_PORT,
            "database": config.DATABASE_NAME,
            "user": config.DATABASE_USER,
            "password": config.DATABASE_PASSWORD,
            "min_size": 2,
            "max_size": 10,
            "command_timeout": 30
        }

        # Add SSL for Azure connections
        if "azure.com" in config.DATABASE_HOST or "postgres.database" in config.DATABASE_HOST:
            conn_params["ssl"] = "require"

        _pool = await asyncpg.create_pool(**conn_params)

    return _pool


async def close_db_pool():
    """Close the database connection pool"""
    global _pool

    if _pool is not None:
        await _pool.close()
        _pool = None
