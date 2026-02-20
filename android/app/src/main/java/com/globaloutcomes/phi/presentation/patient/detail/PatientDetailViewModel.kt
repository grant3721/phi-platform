package com.globaloutcomes.phi.presentation.patient.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import com.globaloutcomes.phi.domain.repository.SurveyRepository
import com.globaloutcomes.phi.domain.usecase.DeletePatientUseCase
import com.globaloutcomes.phi.domain.usecase.UpdatePatientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val scanRepository: ScanRepository,
    private val surveyRepository: SurveyRepository,
    private val referralRepository: ReferralRepository,
    private val updatePatientUseCase: UpdatePatientUseCase,
    private val deletePatientUseCase: DeletePatientUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patientId: String = checkNotNull(savedStateHandle["patientId"])

    private val _state = MutableStateFlow(PatientDetailState())
    val state: StateFlow<PatientDetailState> = _state.asStateFlow()

    private var originalPatient: Patient? = null

    init {
        loadPatientDetails()
    }

    fun onEvent(event: PatientDetailEvent) {
        when (event) {
            is PatientDetailEvent.SelectTab -> {
                _state.value = _state.value.copy(selectedTab = event.tab)
            }

            PatientDetailEvent.ToggleEditMode -> {
                if (_state.value.isEditMode) {
                    // Cancel edit - restore original
                    _state.value = _state.value.copy(
                        patient = originalPatient,
                        isEditMode = false
                    )
                } else {
                    // Enter edit mode - save original
                    originalPatient = _state.value.patient
                    _state.value = _state.value.copy(isEditMode = true)
                }
            }

            PatientDetailEvent.SaveChanges -> {
                saveChanges()
            }

            PatientDetailEvent.CancelEdit -> {
                _state.value = _state.value.copy(
                    patient = originalPatient,
                    isEditMode = false
                )
            }

            PatientDetailEvent.ShowDeleteConfirmation -> {
                _state.value = _state.value.copy(deleteConfirmationVisible = true)
            }

            PatientDetailEvent.HideDeleteConfirmation -> {
                _state.value = _state.value.copy(deleteConfirmationVisible = false)
            }

            PatientDetailEvent.DeletePatient -> {
                deletePatient()
            }

            PatientDetailEvent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }

            // Update events
            is PatientDetailEvent.UpdateFirstName -> {
                _state.value.patient?.let { patient ->
                    _state.value = _state.value.copy(
                        patient = patient.copy(firstName = event.firstName)
                    )
                }
            }

            is PatientDetailEvent.UpdateLastName -> {
                _state.value.patient?.let { patient ->
                    _state.value = _state.value.copy(
                        patient = patient.copy(lastName = event.lastName)
                    )
                }
            }

            is PatientDetailEvent.UpdatePhoneNumber -> {
                _state.value.patient?.let { patient ->
                    _state.value = _state.value.copy(
                        patient = patient.copy(phoneNumber = event.phoneNumber)
                    )
                }
            }

            is PatientDetailEvent.UpdateBarangay -> {
                _state.value.patient?.let { patient ->
                    _state.value = _state.value.copy(
                        patient = patient.copy(barangayId = event.barangayId)
                    )
                }
            }

            is PatientDetailEvent.UpdatePhilHealth -> {
                _state.value.patient?.let { patient ->
                    _state.value = _state.value.copy(
                        patient = patient.copy(philHealthNumber = event.philHealthNumber.takeIf { it.isNotBlank() })
                    )
                }
            }

            is PatientDetailEvent.UpdatePhilSys -> {
                _state.value.patient?.let { patient ->
                    _state.value = _state.value.copy(
                        patient = patient.copy(philSysNumber = event.philSysNumber.takeIf { it.isNotBlank() })
                    )
                }
            }

            is PatientDetailEvent.UpdatePregnancy -> {
                _state.value.patient?.let { patient ->
                    _state.value = _state.value.copy(
                        patient = patient.copy(
                            isPregnant = event.isPregnant,
                            gestationalAgeWeeks = if (!event.isPregnant) null else patient.gestationalAgeWeeks
                        )
                    )
                }
            }

            is PatientDetailEvent.UpdateGestationalAge -> {
                _state.value.patient?.let { patient ->
                    _state.value = _state.value.copy(
                        patient = patient.copy(gestationalAgeWeeks = event.weeks)
                    )
                }
            }

            is PatientDetailEvent.UpdateMessengerOptIn -> {
                _state.value.patient?.let { patient ->
                    _state.value = _state.value.copy(
                        patient = patient.copy(messengerOptIn = event.optIn)
                    )
                }
            }

            else -> {} // StartScan, ViewScanDetail handled by navigation
        }
    }

    private fun loadPatientDetails() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            // Load patient
            val patientResult = patientRepository.getPatientById(patientId)
            if (patientResult.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load patient: ${patientResult.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val patient = patientResult.getOrNull()
            if (patient == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Patient not found"
                )
                return@launch
            }

            // Load scans
            val scansResult = scanRepository.getScansForPatient(patientId)
            val scans = scansResult.getOrNull() ?: emptyList()

            // Load surveys
            val surveysResult = surveyRepository.getSurveysByPatientId(patientId)
            val surveys = surveysResult.getOrNull() ?: emptyList()

            // Load referrals
            val referralsResult = referralRepository.getReferralsByPatientId(patientId)
            val referrals = referralsResult.getOrNull() ?: emptyList()

            _state.value = _state.value.copy(
                patient = patient,
                scans = scans,
                surveys = surveys,
                referrals = referrals,
                isLoading = false,
                errorMessage = null
            )

            originalPatient = patient
        }
    }

    private fun saveChanges() {
        val patient = _state.value.patient ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)

            updatePatientUseCase(patient)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        isEditMode = false,
                        errorMessage = null
                    )
                    originalPatient = patient
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = "Failed to save changes: ${error.message}"
                    )
                }
        }
    }

    private fun deletePatient() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isDeleting = true,
                deleteConfirmationVisible = false,
                errorMessage = null
            )

            deletePatientUseCase(patientId)
                .onSuccess {
                    // Navigation handled in UI via LaunchedEffect
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        patient = null // Signal successful deletion
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        errorMessage = "Failed to delete patient: ${error.message}"
                    )
                }
        }
    }
}
