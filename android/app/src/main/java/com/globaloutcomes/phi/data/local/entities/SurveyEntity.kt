package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Survey entity for structured health questionnaires
 * Types: NCD, MATERNAL, INFECTIOUS, MENTAL_HEALTH
 */
@Entity(
    tableName = "surveys",
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
        Index(value = ["surveyType"]),
        Index(value = ["syncStatus"])
    ]
)
data class SurveyEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val patientId: String,
    val scanId: String?,
    val surveyType: String, // NCD, MATERNAL, INFECTIOUS, MENTAL_HEALTH

    // Survey Responses (structured JSON)
    val responses: String, // JSON object with all answers

    // Metadata
    val completedAt: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val isComplete: Boolean = true,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Sync Status
    val syncStatus: String = "PENDING",
    val syncedAt: Long? = null,
    val serverSurveyId: String? = null
)
