package com.globaloutcomes.phi.presentation.patient.detail

import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Referral
import com.globaloutcomes.phi.domain.model.Scan
import com.globaloutcomes.phi.domain.model.Survey

data class PatientDetailState(
    val patient: Patient? = null,
    val scans: List<Scan> = emptyList(),
    val surveys: List<Survey> = emptyList(),
    val referrals: List<Referral> = emptyList(),
    val selectedTab: PatientDetailTab = PatientDetailTab.OVERVIEW,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteConfirmationVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class PatientDetailTab {
    OVERVIEW,
    SCANS,
    SURVEYS,
    REFERRALS
}
