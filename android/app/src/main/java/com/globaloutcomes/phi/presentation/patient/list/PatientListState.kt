package com.globaloutcomes.phi.presentation.patient.list

import com.globaloutcomes.phi.domain.model.Patient

data class PatientListState(
    val patients: List<Patient> = emptyList(),
    val searchQuery: String = "",
    val filterHighRiskOnly: Boolean = false,
    val selectedPatient: Patient? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
