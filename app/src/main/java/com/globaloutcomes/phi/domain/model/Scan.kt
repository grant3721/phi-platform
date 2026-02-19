package com.globaloutcomes.phi.domain.model

/**
 * Scan Domain Model - BiosenseSignal SDK Vital Signs
 * Contains all 34 vital signs from SDK output
 */
data class Scan(
    val id: String,
    val patientId: String,
    val bhwId: String,
    val scanType: String,
    val validated: Boolean,
    val rejectionReason: String? = null,
    val biomarkers: Biomarkers,
    val riskFlags: List<String>? = null,
    val overallRiskScore: Double? = null,
    val scanDurationMs: Int? = null,
    val signalQuality: Double? = null,
    val gpsLat: Double? = null,
    val gpsLon: Double? = null,
    val deviceId: String? = null,
    val appVersion: String? = null,
    val scannedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus
) {
    val isHighRisk: Boolean
        get() = !riskFlags.isNullOrEmpty()
}

/**
 * Biomarkers - All 34 BiosenseSignal SDK vital signs
 */
data class Biomarkers(
    // Cardiovascular (7)
    val pulseRate: Double? = null,
    val bpSystolic: Double? = null,
    val bpDiastolic: Double? = null,
    val meanArterialPressure: Double? = null,
    val pulsePressure: Double? = null,
    val cardiacWorkload: Double? = null,
    val heartAge: Double? = null,

    // Respiratory (2)
    val respirationRate: Double? = null,
    val oxygenSaturation: Double? = null,

    // Bloodless Blood Tests (2)
    val hemoglobin: Double? = null,
    val hemoglobinA1c: Double? = null,

    // Risk Indicators (6)
    val ascvdRisk: Double? = null,
    val ascvdRiskLevel: String? = null,
    val highBloodPressureRisk: Double? = null,
    val highFastingGlucoseRisk: Double? = null,
    val highHemoglobinA1cRisk: Double? = null,
    val highTotalCholesterolRisk: Double? = null,
    val lowHemoglobinRisk: Double? = null,

    // Heart Rate Variability (8)
    val meanRri: Double? = null,
    val rri: Double? = null,
    val sdnn: Double? = null,
    val rmssd: Double? = null,
    val sd1: Double? = null,
    val sd2: Double? = null,
    val prq: Double? = null,
    val lfhf: Double? = null,

    // Autonomic Nervous System (4)
    val pnsIndex: Double? = null,
    val pnsZone: String? = null,
    val snsIndex: Double? = null,
    val snsZone: String? = null,

    // Stress (3)
    val stressLevel: String? = null,
    val stressIndex: Double? = null,
    val normalizedStressIndex: Double? = null,

    // Wellness (2)
    val wellnessIndex: Double? = null,
    val wellnessLevel: String? = null
)
