package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * YAKAP Claim entity for PhilHealth reimbursement claims
 * Generated from clinical encounters
 */
@Entity(
    tableName = "yakap_claims",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClinicalEncounterEntity::class,
            parentColumns = ["id"],
            childColumns = ["encounterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["patientId"]),
        Index(value = ["encounterId"]),
        Index(value = ["claimStatus"]),
        Index(value = ["claimType"]),
        Index(value = ["syncStatus"])
    ]
)
data class YakapClaimEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val patientId: String,
    val encounterId: String,

    // Claim Details
    val claimType: String, // PRIMARY_CARE, MATERNAL, TB_DOTS, ANIMAL_BITE, MINOR_SURGERY
    val claimAmount: Float,
    val claimStatus: String, // DRAFT, SUBMITTED, APPROVED, REJECTED, PAID

    // Patient PhilHealth Info
    val philHealthNumber: String,
    val philHealthMemberType: String, // MEMBER, DEPENDENT

    // Encounter Data
    val encounterDate: Long,
    val diagnosisIcd10: String,
    val diagnosisText: String,
    val facilityCode: String,
    val providerId: String,

    // Claim Validation
    val isValid: Boolean = false,
    val validationErrors: String? = null, // JSON array of missing/invalid fields
    val requiredDocuments: String, // JSON array of document requirements

    // Submission
    val submittedAt: Long? = null,
    val submittedBy: String? = null,
    val claimReference: String? = null,

    // PhilHealth Processing
    val processedAt: Long? = null,
    val approvedAmount: Float? = null,
    val rejectionReason: String? = null,
    val paidAt: Long? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Sync Status
    val syncStatus: String = "PENDING",
    val syncedAt: Long? = null,
    val serverClaimId: String? = null
)
