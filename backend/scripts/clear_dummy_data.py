#!/usr/bin/env python3
"""Clear all dummy data from silver schema"""
import asyncio
import asyncpg
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))
from shared.config import config


async def main():
    conn_params = {
        "host": config.DATABASE_HOST,
        "port": config.DATABASE_PORT,
        "database": config.DATABASE_NAME,
        "user": config.DATABASE_USER,
        "password": config.DATABASE_PASSWORD,
        "ssl": "require"
    }

    conn = await asyncpg.connect(**conn_params)
    print("Clearing existing data...")

    await conn.execute("DELETE FROM silver.bhw_incentives")
    await conn.execute("DELETE FROM silver.referrals")
    await conn.execute("DELETE FROM silver.surveys")
    await conn.execute("DELETE FROM silver.scans")
    await conn.execute("DELETE FROM silver.patients")

    print("[OK] All dummy data cleared")
    await conn.close()


if __name__ == "__main__":
    asyncio.run(main())
