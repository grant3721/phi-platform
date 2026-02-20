package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Config Threshold entity for clinical decision support
 * Downloaded from server, cached locally for offline operation
 */
@Entity(
    tableName = "config_thresholds",
    indices = [
        Index(value = ["thresholdType"]),
        Index(value = ["parameterName"]),
        Index(value = ["active"])
    ]
)
data class ConfigThresholdEntity(
    @PrimaryKey
    val id: String, // Server-assigned ID (e.g., "THR001")

    // Threshold Classification
    val thresholdType: String, // VITAL_SIGN, BLOODLESS_TEST, RISK_SCORE
    val parameterName: String, // systolic_bp, blood_glucose, etc.

    // Threshold Values
    val lowThreshold: Float? = null,
    val highThreshold: Float, // Primary threshold for flagging
    val criticalThreshold: Float? = null, // Urgent threshold
    val unit: String, // mmHg, mg/dL, %, etc.

    // Metadata
    val description: String,
    val category: String, // CARDIOVASCULAR, METABOLIC, RESPIRATORY, etc.
    val active: Boolean = true,

    // Clinical Guidelines
    val clinicalGuideline: String? = null, // Reference to DOH/WHO guidelines
    val recommendedAction: String? = null, // What to do when threshold exceeded

    // Age/Sex Adjustments
    val ageSpecific: Boolean = false,
    val ageAdjustments: String? = null, // JSON object with age ranges
    val sexSpecific: Boolean = false,
    val sexAdjustments: String? = null, // JSON object with sex-specific values

    // Timestamps
    val updatedAt: Long,
    val lastSyncedAt: Long = System.currentTimeMillis()
)
