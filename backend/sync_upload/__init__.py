"""
Sync Upload Endpoint
Receives batched data from Android devices and processes through Bronze → Silver pipeline
"""
import azure.functions as func
import asyncpg
import json
import logging
from datetime import datetime
from typing import Dict, List, Any

from shared.db import get_db_pool
from shared.auth import require_auth

logger = logging.getLogger(__name__)


async def process_patients(conn: asyncpg.Connection, patients: List[Dict], bhw_id: str, device_id: str) -> Dict:
    """
    Process patient records through Bronze → Silver pipeline
    Returns: {"accepted": count, "rejected": count, "reasons": [...]}
    """
    accepted = 0
    rejected = 0
    reasons = []

    for patient_data in patients:
        try:
            # Generate Bronze ID
            bronze_id = f"BRZ_PAT_{int(datetime.utcnow().timestamp() * 1000)}"

            # Insert to Bronze (raw, immutable)
            await conn.execute("""
                INSERT INTO bronze.patients (
                    id, phone_number, full_name, date_of_birth, sex,
                    barangay_id, barangay_name, philhealth_number, philsys_number,
                    is_pregnant, gestational_age_weeks,
                    messenger_opt_in, messenger_user_id,
                    bhw_id, device_id, ingested_at
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16)
            """, bronze_id, patient_data['phone_number'], patient_data['full_name'],
                patient_data['date_of_birth'], patient_data['sex'],
                patient_data['barangay_id'], patient_data['barangay_name'],
                patient_data.get('philhealth_number'), patient_data.get('philsys_number'),
                patient_data.get('is_pregnant', False), patient_data.get('gestational_age_weeks'),
                patient_data.get('messenger_opt_in', False), patient_data.get('messenger_user_id'),
                bhw_id, device_id, int(datetime.utcnow().timestamp() * 1000))

            # Check if patient exists in Silver (dedup by phone)
            existing = await conn.fetchrow("""
                SELECT id FROM silver.patients WHERE phone_number = $1
            """, patient_data['phone_number'])

            if existing:
                # Patient exists - update if needed
                await conn.execute("""
                    UPDATE silver.patients
                    SET full_name = $1,
                        date_of_birth = $2,
                        barangay_id = $3,
                        barangay_name = $4,
                        philhealth_number = COALESCE($5, philhealth_number),
                        philsys_number = COALESCE($6, philsys_number),
                        is_pregnant = $7,
                        gestational_age_weeks = $8,
                        messenger_opt_in = $9,
                        messenger_user_id = COALESCE($10, messenger_user_id),
                        updated_at = $11
                    WHERE phone_number = $12
                """, patient_data['full_name'], patient_data['date_of_birth'],
                    patient_data['barangay_id'], patient_data['barangay_name'],
                    patient_data.get('philhealth_number'), patient_data.get('philsys_number'),
                    patient_data.get('is_pregnant', False), patient_data.get('gestational_age_weeks'),
                    patient_data.get('messenger_opt_in', False), patient_data.get('messenger_user_id'),
                    int(datetime.utcnow().timestamp() * 1000), patient_data['phone_number'])

                logger.info(f"Updated existing patient: {existing['id']}")
            else:
                # New patient - insert to Silver
                silver_id = patient_data.get('id', f"PAT_{int(datetime.utcnow().timestamp() * 1000)}")
                await conn.execute("""
                    INSERT INTO silver.patients (
                        id, phone_number, full_name, date_of_birth, sex,
                        barangay_id, barangay_name, philhealth_number, philsys_number,
                        is_pregnant, gestational_age_weeks,
                        messenger_opt_in, messenger_user_id,
                        high_risk_flag, maternal_high_risk,
                        created_at, updated_at, bronze_source_id
                    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13,
                              FALSE, FALSE, $14, $14, $15)
                """, silver_id, patient_data['phone_number'], patient_data['full_name'],
                    patient_data['date_of_birth'], patient_data['sex'],
                    patient_data['barangay_id'], patient_data['barangay_name'],
                    patient_data.get('philhealth_number'), patient_data.get('philsys_number'),
                    patient_data.get('is_pregnant', False), patient_data.get('gestational_age_weeks'),
                    patient_data.get('messenger_opt_in', False), patient_data.get('messenger_user_id'),
                    int(datetime.utcnow().timestamp() * 1000), bronze_id)

                logger.info(f"Created new patient: {silver_id}")

            accepted += 1

        except Exception as e:
            logger.error(f"Failed to process patient {patient_data.get('phone_number')}: {e}")
            rejected += 1
            reasons.append({
                "phone": patient_data.get('phone_number'),
                "reason": str(e)
            })

    return {"accepted": accepted, "rejected": rejected, "reasons": reasons}


