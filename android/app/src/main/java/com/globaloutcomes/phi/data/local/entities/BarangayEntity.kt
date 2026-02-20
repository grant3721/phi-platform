package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Barangay entity for location data
 * Pre-seeded with all 50 barangays from backend
 */
@Entity(
    tableName = "barangays",
    indices = [
        Index(value = ["municipality"]),
        Index(value = ["province"]),
        Index(value = ["region"])
    ]
)
data class BarangayEntity(
    @PrimaryKey
    val id: String, // Server ID (e.g., "BRG001")

    // Location Hierarchy
    val name: String,
    val municipality: String,
    val province: String,
    val region: String,
    val islandGroup: String, // LUZON, VISAYAS, MINDANAO

    // Demographics
    val population: Int? = null,
    val households: Int? = null,

    // Geographic
    val latitude: Double? = null,
    val longitude: Double? = null,
    val urbanRural: String? = null, // URBAN, RURAL

    // Timestamps
    val updatedAt: Long,
    val lastSyncedAt: Long = System.currentTimeMillis()
)
