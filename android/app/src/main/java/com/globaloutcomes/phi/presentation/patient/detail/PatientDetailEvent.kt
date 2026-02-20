package com.globaloutcomes.phi.presentation.patient.detail

sealed class PatientDetailEvent {
    data class SelectTab(val tab: Int) : PatientDetailEvent()
    object ToggleEditMode : PatientDetailEvent()
    object SaveChanges : PatientDetailEvent()
    object CancelEdit : PatientDetailEvent()
    object ShowDeleteConfirmation : PatientDetailEvent()
    object HideDeleteConfirmation : PatientDetailEvent()
    object DeletePatient : PatientDetailEvent()
    object ClearError : PatientDetailEvent()

    // Update events
    data class UpdateFirstName(val firstName: String) : PatientDetailEvent()
    data class UpdateLastName(val lastName: String) : PatientDetailEvent()
    data class UpdatePhoneNumber(val phoneNumber: String) : PatientDetailEvent()
    data class UpdateBarangay(val barangayId: String) : PatientDetailEvent()
    data class UpdatePhilHealth(val philHealthNumber: String) : PatientDetailEvent()
    data class UpdatePhilSys(val philSysNumber: String) : PatientDetailEvent()
    data class UpdatePregnancy(val isPregnant: Boolean) : PatientDetailEvent()
    data class UpdateGestationalAge(val weeks: Int) : PatientDetailEvent()
    data class UpdateMessengerOptIn(val optIn: Boolean) : PatientDetailEvent()

    // Navigation events (handled in UI)
    object StartScan : PatientDetailEvent()
    data class ViewScanDetail(val scanId: String) : PatientDetailEvent()
}
