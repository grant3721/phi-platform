package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Referral Entity - Three-Tier Referral System
 * BHW → BHS → RHU escalation
 */
@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val sourceScanId: String,
    val tier: String,                       // "bhw_to_bhs" | "bhs_to_rhu"
    val status: String,                     // "pending" | "confirmed" | "resolved" | "escalated" | "overdue"
    val riskFlags: String? = null,          // JSON array
    val referredAt: Long = System.currentTimeMillis(),
    val dueBy: Long,                        // referredAt + 48 hours
    val resolvedAt: Long? = null,
    val resolvedBy: String? = null,
    val resolutionNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
