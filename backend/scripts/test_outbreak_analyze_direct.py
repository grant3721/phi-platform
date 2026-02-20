"""
Direct test of outbreak_analyze logic to see errors
"""
import asyncio
import asyncpg
import sys
import os

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from shared.db import get_db_pool

async def test():
    try:
        print("Getting database pool...")
        pool = await get_db_pool()
        print(f"Pool created: {pool}")

        async with pool.acquire() as conn:
            print("Connection acquired")

            # Try the query from outbreak_analyze
            query = """
                SELECT
                    s.id,
                    s.barangay_id,
                    b.name as barangay_name,
                    b.municipality,
                    b.province,
                    s.signal_type,
                    s.severity,
                    s.status,
                    s.detected_at,
                    s.description,
                    s.signal_data,
                    s.recommended_actions
                FROM gold.outbreak_signals s
                INNER JOIN silver.barangays b ON b.id = s.barangay_id
                WHERE s.status = 'ACTIVE'
                  AND (s.recommended_actions IS NULL OR s.recommended_actions = '{}')
                ORDER BY s.severity DESC, s.detected_at DESC
                LIMIT 10
            """

            print("Executing query...")
            results = await conn.fetch(query)
            print(f"Query successful! Found {len(results)} signals")

            if len(results) == 0:
                print("No signals to analyze (expected since table is empty)")

    except Exception as e:
        print(f"ERROR: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    asyncio.run(test())
