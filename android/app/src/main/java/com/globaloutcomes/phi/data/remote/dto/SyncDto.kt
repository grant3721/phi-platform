package com.globaloutcomes.phi.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTOs for sync operations
 */

@Serializable
data class SyncUploadRequest(
    val patients: List<PatientDto> = emptyList(),
    val scans: List<ScanDto> = emptyList(),
    val surveys: List<SurveyDto> = emptyList(),
    val referrals: List<ReferralDto> = emptyList()
)

@Serializable
data class SyncUploadResponse(
    val acceptedPatients: Int,
    val acceptedScans: Int,
    val acceptedSurveys: Int,
    val acceptedReferrals: Int,
    val rejectedPatients: List<String> = emptyList(),
    val rejectedScans: List<String> = emptyList(),
    val message: String
)

@Serializable
data class SyncDownloadRequest(
    val lastSyncTimestamp: Long
)

@Serializable
data class SyncDownloadResponse(
    val configThresholds: Map<String, Float>? = null,
    val patientUpdates: List<PatientUpdateDto> = emptyList(),
    val referralUpdates: List<ReferralUpdateDto> = emptyList()
)

@Serializable
data class PatientDto(
    val id: String,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val birthdate: Long,
    val sex: String,
    val barangayId: String,
    val philHealthNumber: String? = null,
    val philSysNumber: String? = null,
    val isPregnant: Boolean = false,
    val gestationalAgeWeeks: Int? = null,
    val messengerOptIn: Boolean = false
)

@Serializable
data class ScanDto(
    val id: String,
    val patientId: String,
    val scannedAt: Long,
    val scanDurationSeconds: Int,
    val systolicBp: Float? = null,
    val diastolicBp: Float? = null,
    val heartRate: Float? = null,
    val spo2: Float? = null,
    val bloodGlucose: Float? = null,
    val riskLevel: String,
    val riskScore: Float,
    val highRiskFlags: List<String> = emptyList(),
    val biomarkersFull: Map<String, Any>
)

@Serializable
data class SurveyDto(
    val id: String,
    val patientId: String,
    val scanId: String? = null,
    val surveyType: String,
    val completedAt: Long,
    val responses: Map<String, String> // Simplified to String values
)

@Serializable
data class ReferralDto(
    val id: String,
    val patientId: String,
    val scanId: String,
    val tier: String,
    val status: String,
    val referredAt: Long,
    val dueBy: Long,
    val riskLevel: String,
    val riskFlags: List<String>
)

@Serializable
data class PatientUpdateDto(
    val patientId: String,
    val highRiskFlag: Boolean,
    val highRiskReasons: List<String>
)

@Serializable
data class ReferralUpdateDto(
    val referralId: String,
    val status: String,
    val resolvedAt: Long? = null
)