async def process_scans(conn: asyncpg.Connection, scans: List[Dict], bhw_id: str, device_id: str) -> Dict:
    """
    Process scan records through Bronze → Silver pipeline
    Validates scans and checks rescan eligibility
    """
    accepted = 0
    rejected = 0
    reasons = []

    for scan_data in scans:
        try:
            # Generate Bronze ID
            bronze_id = f"BRZ_SCN_{int(datetime.utcnow().timestamp() * 1000)}"

            # Insert to Bronze
            await conn.execute("""
                INSERT INTO bronze.scans (
                    id, patient_id, bhw_id, device_id,
                    scan_type, scanned_at, biomarkers,
                    signal_quality, scan_duration_seconds,
                    ingested_at
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
            """, bronze_id, scan_data['patient_id'], bhw_id, device_id,
                scan_data['scan_type'], scan_data['scanned_at'],
                json.dumps(scan_data['biomarkers']),
                scan_data.get('signal_quality', 'GOOD'),
                scan_data.get('scan_duration_seconds', 0),
                int(datetime.utcnow().timestamp() * 1000))

            # Validate signal quality
            signal_quality = scan_data.get('signal_quality', 'GOOD')
            if signal_quality not in ['EXCELLENT', 'GOOD', 'FAIR']:
                rejected += 1
                reasons.append({
                    "scan_id": scan_data.get('id'),
                    "reason": f"Poor signal quality: {signal_quality}"
                })
                continue

            # Check if patient exists in Silver
            patient = await conn.fetchrow("""
                SELECT id, phone_number, last_scan_at, high_risk_flag,
                       is_pregnant, maternal_high_risk
                FROM silver.patients
                WHERE id = $1
            """, scan_data['patient_id'])

            if not patient:
                rejected += 1
                reasons.append({
                    "scan_id": scan_data.get('id'),
                    "reason": "Patient not found in Silver"
                })
                continue

            # Check rescan eligibility (simplified - full logic in separate use case)
            if patient['last_scan_at']:
                hours_since_last = (scan_data['scanned_at'] - patient['last_scan_at']) / (1000 * 3600)

                if patient['high_risk_flag'] and hours_since_last < (24 * 30):  # 30 days
                    rejected += 1
                    reasons.append({
                        "scan_id": scan_data.get('id'),
                        "reason": f"High-risk rescan interval not met: {hours_since_last:.1f}h < 720h"
                    })
                    continue
                elif not patient['high_risk_flag'] and hours_since_last < (24 * 365):  # 1 year
                    rejected += 1
                    reasons.append({
                        "scan_id": scan_data.get('id'),
                        "reason": f"Annual rescan interval not met: {hours_since_last:.1f}h < 8760h"
                    })
                    continue

            # Promote to Silver
            biomarkers = scan_data['biomarkers']
            silver_id = scan_data.get('id', f"SCN_{int(datetime.utcnow().timestamp() * 1000)}")

            await conn.execute("""
                INSERT INTO silver.scans (
                    id, patient_id, bhw_id, scan_type, scanned_at,
                    signal_quality,
                    systolic_bp, diastolic_bp, heart_rate_bpm, spo2, respiratory_rate,
                    hemoglobin, hba1c, blood_glucose,
                    cholesterol_total, sdnn, stress_index,
                    biomarkers_full, risk_score, risk_flags,
                    validated, bronze_source_id
                ) VALUES (
                    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14,
                    $15, $16, $17, $18, $19, $20, TRUE, $21
                )
            """, silver_id, scan_data['patient_id'], bhw_id,
                scan_data['scan_type'], scan_data['scanned_at'],
                signal_quality,
                biomarkers.get('systolic_bp'), biomarkers.get('diastolic_bp'),
                biomarkers.get('heart_rate'), biomarkers.get('spo2'),
                biomarkers.get('respiratory_rate'),
                biomarkers.get('hemoglobin'), biomarkers.get('hba1c'),
                biomarkers.get('blood_glucose'),
                biomarkers.get('cholesterol_total'), biomarkers.get('hrv_sdnn'),
                biomarkers.get('stress_index'),
                json.dumps(biomarkers),
                scan_data.get('risk_score', 0),
                json.dumps(scan_data.get('risk_flags', [])),
                bronze_id)

            # Update patient last_scan_at and total_scans
            await conn.execute("""
                UPDATE silver.patients
                SET last_scan_at = $1,
                    total_scans = total_scans + 1,
                    updated_at = $2
                WHERE id = $3
            """, scan_data['scanned_at'], int(datetime.utcnow().timestamp() * 1000),
                scan_data['patient_id'])

            accepted += 1
            logger.info(f"Validated and promoted scan: {silver_id}")

        except Exception as e:
            logger.error(f"Failed to process scan: {e}")
            rejected += 1
            reasons.append({
                "scan_id": scan_data.get('id'),
                "reason": str(e)
            })

    return {"accepted": accepted, "rejected": rejected, "reasons": reasons}


