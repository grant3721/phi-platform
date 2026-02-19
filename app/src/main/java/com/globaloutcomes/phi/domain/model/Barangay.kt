package com.globaloutcomes.phi.domain.model

/**
 * Barangay Domain Model
 */
data class Barangay(
    val code: String,
    val name: String,
    val municipalityCode: String,
    val municipalityName: String,
    val population: Int? = null
) {
    val displayName: String
        get() = "$name, $municipalityName"
}
