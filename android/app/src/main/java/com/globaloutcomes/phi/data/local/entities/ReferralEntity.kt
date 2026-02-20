package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Referral entity for three-tier referral system
 * Tiers: BHW_TO_BHS, BHS_TO_RHU, RHU_TO_HOSPITAL
 */
@Entity(
    tableName = "referrals",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["patientId"]),
        Index(value = ["scanId"]),
        Index(value = ["tier"]),
        Index(value = ["status"]),
        Index(value = ["dueBy"]),
        Index(value = ["syncStatus"])
    ]
)
data class ReferralEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val patientId: String,
    val scanId: String,

    // Referral Details
    val tier: String, // BHW_TO_BHS, BHS_TO_RHU, RHU_TO_HOSPITAL
    val status: String, // PENDING, CONFIRMED, IN_PROGRESS, RESOLVED, OVERDUE
    val priority: String, // ROUTINE, URGENT, EMERGENCY

    // Risk Information
    val riskLevel: String, // ELEVATED, HIGH
    val riskFlags: String, // JSON array of specific concerns
    val reasonForReferral: String,

    // Dates
    val referredAt: Long = System.currentTimeMillis(),
    val dueBy: Long, // referredAt + 48 hours for BHS, varies by tier
    val confirmedAt: Long? = null,
    val resolvedAt: Long? = null,

    // Resolution
    val resolutionNotes: String? = null,
    val outcome: String? = null, // TREATED, HOSPITALIZED, IMPROVED, DECLINED

    // Facility Information
    val fromFacility: String,
    val toFacility: String,
    val referringProvider: String,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Sync Status
    val syncStatus: String = "PENDING",
    val syncedAt: Long? = null,
    val serverReferralId: String? = null
)
