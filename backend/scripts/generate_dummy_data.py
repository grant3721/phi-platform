#!/usr/bin/env python3
"""
Dummy Data Generator for PHI Platform
Generates realistic test data for development and testing:
- 1,000 patients
- 50,000 scans (realistic distribution)
- Surveys linked to scans
- Referrals for high-risk patients
- Clinical encounters
- BHW incentive records
"""
import asyncio
import asyncpg
import random
import sys
import json
from pathlib import Path
from datetime import datetime, timedelta
from typing import List, Dict

# Add parent directory to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from shared.config import config

# Filipino names for realistic data
FIRST_NAMES_MALE = [
    "Juan", "Jose", "Antonio", "Pedro", "Francisco", "Luis", "Miguel",
    "Ramon", "Carlos", "Ricardo", "Roberto", "Fernando", "Mario", "Eduardo",
    "Rafael", "Manuel", "Jorge", "Alberto", "Daniel", "Javier"
]

FIRST_NAMES_FEMALE = [
    "Maria", "Ana", "Rosa", "Carmen", "Teresa", "Elena", "Isabel", "Patricia",
    "Luz", "Angela", "Gloria", "Josefa", "Rosario", "Cristina", "Margarita",
    "Sofia", "Beatriz", "Victoria", "Clara", "Dolores"
]

LAST_NAMES = [
    "Santos", "Reyes", "Cruz", "Bautista", "Ocampo", "Garcia", "Mendoza",
    "Torres", "Gonzales", "Lopez", "Ramos", "Rivera", "Fernandez", "Martinez",
    "Sanchez", "Castillo", "Morales", "Aquino", "Villanueva", "Dela Cruz",
    "San Jose", "Flores", "Navarro", "Ramirez", "Pascual", "Mercado", "Diaz"
]


