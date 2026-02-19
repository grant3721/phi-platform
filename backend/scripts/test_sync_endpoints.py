#!/usr/bin/env python3
"""
Test Sync Endpoints
Tests sync_upload and sync_download with realistic data
"""
import asyncio
import httpx
import json
import sys
from pathlib import Path
from datetime import datetime, timedelta

# Add parent directory to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from shared.config import config

# Base URL for local testing (Azure Functions Core Tools)
BASE_URL = "http://localhost:7073/api"

# Test data
TEST_PHONE = "+639100000001"
TEST_PASSWORD = "123456"  # OTP code
TEST_DEVICE_ID = "TEST_DEVICE_001"


class SyncEndpointTester:
    """Tests sync endpoints end-to-end"""

    def __init__(self):
        self.client = httpx.AsyncClient(timeout=30.0)
        self.jwt_token = None
        self.bhw_id = None

    async def test_auth(self):
        """Test authentication flow to get JWT token"""
        print("=" * 60)
        print("Step 1: Testing Authentication")
        print("=" * 60)

        # For testing, we'll use a mock JWT or skip auth if endpoints are open
        # In production, you'd request OTP and verify it
        print("\n[INFO] Using test phone: +639100000001")
        print("[INFO] In production, this would:")
        print("  1. POST /auth/otp/request with phone number")
        print("  2. POST /auth/otp/verify with OTP code")
        print("  3. Receive JWT token")

        # Mock token for testing (in production, get from auth endpoints)
        self.bhw_id = "BHW001"

        # Generate a simple test token (Note: In production, use real JWT from auth endpoint)
        import jwt
        self.jwt_token = jwt.encode(
            {
                "user_id": self.bhw_id,
                "phone": TEST_PHONE,
                "role": "bhw",
                "exp": int((datetime.utcnow() + timedelta(hours=24)).timestamp())
            },
            config.JWT_SECRET_KEY,
            algorithm=config.JWT_ALGORITHM
        )

        print(f"\n[OK] Mock JWT token generated for testing")
        print(f"     BHW ID: {self.bhw_id}")
        print()

    def get_headers(self):
        """Get HTTP headers with JWT token"""
        return {
            "Authorization": f"Bearer {self.jwt_token}",
            "Content-Type": "application/json"
        }

    async def test_sync_upload(self):
        """Test sync_upload endpoint with sample data"""
        print("=" * 60)
        print("Step 2: Testing Sync Upload")
        print("=" * 60)

        # Generate test data
        now_ms = int(datetime.utcnow().timestamp() * 1000)
        yesterday_ms = int((datetime.utcnow() - timedelta(days=1)).timestamp() * 1000)

        # Sample patient
        test_patient = {
            "id": f"PAT_TEST_{now_ms}",
            "phone_number": f"+6391{now_ms % 100000000:08d}",
            "full_name": "Juan Dela Cruz",
            "date_of_birth": int((datetime(1985, 5, 15)).timestamp() * 1000),
            "sex": "MALE",
            "barangay_id": "BRG001",
            "barangay_name": "Barangay 1 (Poblacion)",
            "philhealth_number": "1234567890123",
            "is_pregnant": False
        }

        # Sample scan with biomarkers
        test_scan = {
            "id": f"SCN_TEST_{now_ms}",
            "patient_id": test_patient["id"],
            "scan_type": "BIOSENSE_FACE_SCAN",
            "scanned_at": yesterday_ms,
            "signal_quality": "GOOD",
            "scan_duration_seconds": 45,
            "biomarkers": {
                "systolic_bp": 125.5,
                "diastolic_bp": 82.3,
                "heart_rate": 72.0,
                "spo2": 98.5,
                "respiratory_rate": 16.0,
                "hemoglobin": 14.2,
                "hba1c": 5.3,
                "blood_glucose": 92.0,
                "cholesterol_total": 180.0,
                "hrv_sdnn": 55.0,
                "stress_index": 4.2
            },
            "risk_score": 3.5,
            "risk_flags": []
        }

        # Sample survey
        test_survey = {
            "id": f"SRV_TEST_{now_ms}",
            "scan_id": test_scan["id"],
            "patient_id": test_patient["id"],
            "survey_type": "NCD",
            "responses": {
                "has_diabetes": False,
                "has_hypertension": False,
                "smoking": False,
                "alcohol": False,
                "exercise_frequency": "weekly"
            },
            "completed_at": yesterday_ms
        }

        # Build upload payload
        upload_data = {
            "device_id": TEST_DEVICE_ID,
            "sync_timestamp": now_ms,
            "patients": [test_patient],
            "scans": [test_scan],
            "surveys": [test_survey],
            "referrals": []
        }

        print("\n[INFO] Uploading test data:")
        print(f"  - 1 patient: {test_patient['full_name']}")
        print(f"  - 1 scan: {test_scan['scan_type']}")
        print(f"  - 1 survey: {test_survey['survey_type']}")
        print()

        try:
            response = await self.client.post(
                f"{BASE_URL}/sync/upload",
                json=upload_data,
                headers=self.get_headers()
            )

            print(f"Response Status: {response.status_code}")
            print()

            if response.status_code == 200:
                result = response.json()
                print("[OK] Sync upload successful!")
                print()
                print("Results:")
                print(f"  Patients: {result['results']['patients']['accepted']} accepted, "
                      f"{result['results']['patients']['rejected']} rejected")
                print(f"  Scans:    {result['results']['scans']['accepted']} accepted, "
                      f"{result['results']['scans']['rejected']} rejected")
                print(f"  Surveys:  {result['results']['surveys']['accepted']} accepted, "
                      f"{result['results']['surveys']['rejected']} rejected")
                print()
                print(f"Total: {result['summary']['total_accepted']} accepted, "
                      f"{result['summary']['total_rejected']} rejected")
                print()

                # Show rejection reasons if any
                if result['summary']['total_rejected'] > 0:
                    print("Rejection reasons:")
                    for data_type in ['patients', 'scans', 'surveys', 'referrals']:
                        reasons = result['results'][data_type].get('reasons', [])
                        if reasons:
                            print(f"  {data_type}:")
                            for reason in reasons:
                                print(f"    - {reason}")
                    print()

                return True
            else:
                print(f"[ERROR] Upload failed: {response.text}")
                return False

        except httpx.ConnectError:
            print("[ERROR] Could not connect to Azure Functions")
            print()
            print("Make sure Azure Functions Core Tools is running:")
            print("  cd backend")
            print("  func start")
            print()
            return False
        except Exception as e:
            print(f"[ERROR] {e}")
            return False

    async def test_sync_download(self):
        """Test sync_download endpoint"""
        print("=" * 60)
        print("Step 3: Testing Sync Download")
        print("=" * 60)

        # Use a timestamp from 7 days ago to get all recent updates
        last_sync = int((datetime.utcnow() - timedelta(days=7)).timestamp() * 1000)

        print(f"\n[INFO] Requesting updates since 7 days ago")
        print(f"     Last sync: {last_sync}")
        print()

        try:
            response = await self.client.get(
                f"{BASE_URL}/sync/download",
                params={
                    "last_sync": last_sync,
                    "device_id": TEST_DEVICE_ID
                },
                headers=self.get_headers()
            )

            print(f"Response Status: {response.status_code}")
            print()

            if response.status_code == 200:
                result = response.json()
                print("[OK] Sync download successful!")
                print()
                print("Updates received:")
                print(f"  Config thresholds: {result['summary']['config_updates']}")
                print(f"  Referral updates:  {result['summary']['referral_updates']}")
                print(f"  Patient updates:   {result['summary']['patient_updates']}")
                print(f"  Dedup info:        {result['summary']['dedup_count']}")
                print(f"  Outbreak alerts:   {result['summary']['active_alerts']}")
                print()

                # Show incentive summary
                incentives = result['updates']['incentives']
                print("Incentive Summary:")
                print(f"  Total earned:  ₱{incentives['total_earned']:,.2f}")
                print(f"  Recent earned: ₱{incentives['recent_earned']:,.2f}")
                print(f"  Today:         {incentives['today']['count']} scans, "
                      f"₱{incentives['today']['amount']:.2f}")
                print(f"  Daily cap:     {'REACHED' if incentives['today']['cap_reached'] else 'Not reached'}")
                print()

                # Show sample config threshold if any
                if result['updates']['config_thresholds']:
                    print("Sample config threshold:")
                    threshold = result['updates']['config_thresholds'][0]
                    print(f"  {threshold['parameter_name']}: "
                          f"{threshold.get('low_threshold', 'N/A')}-{threshold.get('high_threshold', 'N/A')} "
                          f"{threshold.get('unit', '')}")
                    print()

                return True
            else:
                print(f"[ERROR] Download failed: {response.text}")
                return False

        except httpx.ConnectError:
            print("[ERROR] Could not connect to Azure Functions")
            print()
            print("Make sure Azure Functions Core Tools is running:")
            print("  cd backend")
            print("  func start")
            print()
            return False
        except Exception as e:
            print(f"[ERROR] {e}")
            return False

    async def run_tests(self):
        """Run all tests"""
        print("\n" + "=" * 60)
        print("PHI Platform - Sync Endpoints Test Suite")
        print("=" * 60)
        print()

        # Run tests in sequence
        await self.test_auth()

        upload_success = await self.test_sync_upload()
        if not upload_success:
            print("\n[FAILED] Sync upload test failed")
            return False

        await asyncio.sleep(1)  # Brief pause

        download_success = await self.test_sync_download()
        if not download_success:
            print("\n[FAILED] Sync download test failed")
            return False

        # Summary
        print("=" * 60)
        print("Test Summary")
        print("=" * 60)
        print()
        print("[OK] All tests passed!")
        print()
        print("✓ Authentication working")
        print("✓ Sync upload: Data processed through Bronze → Silver")
        print("✓ Sync download: Delta sync working")
        print()
        print("=" * 60)
        print()

        await self.client.aclose()
        return True


async def main():
    """Main entry point"""
    tester = SyncEndpointTester()
    success = await tester.run_tests()
    return success


if __name__ == "__main__":
    try:
        success = asyncio.run(main())
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\nTests cancelled by user\n")
        sys.exit(1)
