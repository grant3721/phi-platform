#!/usr/bin/env python3
"""
Database Connection Test
Tests connection to PostgreSQL database
"""
import asyncio
import asyncpg
import sys
from pathlib import Path

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent.parent))

from shared.config import config


async def test_connection():
    """Test database connection"""

    print("=" * 60)
    print("PHI Platform - Database Connection Test")
    print("=" * 60)
    print(f"\nHost:     {config.DATABASE_HOST}")
    print(f"Port:     {config.DATABASE_PORT}")
    print(f"Database: {config.DATABASE_NAME}")
    print(f"User:     {config.DATABASE_USER}")
    print(f"Password: {'*' * len(config.DATABASE_PASSWORD)}")
    print()

    try:
        print("Connecting...")
        conn = await asyncpg.connect(
            host=config.DATABASE_HOST,
            port=config.DATABASE_PORT,
            database=config.DATABASE_NAME,
            user=config.DATABASE_USER,
            password=config.DATABASE_PASSWORD,
            timeout=10
        )

        print("✓ Connection successful!\n")

        # Get PostgreSQL version
        version = await conn.fetchval("SELECT version()")
        print(f"PostgreSQL Version:\n{version}\n")

        # Check if schemas exist
        print("Checking schemas...")
        schemas = await conn.fetch("""
            SELECT schema_name
            FROM information_schema.schemata
            WHERE schema_name IN ('bronze', 'silver', 'gold', 'public')
            ORDER BY schema_name
        """)

        if schemas:
            print("✓ Found schemas:")
            for schema in schemas:
                # Count tables in this schema
                table_count = await conn.fetchval("""
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = $1
                """, schema['schema_name'])
                print(f"  - {schema['schema_name']}: {table_count} tables")
        else:
            print("! No schemas found (migrations not run yet)")

        print()

        # Test query performance
        print("Testing query performance...")
        import time
        start = time.time()
        await conn.fetchval("SELECT 1")
        duration = (time.time() - start) * 1000
        print(f"✓ Query latency: {duration:.2f}ms\n")

        await conn.close()

        print("=" * 60)
        print("✓ All tests passed - Database ready!")
        print("=" * 60)
        print()
        return True

    except asyncpg.InvalidCatalogNameError:
        print(f"✗ Database '{config.DATABASE_NAME}' does not exist")
        print("\nTo create the database, run:")
        print(f"  createdb -h {config.DATABASE_HOST} -p {config.DATABASE_PORT} -U {config.DATABASE_USER} {config.DATABASE_NAME}")
        print("\nOr connect to postgres and run:")
        print(f"  CREATE DATABASE {config.DATABASE_NAME};")
        print()
        return False

    except asyncpg.InvalidPasswordError:
        print("✗ Authentication failed - Invalid password")
        print("\nCheck your DATABASE_PASSWORD in local.settings.json")
        print()
        return False

    except asyncpg.PostgresConnectionError as e:
        print(f"✗ Connection failed: {e}")
        print("\nPossible issues:")
        print("  1. PostgreSQL is not running")
        print("  2. Wrong host or port")
        print("  3. Firewall blocking connection")
        print("\nIf using Docker:")
        print("  docker-compose up -d")
        print()
        return False

    except Exception as e:
        print(f"✗ Unexpected error: {e}")
        print()
        return False


def main():
    """Main entry point"""
    try:
        success = asyncio.run(test_connection())
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\nTest cancelled by user\n")
        sys.exit(1)


if __name__ == "__main__":
    main()