class DummyDataGenerator:
    """Generates realistic dummy data for the PHI Platform"""

    def __init__(self, conn: asyncpg.Connection):
        self.conn = conn
        self.barangays = []
        self.thresholds = {}
        self.patient_ids = []
        self.scan_ids = []
        self.bhw_ids = []

    async def load_reference_data(self):
        """Load barangays and thresholds from database"""
        print("Loading reference data...")

        # Load barangays
        self.barangays = await self.conn.fetch(
            "SELECT id, name, municipality FROM silver.barangays WHERE active = TRUE"
        )
        print(f"  Loaded {len(self.barangays)} barangays")

        # Load thresholds
        thresholds = await self.conn.fetch(
            "SELECT parameter_name, low_threshold, high_threshold FROM silver.config_thresholds WHERE active = TRUE"
        )
        self.thresholds = {t['parameter_name']: (t['low_threshold'], t['high_threshold']) for t in thresholds}
        print(f"  Loaded {len(self.thresholds)} clinical thresholds\n")

    def generate_phone_number(self, index: int) -> str:
        """Generate unique Philippine mobile number"""
        # Format: +639XXXXXXXXX (Philippine mobile format)
        return f"+6391{index:08d}"

    def generate_name(self, sex: str) -> str:
        """Generate realistic Filipino name"""
        first_names = FIRST_NAMES_MALE if sex == 'MALE' else FIRST_NAMES_FEMALE
        first_name = random.choice(first_names)
        middle_name = random.choice(LAST_NAMES)
        last_name = random.choice(LAST_NAMES)
        return f"{first_name} {middle_name[0]}. {last_name}"

    def generate_age(self) -> int:
        """Generate age with realistic distribution"""
        # Weighted toward common screening ages (limit max age to avoid timestamp issues)
        weights = [5, 10, 20, 30, 20, 10, 5]  # 0-10, 11-20, 21-30, 31-40, 41-50, 51-60, 61+
        age_group = random.choices(range(7), weights=weights)[0]

        age_ranges = [(0, 10), (11, 20), (21, 30), (31, 40), (41, 50), (51, 60), (61, 75)]
        min_age, max_age = age_ranges[age_group]
        return random.randint(min_age, max_age)

    def generate_biomarkers(self, age: int, sex: str, high_risk: bool = False) -> Dict:
        """Generate realistic biomarkers based on age, sex, and risk level"""
        biomarkers = {}

        # Vital signs
        if high_risk:
            # Out of normal range
            biomarkers['systolic_bp'] = random.uniform(145, 180)
            biomarkers['diastolic_bp'] = random.uniform(95, 110)
            biomarkers['heart_rate'] = random.uniform(100, 130)
            biomarkers['spo2'] = random.uniform(88, 94)
            biomarkers['respiratory_rate'] = random.uniform(22, 30)
        else:
            # Normal range with slight variations
            biomarkers['systolic_bp'] = random.uniform(100, 135)
            biomarkers['diastolic_bp'] = random.uniform(65, 88)
            biomarkers['heart_rate'] = random.uniform(65, 95)
            biomarkers['spo2'] = random.uniform(95, 100)
            biomarkers['respiratory_rate'] = random.uniform(14, 18)

        # Bloodless tests
        if high_risk:
            biomarkers['hemoglobin'] = random.uniform(9, 11)
            biomarkers['hba1c'] = random.uniform(6.5, 9.0)
            biomarkers['blood_glucose'] = random.uniform(126, 200)
        else:
            biomarkers['hemoglobin'] = random.uniform(12, 16)
            biomarkers['hba1c'] = random.uniform(4.5, 5.5)
            biomarkers['blood_glucose'] = random.uniform(75, 95)

        # Risk scores
        if high_risk:
            biomarkers['diabetes_risk'] = random.uniform(7.5, 10)
            biomarkers['cvd_risk'] = random.uniform(7.5, 10)
            biomarkers['stroke_risk'] = random.uniform(7.5, 10)
        else:
            biomarkers['diabetes_risk'] = random.uniform(1, 6)
            biomarkers['cvd_risk'] = random.uniform(1, 6)
            biomarkers['stroke_risk'] = random.uniform(1, 6)

        # Add remaining biomarkers (simplified)
        biomarkers['body_temp'] = random.uniform(36.2, 37.2)
        biomarkers['hrv_sdnn'] = random.uniform(40, 80)
        biomarkers['stress_index'] = random.uniform(1, 9)
        biomarkers['cholesterol_total'] = random.uniform(150, 240)
        biomarkers['bmi'] = random.uniform(18.5, 32)

        return biomarkers

    async def generate_patients(self, count: int = 1000):
        """Generate patient records"""
        print(f"Generating {count} patients...")

        for i in range(count):
            phone = self.generate_phone_number(i + 1)
            sex = random.choice(['MALE', 'FEMALE'])
            age = self.generate_age()
            full_name = self.generate_name(sex)

            # Birth date from age (in milliseconds)
            # Ensure we don't go before 1970-01-01 to avoid Windows timestamp issues
            birth_year = max(1970, datetime.now().year - age)
            birth_date_obj = datetime(birth_year, random.randint(1, 12), random.randint(1, 28))
            try:
                birth_date = int(birth_date_obj.timestamp() * 1000)
            except (OSError, OverflowError):
                # Fallback to 1970 if timestamp calculation fails
                birth_date = int(datetime(1970, 1, 1).timestamp() * 1000)

            # Pregnancy (only females 15-45)
            is_pregnant = False
            gestational_age = None
            if sex == 'FEMALE' and 15 <= age <= 45:
                is_pregnant = random.random() < 0.05  # 5% pregnancy rate
                if is_pregnant:
                    gestational_age = random.randint(4, 36)  # weeks

            # PhilHealth/PhilSys (70% have at least one)
            philhealth = None
            philsys = None
            if random.random() < 0.7:
                philhealth = f"{random.randint(100000000000, 999999999999)}"
            if random.random() < 0.5:
                philsys = f"{random.randint(1000000000000000, 9999999999999999)}"

            # Messenger opt-in (40%)
            messenger_opt_in = random.random() < 0.4
            messenger_id = f"{random.randint(100000000000000, 999999999999999)}" if messenger_opt_in else None

            # Random barangay
            barangay = random.choice(self.barangays)

            created_at = int((datetime.now() - timedelta(days=random.randint(0, 365))).timestamp() * 1000)

            # Generate ID (PAT + timestamp + random)
            patient_id = f"PAT{int(datetime.now().timestamp() * 1000)}{random.randint(100, 999)}"

            await self.conn.execute("""
                INSERT INTO silver.patients (
                    id, phone_number, full_name,
                    date_of_birth, sex, barangay_id, barangay_name,
                    is_pregnant, gestational_age_weeks,
                    philhealth_number, philsys_number, messenger_opt_in, messenger_user_id,
                    high_risk_flag, maternal_high_risk, created_at, updated_at
                ) VALUES (
                    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13,
                    FALSE, FALSE, $14, $14
                )
            """, patient_id, phone, full_name, birth_date, sex,
                barangay['id'], barangay['name'], is_pregnant, gestational_age,
                philhealth, philsys, messenger_opt_in, messenger_id, created_at)

            self.patient_ids.append({
                'id': patient_id,
                'age': age,
                'sex': sex,
                'is_pregnant': is_pregnant,
                'phone': phone
            })

            if (i + 1) % 100 == 0:
                print(f"  Generated {i + 1}/{count} patients")

        print(f"[OK] {count} patients generated\n")

    async def generate_scans(self, target_count: int = 50000):
        """Generate scan records with realistic distribution"""
        print(f"Generating ~{target_count} scans...")

        # Create BHW IDs (simulate 20 BHWs)
        self.bhw_ids = [f"BHW{i:03d}" for i in range(1, 21)]

        scans_generated = 0
        high_risk_count = 0

        # Each patient gets varying number of scans
        for patient in self.patient_ids:
            # Scans per patient (most have 1-3, some have many more)
            if random.random() < 0.1:  # 10% are frequent scanners
                num_scans = random.randint(40, 80)
            elif random.random() < 0.3:  # 30% have multiple scans
                num_scans = random.randint(5, 15)
            else:  # 60% have few scans
                num_scans = random.randint(1, 4)

            # Stop if we've hit target
            if scans_generated >= target_count:
                break

            for scan_num in range(num_scans):
                # Scan date (spread over last 6 months)
                days_ago = random.randint(1, 180)
                scanned_at = datetime.now() - timedelta(days=days_ago)
                scanned_at_ms = int(scanned_at.timestamp() * 1000)

                # High risk (20% of scans)
                is_high_risk = random.random() < 0.2

                # Generate biomarkers
                biomarkers = self.generate_biomarkers(patient['age'], patient['sex'], is_high_risk)

                # Calculate risk score
                risk_score = biomarkers['diabetes_risk'] if is_high_risk else random.uniform(1, 6)

                # Risk flags if high risk
                risk_flags = []
                if is_high_risk:
                    if biomarkers['systolic_bp'] > 140:
                        risk_flags.append("HYPERTENSION")
                    if biomarkers['blood_glucose'] > 125:
                        risk_flags.append("DIABETES")
                    if biomarkers['spo2'] < 95:
                        risk_flags.append("HYPOXIA")
                    if biomarkers['cvd_risk'] > 7:
                        risk_flags.append("CVD_RISK")

                # Random BHW
                bhw_id = random.choice(self.bhw_ids)

                # Signal quality enum
                signal_quality_value = random.random()
                if signal_quality_value > 0.9:
                    signal_quality = "EXCELLENT"
                elif signal_quality_value > 0.7:
                    signal_quality = "GOOD"
                elif signal_quality_value > 0.5:
                    signal_quality = "FAIR"
                else:
                    signal_quality = "POOR"

                # Scan type
                scan_type = "BIOSENSE_FACE_SCAN"

                # Generate scan ID
                scan_id = f"SCN{int(scanned_at.timestamp() * 1000)}{random.randint(100, 999)}"

                await self.conn.execute("""
                    INSERT INTO silver.scans (
                        id, patient_id, bhw_id, scan_type, scanned_at,
                        signal_quality,
                        systolic_bp, diastolic_bp, heart_rate_bpm, spo2, respiratory_rate,
                        hemoglobin, hba1c, blood_glucose,
                        cholesterol_total, sdnn, stress_index,
                        risk_score, risk_flags, biomarkers_full
                    ) VALUES (
                        $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13,
                        $14, $15, $16, $17, $18, $19, $20
                    )
                """, scan_id, patient['id'], bhw_id, scan_type, scanned_at_ms,
                    signal_quality,
                    biomarkers['systolic_bp'], biomarkers['diastolic_bp'],
                    biomarkers['heart_rate'], biomarkers['spo2'],
                    biomarkers['respiratory_rate'],
                    biomarkers['hemoglobin'], biomarkers['hba1c'],
                    biomarkers['blood_glucose'],
                    biomarkers['cholesterol_total'], biomarkers['hrv_sdnn'],
                    biomarkers['stress_index'],
                    risk_score, json.dumps(risk_flags) if risk_flags else None, json.dumps(biomarkers))

                self.scan_ids.append({
                    'id': scan_id,
                    'patient_id': patient['id'],
                    'bhw_id': bhw_id,
                    'is_high_risk': is_high_risk,
                    'scanned_at': scanned_at,
                    'scanned_at_ms': scanned_at_ms
                })

                scans_generated += 1
                if is_high_risk:
                    high_risk_count += 1

                    # Update patient high risk flag
                    await self.conn.execute("""
                        UPDATE silver.patients
                        SET high_risk_flag = TRUE,
                            high_risk_reasons = $1,
                            maternal_high_risk = $2,
                            last_scan_at = $3,
                            updated_at = $4
                        WHERE id = $5
                    """, json.dumps(risk_flags), patient['is_pregnant'] and is_high_risk,
                        scanned_at_ms, scanned_at_ms, patient['id'])

                if scans_generated % 5000 == 0:
                    print(f"  Generated {scans_generated}/{target_count} scans ({high_risk_count} high-risk)")

        print(f"[OK] {scans_generated} scans generated ({high_risk_count} high-risk)\n")

    async def generate_surveys(self):
        """Generate survey responses for scans"""
        print(f"Generating surveys for scans...")

        survey_count = 0

        # Generate surveys for random subset of scans (70%)
        scans_with_surveys = random.sample(self.scan_ids, int(len(self.scan_ids) * 0.7))

        for scan in scans_with_surveys:
            # NCD survey (all scans)
            ncd_responses = {
                "has_diabetes": random.choice([True, False]),
                "has_hypertension": random.choice([True, False]),
                "family_history": random.choice(["diabetes", "hypertension", "none"]),
                "smoking": random.choice([True, False]),
                "alcohol": random.choice([True, False]),
                "exercise_frequency": random.choice(["daily", "weekly", "rarely", "never"]),
                "diet_quality": random.randint(1, 10)
            }

            survey_id = f"SRV{int(scan['scanned_at'].timestamp() * 1000)}{random.randint(100, 999)}"

            await self.conn.execute("""
                INSERT INTO silver.surveys (
                    id, scan_id, patient_id, survey_type, responses,
                    completed_at
                ) VALUES ($1, $2, $3, $4, $5, $6)
            """, survey_id, scan['id'], scan['patient_id'], 'NCD', ncd_responses,
                scan['scanned_at_ms'])

            survey_count += 1

        print(f"[OK] {survey_count} surveys generated\n")

    async def generate_referrals(self):
        """Generate referrals for high-risk scans"""
        print(f"Generating referrals for high-risk patients...")

        referral_count = 0

        # Get high-risk scans
        high_risk_scans = [s for s in self.scan_ids if s['is_high_risk']]

        # Generate referrals for 80% of high-risk scans
        scans_with_referrals = random.sample(high_risk_scans, int(len(high_risk_scans) * 0.8))

        for scan in scans_with_referrals:
            # Tier: BHW -> BHS (most common)
            tier = "BHW_TO_BHS"

            # Status distribution
            status_choice = random.random()
            if status_choice < 0.4:
                status = "PENDING"
            elif status_choice < 0.7:
                status = "CONFIRMED"
            elif status_choice < 0.9:
                status = "RESOLVED"
            else:
                status = "OVERDUE"

            # Due date (48 hours from scan)
            due_by = scan['scanned_at_ms'] + (48 * 3600 * 1000)

            referral_id = f"REF{int(scan['scanned_at'].timestamp() * 1000)}{random.randint(100, 999)}"

            await self.conn.execute("""
                INSERT INTO silver.referrals (
                    id, patient_id, scan_id, tier, status, priority,
                    reason, referred_by, referred_at, due_by,
                    created_at, updated_at
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $11)
            """, referral_id, scan['patient_id'], scan['id'], tier, status, "HIGH",
                "High-risk vital signs detected", scan['bhw_id'],
                scan['scanned_at_ms'], due_by, scan['scanned_at_ms'])

            referral_count += 1

        print(f"[OK] {referral_count} referrals generated\n")

    async def generate_incentives(self):
        """Generate BHW incentive records"""
        print(f"Generating incentive records...")

        incentive_count = 0

        # Generate incentives for all scans (₱3 per scan)
        for scan in self.scan_ids:
            # 95% earned, 5% rejected
            status = "EARNED" if random.random() < 0.95 else "REJECTED"
            amount = 3.00 if status == "EARNED" else 0.00

            incentive_id = f"INC{int(scan['scanned_at'].timestamp() * 1000)}{random.randint(100, 999)}"

            await self.conn.execute("""
                INSERT INTO silver.bhw_incentives (
                    id, bhw_id, scan_id, patient_id, amount, status,
                    earned_at, created_at, updated_at
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $8)
            """, incentive_id, scan['bhw_id'], scan['id'], scan['patient_id'], amount,
                status, scan['scanned_at_ms'], scan['scanned_at_ms'])

            incentive_count += 1

        print(f"[OK] {incentive_count} incentive records generated\n")

    async def print_summary(self):
        """Print generation summary"""
        print("=" * 60)
        print("Dummy Data Generation Summary")
        print("=" * 60)

        # Counts
        patient_count = await self.conn.fetchval("SELECT COUNT(*) FROM silver.patients")
        scan_count = await self.conn.fetchval("SELECT COUNT(*) FROM silver.scans")
        survey_count = await self.conn.fetchval("SELECT COUNT(*) FROM silver.surveys")
        referral_count = await self.conn.fetchval("SELECT COUNT(*) FROM silver.referrals")
        incentive_count = await self.conn.fetchval("SELECT COUNT(*) FROM silver.bhw_incentives")
        high_risk_count = await self.conn.fetchval("SELECT COUNT(*) FROM silver.patients WHERE high_risk_flag = TRUE")

        print(f"\nRecords Created:")
        print(f"  Patients:    {patient_count:>8}")
        print(f"  Scans:       {scan_count:>8}")
        print(f"  Surveys:     {survey_count:>8}")
        print(f"  Referrals:   {referral_count:>8}")
        print(f"  Incentives:  {incentive_count:>8}")
        print(f"\nHigh-Risk Patients: {high_risk_count}")

        # Total incentives earned
        total_earned = await self.conn.fetchval(
            "SELECT SUM(amount) FROM silver.bhw_incentives WHERE status = 'EARNED'"
        )
        print(f"Total Incentives: ₱{total_earned:,.2f}")

        print("\n" + "=" * 60)
        print("[OK] Dummy data generation complete!")
        print("=" * 60)


