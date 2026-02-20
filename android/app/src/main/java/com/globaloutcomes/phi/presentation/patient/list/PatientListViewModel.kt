package com.globaloutcomes.phi.presentation.patient.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientListViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PatientListState())
    val state: StateFlow<PatientListState> = _state.asStateFlow()

    init {
        loadPatients()
    }

    fun onEvent(event: PatientListEvent) {
        when (event) {
            is PatientListEvent.SearchQueryChanged -> {
                _state.value = _state.value.copy(searchQuery = event.query)
            }

            is PatientListEvent.HighRiskFilterToggled -> {
                _state.value = _state.value.copy(filterHighRiskOnly = event.enabled)
            }

            is PatientListEvent.PatientSelected -> {
                _state.value = _state.value.copy(selectedPatient = event.patient)
            }

            PatientListEvent.AddNewPatient -> {
                // Handled by navigation in UI
            }

            PatientListEvent.Refresh -> {
                loadPatients()
            }

            PatientListEvent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }
        }
    }

    private fun loadPatients() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            patientRepository.getAllPatientsFlow()
                .catch { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load patients: ${error.message}"
                    )
                }
                .collect { patients ->
                    _state.value = _state.value.copy(
                        patients = patients,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }
}
