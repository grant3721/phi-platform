package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Scan Entity - BiosenseSignal SDK v5.11 Vital Signs
 *
 * CRITICAL: Every field maps 1:1 to BiosenseSignal SDK output.
 * No invented fields. No estimates. If SDK doesn't output it, we don't store it.
 *
 * Total: 34 distinct SDK vital signs + metadata
 */
@Entity(
    tableName = "scans",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ScanEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val bhwId: String,
    val scanType: String,                   // "baseline" | "maternal_rescan" | "high_risk_rescan"
    val validated: Boolean,
    val rejectionReason: String? = null,

    // ============================================================
    // BIOSENSESIGNAL SDK v5.11 — ALL 34 VITAL SIGNS
    // Source: https://developer.biosensesignal.com/android/5.11/
    // Every field below is a direct SDK output. Nothing invented.
    // ============================================================

    // --- CARDIOVASCULAR (7) ---
    val pulseRate: Double? = null,              // bpm — SDK: Pulse Rate
    val bpSystolic: Double? = null,             // mmHg — SDK: Blood Pressure (systolic)
    val bpDiastolic: Double? = null,            // mmHg — SDK: Blood Pressure (diastolic)
    val meanArterialPressure: Double? = null,   // mmHg — SDK: Mean Arterial Pressure (NEW in v5.11)
    val pulsePressure: Double? = null,          // mmHg — SDK: Pulse Pressure (NEW in v5.11)
    val cardiacWorkload: Double? = null,        // index — SDK: Cardiac Workload (NEW in v5.11)
    val heartAge: Double? = null,               // years — SDK: Heart Age

    // --- RESPIRATORY (2) ---
    val respirationRate: Double? = null,        // breaths/min — SDK: Respiration Rate
    val oxygenSaturation: Double? = null,       // % — SDK: Oxygen Saturation (SpO2)

    // --- BLOODLESS BLOOD TESTS (2) ---
    val hemoglobin: Double? = null,             // g/dL — SDK: Hemoglobin
    val hemoglobinA1c: Double? = null,          // % — SDK: Hemoglobin A1c

    // --- RISK INDICATORS (6) ---
    val ascvdRisk: Double? = null,              // score — SDK: ASCVD Risk
    val ascvdRiskLevel: String? = null,         // category — SDK: ASCVD Risk Level (NEW in v5.11)
    val highBloodPressureRisk: Double? = null,  // score — SDK: High Blood Pressure Risk
    val highFastingGlucoseRisk: Double? = null, // score — SDK: High Fasting Glucose Risk
    val highHemoglobinA1cRisk: Double? = null,  // score — SDK: High Hemoglobin A1c Risk
    val highTotalCholesterolRisk: Double? = null,// score — SDK: High Total Cholesterol Risk
    val lowHemoglobinRisk: Double? = null,      // score — SDK: Low Hemoglobin Risk

    // --- HEART RATE VARIABILITY (8) ---
    val meanRri: Double? = null,                // ms — SDK: Mean RRi
    val rri: Double? = null,                    // ms — SDK: RRi (individual interval)
    val sdnn: Double? = null,                   // ms — SDK: SDNN
    val rmssd: Double? = null,                  // ms — SDK: RMSSD
    val sd1: Double? = null,                    // ms — SDK: SD1
    val sd2: Double? = null,                    // ms — SDK: SD2
    val prq: Double? = null,                    // ratio — SDK: PRQ
    val lfhf: Double? = null,                   // ratio — SDK: LF/HF

    // --- AUTONOMIC NERVOUS SYSTEM (4) ---
    val pnsIndex: Double? = null,               // index — SDK: PNS Index
    val pnsZone: String? = null,                // category — SDK: PNS Zone
    val snsIndex: Double? = null,               // index — SDK: SNS Index
    val snsZone: String? = null,                // category — SDK: SNS Zone

    // --- STRESS (3) ---
    val stressLevel: String? = null,            // category — SDK: Stress Level
    val stressIndex: Double? = null,            // index — SDK: Stress Index
    val normalizedStressIndex: Double? = null,  // index — SDK: Normalized Stress Index

    // --- WELLNESS (2) ---
    val wellnessIndex: Double? = null,          // 0-100 — SDK: Wellness Index
    val wellnessLevel: String? = null,          // category — SDK: Wellness Level

    // ============================================================
    // END SDK VITAL SIGNS — Total: 34 distinct outputs
    // ============================================================

    // --- SCAN METADATA (not from SDK) ---
    val riskFlags: String? = null,              // JSON array: ["hypertension_crisis","hypoxemia"]
    val overallRiskScore: Double? = null,       // Computed by our app from SDK outputs
    val scanDurationMs: Int? = null,
    val signalQuality: Double? = null,          // SDK reports this
    val gpsLat: Double? = null,
    val gpsLon: Double? = null,
    val deviceId: String? = null,
    val appVersion: String? = null,
    val scannedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
