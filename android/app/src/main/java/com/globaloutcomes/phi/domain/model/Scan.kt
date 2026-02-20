package com.globaloutcomes.phi.domain.model

import java.util.UUID

/**
 * Domain model for Scan (34 biomarkers from BiosenseSignal)
 */
data class Scan(
    val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val scannedAt: Long = System.currentTimeMillis(),
    val scanDurationSeconds: Int,

    // Cardiovascular
    val systolicBp: Float? = null,
    val diastolicBp: Float? = null,
    val heartRate: Float? = null,
    val heartRateVariability: Float? = null,
    val cardiacOutput: Float? = null,
    val strokeVolume: Float? = null,

    // Respiratory
    val respiratoryRate: Float? = null,
    val spo2: Float? = null,
    val perfusionIndex: Float? = null,

    // Metabolic
    val bloodGlucose: Float? = null,
    val hba1c: Float? = null,
    val cholesterol: Float? = null,
    val triglycerides: Float? = null,

    // Hematology
    val hemoglobin: Float? = null,
    val hematocrit: Float? = null,

    // Renal
    val bun: Float? = null,
    val creatinine: Float? = null,

    // Vascular
    val arterialStiffness: Float? = null,
    val vascularAge: Float? = null,
    val peripheralResistance: Float? = null,
    val meanArterialPressure: Float? = null,

    // Autonomic
    val sympatheticTone: Float? = null,
    val parasympatheticTone: Float? = null,
    val stressIndex: Float? = null,

    // Body Composition
    val bmi: Float? = null,
    val bodyFatPercentage: Float? = null,
    val visceralFatLevel: Float? = null,

    // Advanced Cardiac
    val leftVentricularEjectionFraction: Float? = null,
    val systolicTimeIntervals: Float? = null,
    val diastolicFunction: Float? = null,
    val qrsDuration: Float? = null,

    // Pulmonary
    val lungCapacity: Float? = null,
    val respiratoryEfficiency: Float? = null,
    val oxygenSaturationVariability: Float? = null,

    // Risk Assessment
    val riskScore: Float,
    val riskLevel: RiskLevel,
    val highRiskFlags: List<String> = emptyList(),

    // Quality
    val signalQuality: SignalQuality,
    val qualityScore: Float,
    val rejectionReason: String? = null,

    val biomarkersFull: Map<String, Any>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val syncedAt: Long? = null,
    val serverScanId: String? = null
) {
    val isHighRisk: Boolean
        get() = riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.ELEVATED

    val isValidQuality: Boolean
        get() = signalQuality in listOf(SignalQuality.EXCELLENT, SignalQuality.GOOD, SignalQuality.FAIR)

    fun needsSync(): Boolean = syncStatus == SyncStatus.PENDING || syncStatus == SyncStatus.FAILED
}

enum class RiskLevel {
    NORMAL, ELEVATED, HIGH
}

enum class SignalQuality {
    EXCELLENT, GOOD, FAIR, POOR
}
