#!/usr/bin/env python3
"""
Quick Azure Database Connection Test
Direct connection test with hardcoded values
"""
import asyncio
import asyncpg


async def test_azure():
    """Test direct connection to Azure PostgreSQL"""

    print("=" * 60)
    print("Testing Azure PostgreSQL Connection...")
    print("=" * 60)

    host = "phi-platform-db-2026.postgres.database.azure.com"
    port = 5432
    database = "phi_platform"
    user = "phiadmin"
    password = "PHI_DevPass_123!"

    print(f"\nHost:     {host}")
    print(f"Port:     {port}")
    print(f"Database: {database}")
    print(f"User:     {user}")
    print()

    try:
        print("Connecting...")
        conn = await asyncpg.connect(
            host=host,
            port=port,
            database=database,
            user=user,
            password=password,
            ssl="require",  # Azure requires SSL
            timeout=30
        )

        print("Connected successfully!\n")

        # Test query
        version = await conn.fetchval("SELECT version()")
        print(f"PostgreSQL Version:\n{version}\n")

        await conn.close()

        print("=" * 60)
        print("SUCCESS: Azure database is accessible!")
        print("=" * 60)
        return True

    except Exception as e:
        print(f"ERROR: {e}\n")
        print("=" * 60)
        print("FAILED: Could not connect to Azure database")
        print("=" * 60)
        return False


if __name__ == "__main__":
    asyncio.run(test_azure())
