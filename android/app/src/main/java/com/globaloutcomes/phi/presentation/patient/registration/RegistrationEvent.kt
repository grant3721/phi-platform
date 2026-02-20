package com.globaloutcomes.phi.presentation.patient.registration

sealed class RegistrationEvent {
    data class PhoneNumberChanged(val phoneNumber: String) : RegistrationEvent()
    data class FirstNameChanged(val firstName: String) : RegistrationEvent()
    data class LastNameChanged(val lastName: String) : RegistrationEvent()
    data class BirthdateSelected(val birthdate: Long) : RegistrationEvent()
    data class SexSelected(val sex: String) : RegistrationEvent()
    data class BarangaySelected(val barangayId: String) : RegistrationEvent()
    data class PhilHealthChanged(val number: String) : RegistrationEvent()
    data class PhilSysChanged(val number: String) : RegistrationEvent()
    data class PregnancyToggled(val isPregnant: Boolean) : RegistrationEvent()
    data class GestationalAgeChanged(val weeks: Int) : RegistrationEvent()
    data class MessengerOptInToggled(val optIn: Boolean) : RegistrationEvent()
    object CheckDuplicate : RegistrationEvent()
    object ProceedWithDuplicate : RegistrationEvent()
    object SubmitRegistration : RegistrationEvent()
    object ClearError : RegistrationEvent()
}
