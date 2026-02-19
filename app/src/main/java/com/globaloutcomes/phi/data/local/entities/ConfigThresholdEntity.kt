package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Config Threshold Entity - Server-Configurable Risk Thresholds
 * Allows operational flexibility without app updates
 */
@Entity(tableName = "config_thresholds")
data class ConfigThresholdEntity(
    @PrimaryKey val key: String,            // e.g. "bp_systolic_crisis", "spo2_low"
    val value: Double,
    val description: String,
    val updatedAt: Long = System.currentTimeMillis()
)
