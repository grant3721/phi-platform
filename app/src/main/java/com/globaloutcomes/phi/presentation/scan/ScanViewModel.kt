package com.globaloutcomes.phi.presentation.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Scan
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import com.globaloutcomes.phi.domain.scan.ScanConfig
import com.globaloutcomes.phi.domain.scan.ScanEngine
import com.globaloutcomes.phi.domain.scan.ScanSessionState
import com.globaloutcomes.phi.domain.scan.UserInfo
import com.globaloutcomes.phi.domain.usecase.referral.CreateReferralUseCase
import com.globaloutcomes.phi.domain.usecase.scan.AssessRiskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Scan Screen ViewModel
 * Manages scan session, camera preview, and scan results
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanEngine: ScanEngine,
    private val patientRepository: PatientRepository,
    private val scanRepository: ScanRepository,
    private val assessRiskUseCase: AssessRiskUseCase,
    private val createReferralUseCase: CreateReferralUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patientId: String = checkNotNull(savedStateHandle["patientId"])

    private val _state = MutableStateFlow(ScanScreenState())
    val state: StateFlow<ScanScreenState> = _state.asStateFlow()

    init {
        loadPatient()
    }

    private fun loadPatient() {
        viewModelScope.launch {
            patientRepository.getById(patientId)
                .onSuccess { patient ->
                    if (patient != null) {
                        _state.update {
                            it.copy(
                                patient = patient,
                                isLoading = false
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Patient not found"
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load patient"
                        )
                    }
                }
        }
    }

    fun startScan() {
        val patient = _state.value.patient ?: return

        val config = ScanConfig(
            patientId = patient.id,
            bhwId = "BHW001", // TODO: Get from authenticated user session
            scanType = "baseline",
            measurementDuration = 30, // 30 seconds for stub, 50 for real SDK
            userInfo = UserInfo(
                age = patient.age,
                sex = patient.sex,
                weightKg = null, // Not collected in registration
                heightCm = null
            )
        )

        viewModelScope.launch {
            _state.update { it.copy(scanInProgress = true, error = null) }

            scanEngine.startSession(config)
                .catch { error ->
                    _state.update {
                        it.copy(
                            scanInProgress = false,
                            error = error.message ?: "Scan failed"
                        )
                    }
                }
                .collect { sessionState ->
                    handleSessionState(sessionState)
                }
        }
    }

    fun stopScan() {
        scanEngine.stopSession()
        _state.update {
            it.copy(
                scanInProgress = false,
                sessionState = null
            )
        }
    }

    private suspend fun handleSessionState(sessionState: ScanSessionState) {
        _state.update { it.copy(sessionState = sessionState) }

        when (sessionState) {
            is ScanSessionState.Complete -> {
                // Save scan to database
                saveScan(sessionState)
            }
            is ScanSessionState.Error -> {
                _state.update {
                    it.copy(
                        scanInProgress = false,
                        error = sessionState.message
                    )
                }
            }
            else -> {
                // Other states are just displayed
            }
        }
    }

    private suspend fun saveScan(completeState: ScanSessionState.Complete) {
        val patient = _state.value.patient ?: return

        val scan = Scan(
            id = java.util.UUID.randomUUID().toString(),
            patientId = patient.id,
            bhwId = "BHW001", // TODO: Get from authenticated user session
            scanType = "baseline",
            biomarkers = completeState.biomarkers,
            signalQuality = completeState.signalQuality,
            validated = false, // Will be validated during sync
            riskFlags = emptyList(), // Will be calculated by risk assessment use case
            scannedAt = System.currentTimeMillis(),
            syncStatus = com.globaloutcomes.phi.domain.model.SyncStatus.PENDING
        )

        scanRepository.insert(scan)
            .onSuccess {
                // Assess risk after scan is saved
                assessRiskAndCreateReferral(scan.id)

                _state.update {
                    it.copy(
                        scanInProgress = false,
                        scanComplete = true,
                        completedScanId = scan.id
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        scanInProgress = false,
                        error = "Failed to save scan: ${error.message}"
                    )
                }
            }
    }

    private suspend fun assessRiskAndCreateReferral(scanId: String) {
        // Assess risk
        val riskAssessment = assessRiskUseCase(scanId)
        if (riskAssessment.isFailure) {
            // Log error but don't fail the scan
            return
        }

        val assessment = riskAssessment.getOrNull() ?: return

        // Create referral if high risk detected
        if (assessment.requiresReferral) {
            createReferralUseCase(
                scanId = scanId,
                riskFlags = assessment.riskReasons,
                notes = "Automatic referral: High-risk scan detected"
            )
            // Note: We don't check result here - referral will be created in background
            // If it fails, it will be retried during sync
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}

/**
 * Scan Screen State
 */
data class ScanScreenState(
    val patient: Patient? = null,
    val isLoading: Boolean = true,
    val scanInProgress: Boolean = false,
    val sessionState: ScanSessionState? = null,
    val scanComplete: Boolean = false,
    val completedScanId: String? = null,
    val error: String? = null
)
