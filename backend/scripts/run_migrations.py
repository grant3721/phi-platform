#!/usr/bin/env python3
"""
Database Migration Runner
Runs all SQL migration files in order
"""
import asyncio
import asyncpg
import os
import sys
from pathlib import Path

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent.parent))

from shared.config import config


async def run_migrations():
    """Run all migration files in order"""

    print("=" * 60)
    print("PHI Platform - Database Migration Runner")
    print("=" * 60)

    # Connect to database
    print(f"\nConnecting to database: {config.DATABASE_HOST}:{config.DATABASE_PORT}/{config.DATABASE_NAME}")

    try:
        # Prepare connection parameters
        conn_params = {
            "host": config.DATABASE_HOST,
            "port": config.DATABASE_PORT,
            "database": config.DATABASE_NAME,
            "user": config.DATABASE_USER,
            "password": config.DATABASE_PASSWORD
        }

        # Add SSL for Azure connections
        if "azure.com" in config.DATABASE_HOST or "postgres.database" in config.DATABASE_HOST:
            conn_params["ssl"] = "require"

        conn = await asyncpg.connect(**conn_params)
        print("[OK] Connected successfully\n")
    except Exception as e:
        print(f"[ERROR] Failed to connect: {e}")
        return False

    # Get migration files
    migrations_dir = Path(__file__).parent.parent / "migrations"
    migration_files = sorted(migrations_dir.glob("*.sql"))

    if not migration_files:
        print("[ERROR] No migration files found in:", migrations_dir)
        await conn.close()
        return False

    print(f"Found {len(migration_files)} migration files:\n")

    # Run each migration
    success_count = 0
    for migration_file in migration_files:
        print(f"Running: {migration_file.name}...")

        try:
            # Read migration file
            sql = migration_file.read_text(encoding='utf-8')

            # Execute migration
            await conn.execute(sql)

            print(f"[OK] {migration_file.name} completed successfully\n")
            success_count += 1

        except Exception as e:
            print(f"[ERROR] {migration_file.name} failed: {e}\n")
            # Continue with remaining migrations

    # Verify schemas
    print("\nVerifying database schemas...")
    schemas = await conn.fetch("""
        SELECT schema_name
        FROM information_schema.schemata
        WHERE schema_name IN ('bronze', 'silver', 'gold')
        ORDER BY schema_name
    """)

    for schema in schemas:
        print(f"[OK] Schema: {schema['schema_name']}")

    # Count tables in each schema
    print("\nTable counts:")
    for schema_name in ['bronze', 'silver', 'gold']:
        count = await conn.fetchval(f"""
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = '{schema_name}'
        """)
        print(f"  {schema_name}: {count} tables")

    # Count seeded data
    print("\nSeeded data:")
    barangay_count = await conn.fetchval("SELECT COUNT(*) FROM silver.barangays")
    threshold_count = await conn.fetchval("SELECT COUNT(*) FROM silver.config_thresholds")
    print(f"  Barangays: {barangay_count}")
    print(f"  Thresholds: {threshold_count}")

    await conn.close()

    print("\n" + "=" * 60)
    print(f"Migration complete: {success_count}/{len(migration_files)} successful")
    print("=" * 60 + "\n")

    return success_count == len(migration_files)


def main():
    """Main entry point"""
    try:
        success = asyncio.run(run_migrations())
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\nMigration cancelled by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n\nUnexpected error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
