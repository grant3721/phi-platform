package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Patient entity for local Room database
 * Stores patient demographic and health status information
 */
@Entity(
    tableName = "patients",
    indices = [
        Index(value = ["phoneNumber"], unique = true),
        Index(value = ["barangayId"]),
        Index(value = ["highRiskFlag"]),
        Index(value = ["syncStatus"])
    ]
)
data class PatientEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // Demographics
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val birthdate: Long, // timestamp in milliseconds
    val sex: String, // MALE, FEMALE, OTHER
    val barangayId: String,

    // Optional Government IDs
    val philHealthNumber: String? = null,
    val philSysNumber: String? = null,

    // Maternal Health
    val isPregnant: Boolean = false,
    val gestationalAgeWeeks: Int? = null,
    val lastMenstrualPeriod: Long? = null, // timestamp

    // Messenger Integration (Phase 13)
    val messengerOptIn: Boolean = false,
    val messengerUserId: String? = null,
    val messengerName: String? = null,

    // Risk Flags
    val highRiskFlag: Boolean = false,
    val highRiskReasons: String? = null, // JSON array of strings
    val maternalHighRisk: Boolean = false,
    val lastRiskAssessment: Long? = null, // timestamp

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Sync Status
    val syncStatus: String = "PENDING", // PENDING, SYNCED, FAILED
    val syncedAt: Long? = null,
    val serverPatientId: String? = null // ID from backend after dedup
)
