package com.globaloutcomes.phi.presentation.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.usecase.PerformScanUseCase
import com.globaloutcomes.phi.domain.usecase.ScanProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val performScanUseCase: PerformScanUseCase,
    private val patientRepository: PatientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patientId: String = checkNotNull(savedStateHandle["patientId"])

    private val _state = MutableStateFlow(ScanState(patientId = patientId))
    val state: StateFlow<ScanState> = _state.asStateFlow()

    init {
        loadPatientInfo()
    }

    fun onEvent(event: ScanEvent) {
        when (event) {
            ScanEvent.StartScan -> {
                startScan()
            }

            ScanEvent.CancelScan -> {
                cancelScan()
            }

            ScanEvent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }

            else -> {
                // Navigation events handled in UI
            }
        }
    }

    private fun loadPatientInfo() {
        viewModelScope.launch {
            val patientResult = patientRepository.getPatientById(patientId)
            patientResult.onSuccess { patient ->
                patient?.let {
                    _state.value = _state.value.copy(
                        patientName = it.fullName
                    )
                }
            }
        }
    }

    private fun startScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isScanning = true,
                errorMessage = null,
                isComplete = false,
                elapsedSeconds = 0,
                progress = 0f
            )

            performScanUseCase(patientId).collect { progress ->
                when (progress) {
                    is ScanProgress.InProgress -> {
                        val qualityText = when {
                            progress.signalQuality >= 0.8f -> "Excellent"
                            progress.signalQuality >= 0.6f -> "Good"
                            progress.signalQuality >= 0.4f -> "Fair"
                            else -> "Poor"
                        }
                        _state.value = _state.value.copy(
                            elapsedSeconds = progress.elapsedSeconds,
                            totalSeconds = progress.totalSeconds,
                            progress = progress.progress,
                            signalQuality = qualityText,
                            guidance = progress.guidance,
                            faceDetected = true
                        )
                    }

                    is ScanProgress.Complete -> {
                        _state.value = _state.value.copy(
                            isScanning = false,
                            isComplete = true,
                            completedScan = progress.scan,
                            guidance = "Scan complete!"
                        )
                    }

                    is ScanProgress.Failed -> {
                        _state.value = _state.value.copy(
                            isScanning = false,
                            errorMessage = progress.reason,
                            guidance = progress.reason
                        )
                    }
                }
            }
        }
    }

    private fun cancelScan() {
        viewModelScope.launch {
            performScanUseCase.cancel()
            _state.value = _state.value.copy(
                isScanning = false,
                guidance = "Scan cancelled",
                elapsedSeconds = 0,
                progress = 0f
            )
        }
    }
}
