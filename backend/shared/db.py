"""
Database connection management
Uses asyncpg for async PostgreSQL operations
"""
import asyncpg
from typing import Optional, List, Dict, Any
from contextlib import asynccontextmanager
import logging

from .config import config

logger = logging.getLogger(__name__)


class Database:
    """Database connection pool manager"""

    def __init__(self):
        self.pool: Optional[asyncpg.Pool] = None

    async def connect(self):
        """Create connection pool"""
        if self.pool is None:
            try:
                self.pool = await asyncpg.create_pool(
                    host=config.DATABASE_HOST,
                    port=config.DATABASE_PORT,
                    database=config.DATABASE_NAME,
                    user=config.DATABASE_USER,
                    password=config.DATABASE_PASSWORD,
                    min_size=2,
                    max_size=10,
                    command_timeout=60
                )
                logger.info("Database connection pool created")
            except Exception as e:
                logger.error(f"Failed to create database pool: {e}")
                raise

    async def disconnect(self):
        """Close connection pool"""
        if self.pool is not None:
            await self.pool.close()
            self.pool = None
            logger.info("Database connection pool closed")

    @asynccontextmanager
    async def acquire(self):
        """Acquire a connection from the pool"""
        if self.pool is None:
            await self.connect()

        async with self.pool.acquire() as connection:
            yield connection

    async def execute(self, query: str, *args) -> str:
        """Execute a query (INSERT/UPDATE/DELETE)"""
        async with self.acquire() as conn:
            return await conn.execute(query, *args)

    async def fetch(self, query: str, *args) -> List[asyncpg.Record]:
        """Fetch multiple rows"""
        async with self.acquire() as conn:
            return await conn.fetch(query, *args)

    async def fetchrow(self, query: str, *args) -> Optional[asyncpg.Record]:
        """Fetch single row"""
        async with self.acquire() as conn:
            return await conn.fetchrow(query, *args)

    async def fetchval(self, query: str, *args) -> Any:
        """Fetch single value"""
        async with self.acquire() as conn:
            return await conn.fetchval(query, *args)


# Global database instance
db = Database()


def record_to_dict(record: Optional[asyncpg.Record]) -> Optional[Dict[str, Any]]:
    """Convert asyncpg Record to dictionary"""
    if record is None:
        return None
    return dict(record)


def records_to_list(records: List[asyncpg.Record]) -> List[Dict[str, Any]]:
    """Convert list of asyncpg Records to list of dictionaries"""
    return [dict(record) for record in records]
