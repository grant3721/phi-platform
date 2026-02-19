package com.globaloutcomes.phi.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Barangay Entity - Philippine Administrative Division Data
 * Quezon Province: 1,209 barangays across 40 municipalities
 */
@Entity(tableName = "barangays")
data class BarangayEntity(
    @PrimaryKey val code: String,
    val name: String,
    val municipalityCode: String,
    val municipalityName: String,
    val population: Int? = null
)