async def process_surveys(conn: asyncpg.Connection, surveys: List[Dict]) -> Dict:
    """Process survey records"""
    accepted = 0
    rejected = 0
    reasons = []

    for survey_data in surveys:
        try:
            silver_id = survey_data.get('id', f"SRV_{int(datetime.utcnow().timestamp() * 1000)}")

            await conn.execute("""
                INSERT INTO silver.surveys (
                    id, scan_id, patient_id, survey_type,
                    responses, completed_at
                ) VALUES ($1, $2, $3, $4, $5, $6)
                ON CONFLICT (id) DO NOTHING
            """, silver_id, survey_data['scan_id'], survey_data['patient_id'],
                survey_data['survey_type'], json.dumps(survey_data['responses']),
                survey_data['completed_at'])

            accepted += 1

        except Exception as e:
            logger.error(f"Failed to process survey: {e}")
            rejected += 1
            reasons.append({"survey_id": survey_data.get('id'), "reason": str(e)})

    return {"accepted": accepted, "rejected": rejected, "reasons": reasons}


async def process_referrals(conn: asyncpg.Connection, referrals: List[Dict]) -> Dict:
    """Process referral records"""
    accepted = 0
    rejected = 0
    reasons = []

    for referral_data in referrals:
        try:
            silver_id = referral_data.get('id', f"REF_{int(datetime.utcnow().timestamp() * 1000)}")

            await conn.execute("""
                INSERT INTO silver.referrals (
                    id, patient_id, scan_id, tier, status, priority,
                    reason, referred_by, referred_at, due_by,
                    created_at, updated_at
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $11)
                ON CONFLICT (id) DO UPDATE SET
                    status = EXCLUDED.status,
                    updated_at = EXCLUDED.updated_at
            """, silver_id, referral_data['patient_id'], referral_data['scan_id'],
                referral_data['tier'], referral_data['status'], referral_data['priority'],
                referral_data['reason'], referral_data['referred_by'],
                referral_data['referred_at'], referral_data['due_by'],
                int(datetime.utcnow().timestamp() * 1000))

            accepted += 1

        except Exception as e:
            logger.error(f"Failed to process referral: {e}")
            rejected += 1
            reasons.append({"referral_id": referral_data.get('id'), "reason": str(e)})

    return {"accepted": accepted, "rejected": rejected, "reasons": reasons}


