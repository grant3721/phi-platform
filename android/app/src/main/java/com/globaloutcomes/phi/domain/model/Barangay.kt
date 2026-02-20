package com.globaloutcomes.phi.domain.model

data class Barangay(
    val id: String,
    val name: String,
    val municipality: String,
    val province: String,
    val region: String,
    val islandGroup: String,
    val population: Int? = null,
    val households: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val urbanRural: String? = null
) {
    val fullLocation: String
        get() = "$name, $municipality, $province"
}
