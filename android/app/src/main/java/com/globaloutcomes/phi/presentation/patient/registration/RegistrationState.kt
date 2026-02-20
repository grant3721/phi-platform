package com.globaloutcomes.phi.presentation.patient.registration

import com.globaloutcomes.phi.domain.model.Patient

data class RegistrationState(
    val phoneNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthdate: Long? = null,
    val sex: String = "",
    val selectedBarangayId: String = "",
    val philHealthNumber: String = "",
    val philSysNumber: String = "",
    val isPregnant: Boolean = false,
    val gestationalAgeWeeks: Int? = null,
    val messengerOptIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val isDuplicateCheck: Boolean = false,
    val duplicatePatient: Patient? = null,
    val isSuccess: Boolean = false,
    val registeredPatientId: String? = null
)