async def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    Sync Upload Endpoint
    POST /sync_upload
    """
    logger.info("Sync upload request received")

    # Require authentication
    is_authorized, error_msg, user = require_auth(req)
    if not is_authorized:
        return func.HttpResponse(
            json.dumps({"success": False, "error": error_msg}),
            mimetype="application/json",
            status_code=401
        )

    bhw_id = user.get('user_id')

    try:
        # Parse request body
        body = req.get_json()
        device_id = body.get('device_id')
        sync_timestamp = body.get('sync_timestamp', int(datetime.utcnow().timestamp() * 1000))

        if not device_id:
            return func.HttpResponse(
                json.dumps({"success": False, "error": "device_id required"}),
                mimetype="application/json",
                status_code=400
            )

        # Get data batches
        patients = body.get('patients', [])
        scans = body.get('scans', [])
        surveys = body.get('surveys', [])
        referrals = body.get('referrals', [])

        logger.info(f"Sync batch: {len(patients)} patients, {len(scans)} scans, "
                   f"{len(surveys)} surveys, {len(referrals)} referrals")

        # Process through pipeline
        pool = await get_db_pool()
        async with pool.acquire() as conn:
            async with conn.transaction():
                # Process each data type
                patient_result = await process_patients(conn, patients, bhw_id, device_id)
                scan_result = await process_scans(conn, scans, bhw_id, device_id)
                survey_result = await process_surveys(conn, surveys)
                referral_result = await process_referrals(conn, referrals)

                # Record sync receipt
                await conn.execute("""
                    INSERT INTO bronze.sync_receipts (
                        id, device_id, bhw_id, sync_timestamp,
                        patients_accepted, patients_rejected,
                        scans_accepted, scans_rejected,
                        surveys_accepted, surveys_rejected,
                        referrals_accepted, referrals_rejected,
                        ingested_at
                    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
                """, f"SYNC_{int(datetime.utcnow().timestamp() * 1000)}",
                    device_id, bhw_id, sync_timestamp,
                    patient_result['accepted'], patient_result['rejected'],
                    scan_result['accepted'], scan_result['rejected'],
                    survey_result['accepted'], survey_result['rejected'],
                    referral_result['accepted'], referral_result['rejected'],
                    int(datetime.utcnow().timestamp() * 1000))

        # Build response
        response = {
            "success": True,
            "sync_timestamp": int(datetime.utcnow().timestamp() * 1000),
            "results": {
                "patients": patient_result,
                "scans": scan_result,
                "surveys": survey_result,
                "referrals": referral_result
            },
            "summary": {
                "total_accepted": (patient_result['accepted'] + scan_result['accepted'] +
                                 survey_result['accepted'] + referral_result['accepted']),
                "total_rejected": (patient_result['rejected'] + scan_result['rejected'] +
                                 survey_result['rejected'] + referral_result['rejected'])
            }
        }

        logger.info(f"Sync complete: {response['summary']['total_accepted']} accepted, "
                   f"{response['summary']['total_rejected']} rejected")

        return func.HttpResponse(
            json.dumps(response),
            mimetype="application/json",
            status_code=200
        )

    except ValueError as e:
        logger.error(f"Invalid request: {e}")
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Invalid JSON body"}),
            mimetype="application/json",
            status_code=400
        )

    except Exception as e:
        logger.error(f"Sync upload failed: {e}")
        return func.HttpResponse(
            json.dumps({"success": False, "error": "Internal server error"}),
            mimetype="application/json",
            status_code=500
        )
