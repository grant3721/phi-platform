package com.globaloutcomes.phi.presentation.patient.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.Barangay
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.repository.BarangayRepository
import com.globaloutcomes.phi.domain.usecase.patient.CheckDuplicateUseCase
import com.globaloutcomes.phi.domain.usecase.patient.DuplicatePatientException
import com.globaloutcomes.phi.domain.usecase.patient.RegisterPatientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Registration Screen ViewModel
 * Handles patient registration with validation and duplicate checking
 */
@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerPatientUseCase: RegisterPatientUseCase,
    private val checkDuplicateUseCase: CheckDuplicateUseCase,
    private val barangayRepository: BarangayRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    init {
        loadBarangays()
    }

    fun onPhoneNumberChanged(phone: String) {
        _state.update { it.copy(phoneNumber = phone, phoneError = null, duplicatePatient = null) }
    }

    fun onFirstNameChanged(name: String) {
        _state.update { it.copy(firstName = name, firstNameError = null) }
    }

    fun onLastNameChanged(name: String) {
        _state.update { it.copy(lastName = name, lastNameError = null) }
    }

    fun onDateOfBirthChanged(dateMillis: Long) {
        _state.update { it.copy(dateOfBirth = dateMillis, dateOfBirthError = null) }
    }

    fun onSexChanged(sex: String) {
        _state.update { it.copy(sex = sex) }
    }

    fun onBarangaySelected(barangay: Barangay) {
        _state.update {
            it.copy(
                selectedBarangay = barangay,
                barangayError = null
            )
        }
    }

    fun onPhilHealthNumberChanged(number: String) {
        _state.update { it.copy(philHealthNumber = number.takeIf { it.isNotBlank() }) }
    }

    fun onPhilsysIdChanged(id: String) {
        _state.update { it.copy(philsysId = id.takeIf { it.isNotBlank() }) }
    }

    fun onPregnantChanged(isPregnant: Boolean) {
        _state.update {
            it.copy(
                isPregnant = isPregnant,
                gestationalAgeWeeks = if (!isPregnant) null else it.gestationalAgeWeeks
            )
        }
    }

    fun onGestationalAgeChanged(weeks: Int?) {
        _state.update { it.copy(gestationalAgeWeeks = weeks) }
    }

    fun onMessengerOptInChanged(optIn: Boolean) {
        _state.update {
            it.copy(
                messengerOptIn = optIn,
                messengerContactMethod = if (!optIn) null else it.messengerContactMethod,
                messengerContactValue = if (!optIn) null else it.messengerContactValue
            )
        }
    }

    fun onMessengerContactMethodChanged(method: String?) {
        _state.update { it.copy(messengerContactMethod = method) }
    }

    fun onMessengerContactValueChanged(value: String?) {
        _state.update { it.copy(messengerContactValue = value) }
    }

    fun onCheckDuplicate() {
        val phone = _state.value.phoneNumber.trim()
        if (phone.isBlank()) {
            _state.update { it.copy(phoneError = "Phone number is required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isCheckingDuplicate = true) }

            checkDuplicateUseCase(phone)
                .onSuccess { existingPatient ->
                    if (existingPatient != null) {
                        _state.update {
                            it.copy(
                                isCheckingDuplicate = false,
                                duplicatePatient = existingPatient,
                                phoneError = "Patient already registered"
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isCheckingDuplicate = false,
                                duplicatePatient = null,
                                phoneError = null
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isCheckingDuplicate = false,
                            phoneError = error.message ?: "Failed to check duplicate"
                        )
                    }
                }
        }
    }

    fun onRegister() {
        if (!validateForm()) {
            return
        }

        val currentState = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isRegistering = true, registrationError = null) }

            registerPatientUseCase(
                phoneNumber = currentState.phoneNumber.trim(),
                firstName = currentState.firstName.trim(),
                lastName = currentState.lastName.trim(),
                dateOfBirth = currentState.dateOfBirth!!,
                sex = currentState.sex,
                barangayCode = currentState.selectedBarangay!!.code,
                municipalityCode = currentState.selectedBarangay!!.municipalityCode,
                philhealthNumber = currentState.philHealthNumber,
                philsysId = currentState.philsysId,
                pregnant = currentState.isPregnant,
                gestationalAgeWeeks = currentState.gestationalAgeWeeks,
                messengerOptIn = currentState.messengerOptIn,
                messengerContactMethod = currentState.messengerContactMethod,
                messengerContactValue = currentState.messengerContactValue
            )
                .onSuccess { patient ->
                    _state.update {
                        it.copy(
                            isRegistering = false,
                            registrationComplete = true,
                            registeredPatient = patient
                        )
                    }
                }
                .onFailure { error ->
                    val errorMessage = when (error) {
                        is DuplicatePatientException -> "Patient already registered with this phone number"
                        else -> error.message ?: "Registration failed"
                    }
                    _state.update {
                        it.copy(
                            isRegistering = false,
                            registrationError = errorMessage
                        )
                    }
                }
        }
    }

    private fun validateForm(): Boolean {
        val currentState = _state.value
        var isValid = true
        var newState = currentState

        // Validate phone number
        if (currentState.phoneNumber.isBlank()) {
            newState = newState.copy(phoneError = "Phone number is required")
            isValid = false
        } else if (currentState.duplicatePatient != null) {
            newState = newState.copy(phoneError = "Patient already registered")
            isValid = false
        }

        // Validate first name
        if (currentState.firstName.isBlank()) {
            newState = newState.copy(firstNameError = "First name is required")
            isValid = false
        }

        // Validate last name
        if (currentState.lastName.isBlank()) {
            newState = newState.copy(lastNameError = "Last name is required")
            isValid = false
        }

        // Validate date of birth
        if (currentState.dateOfBirth == null) {
            newState = newState.copy(dateOfBirthError = "Date of birth is required")
            isValid = false
        } else {
            val ageMillis = System.currentTimeMillis() - currentState.dateOfBirth!!
            val ageYears = ageMillis / (365.25 * 24 * 60 * 60 * 1000)
            if (ageYears < 0 || ageYears > 120) {
                newState = newState.copy(dateOfBirthError = "Invalid date of birth")
                isValid = false
            }
        }

        // Validate barangay
        if (currentState.selectedBarangay == null) {
            newState = newState.copy(barangayError = "Barangay is required")
            isValid = false
        }

        _state.update { newState }
        return isValid
    }

    private fun loadBarangays() {
        viewModelScope.launch {
            barangayRepository.getAll()
                .onSuccess { barangays ->
                    _state.update { it.copy(barangays = barangays) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(registrationError = "Failed to load barangays: ${error.message}")
                    }
                }
        }
    }
}

/**
 * Registration Screen State
 */
data class RegistrationState(
    // Form fields
    val phoneNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: Long? = null,
    val sex: String = "M", // Default to Male
    val selectedBarangay: Barangay? = null,
    val philHealthNumber: String? = null,
    val philsysId: String? = null,
    val isPregnant: Boolean = false,
    val gestationalAgeWeeks: Int? = null,
    val messengerOptIn: Boolean = false,
    val messengerContactMethod: String? = null,
    val messengerContactValue: String? = null,

    // Validation errors
    val phoneError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val dateOfBirthError: String? = null,
    val barangayError: String? = null,

    // State
    val barangays: List<Barangay> = emptyList(),
    val isCheckingDuplicate: Boolean = false,
    val duplicatePatient: Patient? = null,
    val isRegistering: Boolean = false,
    val registrationComplete: Boolean = false,
    val registeredPatient: Patient? = null,
    val registrationError: String? = null
)
