package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Survey Entity - Health Survey Responses
 * Stores structured survey data as JSON
 */
@Entity(
    tableName = "surveys",
    foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SurveyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val scanId: String,
    val patientId: String,
    val surveyType: String,                 // "ncd" | "infectious" | "maternal" | "mental_health"
    val responses: String,                   // Full JSON blob of all responses
    val completedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
