"""
Test Analytics Endpoints
Tests the 4 analytics endpoints to verify they work correctly
"""
import asyncio
import aiohttp
import json
from datetime import datetime

BASE_URL = "http://localhost:7076/api"

async def test_analytics_province():
    """Test province-level analytics"""
    print("\n=== Testing analytics/province ===")

    async with aiohttp.ClientSession() as session:
        # Test without province filter (all provinces)
        print("\n1. GET /analytics/province (all provinces)")
        async with session.get(f"{BASE_URL}/analytics/province") as resp:
            status = resp.status
            data = await resp.json()

            if status == 200:
                print(f"  [OK] Status: {status}")
                print(f"  Total patients: {data.get('total_patients', 0)}")
                print(f"  High-risk percentage: {data.get('high_risk_percentage', 0):.1f}%")
                print(f"  Scans (7 days): {data.get('scans_last_7_days', 0)}")
                print(f"  Average systolic BP: {data.get('avg_systolic_bp', 0):.1f}")
                print(f"  Active referrals: {data.get('active_referrals', 0)}")
            else:
                print(f"  [ERROR] Status: {status}")
                print(f"  Response: {json.dumps(data, indent=2)}")

        # Test with province filter
        print("\n2. GET /analytics/province?province=Cavite")
        async with session.get(f"{BASE_URL}/analytics/province?province=Cavite") as resp:
            status = resp.status
            data = await resp.json()

            if status == 200:
                print(f"  [OK] Status: {status}")
                print(f"  Province: {data.get('province', 'N/A')}")
                print(f"  Total patients: {data.get('total_patients', 0)}")
                print(f"  Scans (30 days): {data.get('scans_last_30_days', 0)}")
            else:
                print(f"  [ERROR] Status: {status}")

async def test_analytics_municipality():
    """Test municipality-level analytics"""
    print("\n=== Testing analytics/municipality ===")

    async with aiohttp.ClientSession() as session:
        # Test with municipality filter
        print("\n1. GET /analytics/municipality?municipality=Bacoor&province=Cavite")
        async with session.get(
            f"{BASE_URL}/analytics/municipality?municipality=Bacoor&province=Cavite"
        ) as resp:
            status = resp.status
            data = await resp.json()

            if status == 200:
                print(f"  [OK] Status: {status}")
                print(f"  Municipality: {data.get('municipality', 'N/A')}")
                print(f"  Total patients: {data.get('total_patients', 0)}")
                print(f"  Total scans: {data.get('total_scans', 0)}")
                print(f"  Coverage percentage: {data.get('coverage_percentage', 0):.1f}%")

                barangays = data.get('barangays', [])
                if barangays:
                    print(f"  Barangays: {len(barangays)}")
                    # Check which field name is used
                    barangay_name = barangays[0].get('barangay_name') or barangays[0].get('name')
                    patient_count = barangays[0].get('patient_count') or barangays[0].get('registered_patients', 0)
                    print(f"    Top barangay: {barangay_name} "
                          f"({patient_count} patients)")
            else:
                print(f"  [ERROR] Status: {status}")
                print(f"  Response: {json.dumps(data, indent=2)}")

async def test_outbreak_signals():
    """Test outbreak signal detection"""
    print("\n=== Testing outbreak/signals ===")

    async with aiohttp.ClientSession() as session:
        # Test GET (detect patterns from recent scans)
        print("\n1. GET /outbreak/signals?days=7")
        async with session.get(f"{BASE_URL}/outbreak/signals?days=7") as resp:
            status = resp.status
            data = await resp.json()

            if status == 200:
                print(f"  [OK] Status: {status}")
                summary = data.get('summary', {})
                total = summary.get('total_signals', 0)
                print(f"  Total signals detected: {total}")
                print(f"  - Respiratory: {summary.get('respiratory_count', 0)}")
                print(f"  - Hypertension: {summary.get('hypertension_count', 0)}")
                print(f"  - Diabetes: {summary.get('diabetes_count', 0)}")

                # Show sample signals if any
                signals = data.get('signals', {})
                respiratory = signals.get('respiratory', [])
                if respiratory:
                    print(f"\n  Sample respiratory signal:")
                    sig = respiratory[0]
                    print(f"    Barangay: {sig.get('barangay_name')}, {sig.get('municipality')}")
                    print(f"    SpO2 deviation: {sig.get('spo2_deviation'):.1f}")
            else:
                print(f"  [ERROR] Status: {status}")

async def test_outbreak_analyze():
    """Test outbreak analysis with Claude AI"""
    print("\n=== Testing outbreak/analyze ===")

    async with aiohttp.ClientSession() as session:
        # Test analyzing all pending signals
        print("\n1. GET /outbreak/analyze (analyze all pending)")
        async with session.get(f"{BASE_URL}/outbreak/analyze") as resp:
            status = resp.status
            data = await resp.json()

            if status == 200:
                print(f"  [OK] Status: {status}")
                analyzed_count = data.get('analyzed_count', 0)
                print(f"  Analyzed count: {analyzed_count}")

                if analyzed_count == 0:
                    print(f"  Message: {data.get('message', 'No signals pending analysis')}")
                else:
                    analyses = data.get('analyses', [])
                    if analyses:
                        # Show first analysis
                        first = analyses[0]
                        analysis = first.get('analysis', {})
                        print(f"\n  Sample Analysis:")
                        print(f"    Signal: {first.get('signal_type', 'N/A')} in {first.get('barangay', 'N/A')}")
                        print(f"    Risk Level: {analysis.get('risk_level', 'N/A')}")

                        if 'immediate_actions' in analysis:
                            actions = analysis['immediate_actions'][:2]
                            print(f"    Immediate Actions:")
                            for action in actions:
                                print(f"      - {action}")
            else:
                print(f"  [ERROR] Status: {status}")
                if 'error' in data:
                    print(f"  Error: {data['error']}")

async def main():
    print("=" * 60)
    print("PHI Platform Analytics Endpoints Test")
    print("=" * 60)
    print(f"Base URL: {BASE_URL}")
    print(f"Started: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

    try:
        # Test all 4 analytics endpoints
        await test_analytics_province()
        await test_analytics_municipality()
        await test_outbreak_signals()
        await test_outbreak_analyze()

        print("\n" + "=" * 60)
        print("Analytics Endpoints Test Complete")
        print("=" * 60)

    except aiohttp.ClientConnectorError:
        print("\n[ERROR] Could not connect to backend")
        print("Make sure Azure Functions is running on port 7074")
    except Exception as e:
        print(f"\n[ERROR] Test failed: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    asyncio.run(main())
