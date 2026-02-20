package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Scan entity storing all 34 biomarkers from BiosenseSignal SDK
 * Represents a single health screening scan
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
    ],
    indices = [
        Index(value = ["patientId"]),
        Index(value = ["scannedAt"]),
        Index(value = ["riskScore"]),
        Index(value = ["syncStatus"])
    ]
)
data class ScanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val patientId: String,
    val scannedAt: Long = System.currentTimeMillis(),
    val scanDurationSeconds: Int,

    // Cardiovascular (6 metrics)
    val systolicBp: Float? = null,
    val diastolicBp: Float? = null,
    val heartRate: Float? = null,
    val heartRateVariability: Float? = null,
    val cardiacOutput: Float? = null,
    val strokeVolume: Float? = null,

    // Respiratory (3 metrics)
    val respiratoryRate: Float? = null,
    val spo2: Float? = null,
    val perfusionIndex: Float? = null,

    // Metabolic (4 metrics)
    val bloodGlucose: Float? = null,
    val hba1c: Float? = null,
    val cholesterol: Float? = null,
    val triglycerides: Float? = null,

    // Hematology (2 metrics)
    val hemoglobin: Float? = null,
    val hematocrit: Float? = null,

    // Renal (2 metrics)
    val bun: Float? = null, // Blood Urea Nitrogen
    val creatinine: Float? = null,

    // Vascular (4 metrics)
    val arterialStiffness: Float? = null,
    val vascularAge: Float? = null,
    val peripheralResistance: Float? = null,
    val meanArterialPressure: Float? = null,

    // Autonomic (3 metrics)
    val sympatheticTone: Float? = null,
    val parasympatheticTone: Float? = null,
    val stressIndex: Float? = null,

    // Body Composition (3 metrics)
    val bmi: Float? = null,
    val bodyFatPercentage: Float? = null,
    val visceralFatLevel: Float? = null,

    // Advanced Cardiac (4 metrics)
    val leftVentricularEjectionFraction: Float? = null,
    val systolicTimeIntervals: Float? = null,
    val diastolicFunction: Float? = null,
    val qrsDuration: Float? = null, // milliseconds

    // Pulmonary (3 metrics)
    val lungCapacity: Float? = null,
    val respiratoryEfficiency: Float? = null,
    val oxygenSaturationVariability: Float? = null,

    // Risk Assessment
    val riskScore: Float, // 0-10 scale
    val riskLevel: String, // NORMAL, ELEVATED, HIGH
    val highRiskFlags: String? = null, // JSON array of threshold violations

    // Quality Metrics
    val signalQuality: String, // EXCELLENT, GOOD, FAIR, POOR
    val qualityScore: Float, // 0-100
    val rejectionReason: String? = null,

    // Full Biomarkers JSON (for future SDK changes)
    val biomarkersFull: String, // JSON object with all raw data

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Sync Status
    val syncStatus: String = "PENDING", // PENDING, SYNCED, FAILED
    val syncedAt: Long? = null,
    val serverScanId: String? = null
)
