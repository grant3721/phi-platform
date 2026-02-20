package com.globaloutcomes.phi.presentation.patient.list

import com.globaloutcomes.phi.domain.model.Patient

sealed class PatientListEvent {
    data class SearchQueryChanged(val query: String) : PatientListEvent()
    data class HighRiskFilterToggled(val enabled: Boolean) : PatientListEvent()
    data class PatientSelected(val patient: Patient) : PatientListEvent()
    object AddNewPatient : PatientListEvent()
    object Refresh : PatientListEvent()
    object ClearError : PatientListEvent()
}
