package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Patient Entity - Demographics and Registration Data
 * Index on phoneNumber for deduplication
 */
@Entity(
    tableName = "patients",
    indices = [Index(value = ["phoneNumber"], unique = true)]
)
data class PatientEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Long,                  // epoch millis
    val sex: String,                         // "male" | "female"
    val barangayCode: String,
    val municipalityCode: String,
    val philhealthNumber: String? = null,    // optional in demo
    val philsysId: String? = null,           // optional in demo
    val pregnant: Boolean = false,
    val gestationalAgeWeeks: Int? = null,
    val highRiskFlag: Boolean = false,
    val highRiskReasons: String? = null,     // JSON array stored as string
    val maternalHighRisk: Boolean = false,
    val lastScanDate: Long? = null,
    val totalScans: Int = 0,

    // Facebook Messenger Integration (Phase 13)
    val messengerOptIn: Boolean = false,
    val messengerContactMethod: String? = null,   // "phone" | "facebook_name"
    val messengerContactValue: String? = null,     // phone number or FB name
    val messengerPsid: String? = null,             // Facebook Page-Scoped ID

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"       // PENDING | SYNCED | FAILED
)