async def main():
    """Main entry point"""
    print("=" * 60)
    print("PHI Platform - Dummy Data Generator")
    print("=" * 60)
    print()

    # Connect to database
    print(f"Connecting to: {config.DATABASE_HOST}:{config.DATABASE_PORT}/{config.DATABASE_NAME}")

    try:
        conn_params = {
            "host": config.DATABASE_HOST,
            "port": config.DATABASE_PORT,
            "database": config.DATABASE_NAME,
            "user": config.DATABASE_USER,
            "password": config.DATABASE_PASSWORD
        }

        # Add SSL for Azure
        if "azure.com" in config.DATABASE_HOST or "postgres.database" in config.DATABASE_HOST:
            conn_params["ssl"] = "require"

        conn = await asyncpg.connect(**conn_params)
        print("[OK] Connected\n")

        # Create generator
        generator = DummyDataGenerator(conn)

        # Load reference data
        await generator.load_reference_data()

        # Generate data
        await generator.generate_patients(count=1000)
        await generator.generate_scans(target_count=50000)
        await generator.generate_surveys()
        await generator.generate_referrals()
        await generator.generate_incentives()

        # Print summary
        await generator.print_summary()

        await conn.close()
        return True

    except Exception as e:
        print(f"[ERROR] {e}")
        import traceback
        traceback.print_exc()
        return False


if __name__ == "__main__":
    success = asyncio.run(main())
    sys.exit(0 if success else 1)
