package com.globaloutcomes.phi.presentation.patient.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Sex
import com.globaloutcomes.phi.domain.usecase.CheckDuplicateUseCase
import com.globaloutcomes.phi.domain.usecase.RegisterPatientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerPatientUseCase: RegisterPatientUseCase,
    private val checkDuplicateUseCase: CheckDuplicateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    fun onEvent(event: RegistrationEvent) {
        when (event) {
            is RegistrationEvent.PhoneNumberChanged -> {
                _state.value = _state.value.copy(
                    phoneNumber = event.phoneNumber,
                    errorMessage = null,
                    duplicatePatient = null
                )
            }
            is RegistrationEvent.FirstNameChanged -> {
                _state.value = _state.value.copy(firstName = event.firstName)
            }
            is RegistrationEvent.LastNameChanged -> {
                _state.value = _state.value.copy(lastName = event.lastName)
            }
            is RegistrationEvent.BirthdateSelected -> {
                _state.value = _state.value.copy(birthdate = event.birthdate)
            }
            is RegistrationEvent.SexSelected -> {
                _state.value = _state.value.copy(sex = event.sex)
            }
            is RegistrationEvent.BarangaySelected -> {
                _state.value = _state.value.copy(selectedBarangayId = event.barangayId)
            }
            is RegistrationEvent.PhilHealthChanged -> {
                _state.value = _state.value.copy(philHealthNumber = event.number)
            }
            is RegistrationEvent.PhilSysChanged -> {
                _state.value = _state.value.copy(philSysNumber = event.number)
            }
            is RegistrationEvent.PregnancyToggled -> {
                _state.value = _state.value.copy(
                    isPregnant = event.isPregnant,
                    gestationalAgeWeeks = if (!event.isPregnant) null else _state.value.gestationalAgeWeeks
                )
            }
            is RegistrationEvent.GestationalAgeChanged -> {
                _state.value = _state.value.copy(gestationalAgeWeeks = event.weeks)
            }
            is RegistrationEvent.MessengerOptInToggled -> {
                _state.value = _state.value.copy(messengerOptIn = event.optIn)
            }
            is RegistrationEvent.CheckDuplicate -> {
                checkForDuplicate()
            }
            is RegistrationEvent.ProceedWithDuplicate -> {
                _state.value = _state.value.copy(duplicatePatient = null)
            }
            is RegistrationEvent.SubmitRegistration -> {
                submitRegistration()
            }
            is RegistrationEvent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }
        }
    }

    private fun checkForDuplicate() {
        val phoneNumber = _state.value.phoneNumber

        if (phoneNumber.isBlank()) {
            _state.value = _state.value.copy(
                errorMessage = "Phone number is required"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isDuplicateCheck = true)

            checkDuplicateUseCase(phoneNumber)
                .onSuccess { result ->
                    if (result.isDuplicate) {
                        Timber.d("Duplicate patient found: ${result.existingPatient?.fullName}")
                        _state.value = _state.value.copy(
                            isDuplicateCheck = false,
                            duplicatePatient = result.existingPatient,
                            errorMessage = "Patient already registered"
                        )
                    } else {
                        Timber.d("No duplicate found, proceeding")
                        _state.value = _state.value.copy(
                            isDuplicateCheck = false,
                            duplicatePatient = null
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error, "Duplicate check failed")
                    _state.value = _state.value.copy(
                        isDuplicateCheck = false,
                        errorMessage = "Could not check for duplicates: ${error.message}"
                    )
                }
        }
    }

    private fun submitRegistration() {
        // Validate form
        val errors = validateForm()
        if (errors.isNotEmpty()) {
            _state.value = _state.value.copy(
                validationErrors = errors,
                errorMessage = "Please fix the errors below"
            )
            return
        }

        // Create patient
        val patient = Patient(
            phoneNumber = _state.value.phoneNumber,
            firstName = _state.value.firstName.trim(),
            lastName = _state.value.lastName.trim(),
            birthdate = _state.value.birthdate!!,
            sex = Sex.valueOf(_state.value.sex.uppercase()),
            barangayId = _state.value.selectedBarangayId!!,
            philHealthNumber = _state.value.philHealthNumber.takeIf { it.isNotBlank() },
            philSysNumber = _state.value.philSysNumber.takeIf { it.isNotBlank() },
            isPregnant = _state.value.isPregnant,
            gestationalAgeWeeks = _state.value.gestationalAgeWeeks,
            messengerOptIn = _state.value.messengerOptIn
        )

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            registerPatientUseCase(patient)
                .onSuccess { patientId ->
                    Timber.i("Patient registered successfully: $patientId")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        registeredPatientId = patientId,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    Timber.e(error, "Registration failed")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Registration failed: ${error.message}"
                    )
                }
        }
    }

    private fun validateForm(): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (_state.value.phoneNumber.isBlank()) {
            errors["phoneNumber"] = "Phone number is required"
        } else if (!_state.value.phoneNumber.matches(Regex("^09\\d{9}$"))) {
            errors["phoneNumber"] = "Invalid phone number format (09XXXXXXXXX)"
        }

        if (_state.value.firstName.isBlank()) {
            errors["firstName"] = "First name is required"
        }

        if (_state.value.lastName.isBlank()) {
            errors["lastName"] = "Last name is required"
        }

        if (_state.value.birthdate == null) {
            errors["birthdate"] = "Birthdate is required"
        }

        if (_state.value.selectedBarangayId == null) {
            errors["barangay"] = "Barangay is required"
        }

        if (_state.value.isPregnant) {
            val weeks = _state.value.gestationalAgeWeeks
            if (weeks == null) {
                errors["gestationalAge"] = "Gestational age is required for pregnant patients"
            } else if (weeks < 0 || weeks > 42) {
                errors["gestationalAge"] = "Gestational age must be between 0 and 42 weeks"
            }
        }

        return errors
    }
}
