package com.globaloutcomes.phi.domain.model

/**
 * Patient Domain Model - Clean Architecture
 * Pure business logic model without database dependencies
 */
data class Patient(
    val id: String,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Long,
    val sex: String,
    val barangayCode: String,
    val municipalityCode: String,
    val philhealthNumber: String? = null,
    val philsysId: String? = null,
    val pregnant: Boolean = false,
    val gestationalAgeWeeks: Int? = null,
    val highRiskFlag: Boolean = false,
    val highRiskReasons: List<String>? = null,
    val maternalHighRisk: Boolean = false,
    val lastScanDate: Long? = null,
    val totalScans: Int = 0,
    val messengerOptIn: Boolean = false,
    val messengerContactMethod: String? = null,
    val messengerContactValue: String? = null,
    val messengerPsid: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus
) {
    val fullName: String
        get() = "$firstName $lastName"

    val age: Int
        get() {
            val now = System.currentTimeMillis()
            val ageInMillis = now - dateOfBirth
            return (ageInMillis / (365.25 * 24 * 60 * 60 * 1000)).toInt()
        }
}

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}
