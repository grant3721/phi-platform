package com.globaloutcomes.phi.presentation.home

import com.globaloutcomes.phi.domain.model.Patient

data class HomeState(
    val scansToday: Int = 0,
    val earningsToday: Float = 0f,
    val remainingScans: Int = 50,
    val remainingAmount: Float = 150f,
    val dailyCapReached: Boolean = false,
    val totalPatients: Int = 0,
    val totalHighRiskPatients: Int = 0,
    val highRiskPatients: List<Patient> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
