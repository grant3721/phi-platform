package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * YAKAP Claim Entity - PhilHealth Claims
 * Generated from clinical encounters
 */
@Entity(tableName = "yakap_claims")
data class YakapClaimEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val encounterId: String,
    val claimType: String,                  // "fpe" | "follow_up" | "lab" | "medicine" | "screening"
    val philhealthNumber: String? = null,
    val providerId: String,
    val facilityCode: String,
    val diagnosisIcd10: String? = null,
    val claimAmount: Double? = null,
    val status: String = "draft",           // "draft" | "ready" | "submitted" | "accepted" | "denied"
    val denialReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val submittedAt: Long? = null,
    val syncStatus: String = "PENDING"
)
