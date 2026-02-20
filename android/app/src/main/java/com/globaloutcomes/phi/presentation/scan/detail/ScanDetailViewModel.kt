package com.globaloutcomes.phi.presentation.scan.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanDetailViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val patientRepository: PatientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scanId: String = checkNotNull(savedStateHandle["scanId"])

    private val _state = MutableStateFlow(ScanDetailState())
    val state: StateFlow<ScanDetailState> = _state.asStateFlow()

    init {
        loadScanDetails()
    }

    fun onEvent(event: ScanDetailEvent) {
        when (event) {
            is ScanDetailEvent.LoadScan -> {
                loadScanDetails()
            }

            is ScanDetailEvent.SelectCategory -> {
                _state.value = _state.value.copy(selectedCategory = event.category)
            }

            ScanDetailEvent.ShareScan -> {
                _state.value = _state.value.copy(showShareDialog = true)
            }

            ScanDetailEvent.DismissShareDialog -> {
                _state.value = _state.value.copy(showShareDialog = false)
            }

            ScanDetailEvent.NavigateBack -> {
                // Handled by navigation
            }

            ScanDetailEvent.CompareToPrevious -> {
                // TODO: Implement comparison view
            }
        }
    }

    private fun loadScanDetails() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            // Load scan
            val scanResult = scanRepository.getScanById(scanId)
            if (scanResult.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load scan: ${scanResult.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val scan = scanResult.getOrNull()
            if (scan == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Scan not found"
                )
                return@launch
            }

            // Load patient
            val patientResult = patientRepository.getPatientById(scan.patientId)
            val patient = patientResult.getOrNull()

            _state.value = _state.value.copy(
                scan = scan,
                patient = patient,
                isLoading = false,
                errorMessage = null
            )
        }
    }
}
