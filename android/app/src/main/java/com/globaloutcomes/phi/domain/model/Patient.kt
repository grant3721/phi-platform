package com.globaloutcomes.phi.domain.model

import java.util.UUID

/**
 * Domain model for Patient (business logic layer)
 * Separate from PatientEntity to maintain clean architecture
 */
data class Patient(
    val id: String = UUID.randomUUID().toString(),
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val birthdate: Long,
    val sex: Sex,
    val barangayId: String,
    val philHealthNumber: String? = null,
    val philSysNumber: String? = null,
    val isPregnant: Boolean = false,
    val gestationalAgeWeeks: Int? = null,
    val lastMenstrualPeriod: Long? = null,
    val messengerOptIn: Boolean = false,
    val messengerUserId: String? = null,
    val messengerName: String? = null,
    val highRiskFlag: Boolean = false,
    val highRiskReasons: List<String> = emptyList(),
    val maternalHighRisk: Boolean = false,
    val lastRiskAssessment: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val syncedAt: Long? = null,
    val serverPatientId: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName"

    val age: Int
        get() {
            val now = System.currentTimeMillis()
            val ageMillis = now - birthdate
            return (ageMillis / (365.25 * 24 * 60 * 60 * 1000)).toInt()
        }

    val isHighRisk: Boolean
        get() = highRiskFlag || maternalHighRisk

    fun needsSync(): Boolean = syncStatus == SyncStatus.PENDING || syncStatus == SyncStatus.FAILED
}

enum class Sex {
    MALE, FEMALE, OTHER
}

enum class SyncStatus {
    PENDING, SYNCED, FAILED
}
