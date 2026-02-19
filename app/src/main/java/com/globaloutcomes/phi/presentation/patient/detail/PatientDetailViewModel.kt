package com.globaloutcomes.phi.presentation.patient.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Referral
import com.globaloutcomes.phi.domain.model.ReferralStatus
import com.globaloutcomes.phi.domain.model.Scan
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import com.globaloutcomes.phi.domain.repository.SurveyRepository
import com.globaloutcomes.phi.domain.usecase.scan.CheckRescanEligibilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Patient Detail ViewModel
 * Loads and manages comprehensive patient information
 */
@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val scanRepository: ScanRepository,
    private val surveyRepository: SurveyRepository,
    private val referralRepository: ReferralRepository,
    private val checkRescanEligibilityUseCase: CheckRescanEligibilityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PatientDetailState())
    val state: StateFlow<PatientDetailState> = _state.asStateFlow()

    fun loadPatient(patientId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Load patient
            val patientResult = patientRepository.getById(patientId)
            if (patientResult.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = patientResult.exceptionOrNull()?.message ?: "Failed to load patient"
                )
                return@launch
            }

            val patient = patientResult.getOrNull()
            if (patient == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Patient not found"
                )
                return@launch
            }

            // Load scans
            val scansResult = scanRepository.getByPatientId(patientId)
            val scans = scansResult.getOrNull() ?: emptyList()
            val sortedScans = scans.sortedByDescending { it.scannedAt }

            // Load surveys
            val surveysResult = surveyRepository.getByPatientId(patientId)
            val surveys = surveysResult.getOrNull() ?: emptyList()

            // Load referrals
            val referralsResult = referralRepository.getByPatientId(patientId)
            val referrals = referralsResult.getOrNull() ?: emptyList()
            val sortedReferrals = referrals.sortedByDescending { it.referredAt }

            // Count active referrals
            val activeReferrals = referrals.count {
                it.status == ReferralStatus.PENDING ||
                it.status == ReferralStatus.CONFIRMED ||
                it.status == ReferralStatus.IN_PROGRESS ||
                it.status == ReferralStatus.OVERDUE
            }

            // Check rescan eligibility
            val eligibilityResult = checkRescanEligibilityUseCase(patientId)
            val eligibilityMessage = when {
                eligibilityResult.isFailure -> "Unable to check eligibility"
                else -> {
                    val eligibility = eligibilityResult.getOrNull()
                    when {
                        eligibility == null -> "Unable to check eligibility"
                        eligibility.allowed -> "✓ ${eligibility.reason}"
                        else -> {
                            if (eligibility.daysUntilEligible != null && eligibility.daysUntilEligible > 0) {
                                "✗ ${eligibility.reason} (${eligibility.daysUntilEligible} days remaining)"
                            } else {
                                "✗ ${eligibility.reason}"
                            }
                        }
                    }
                }
            }

            _state.value = PatientDetailState(
                patient = patient,
                scans = sortedScans,
                referrals = sortedReferrals,
                activeReferralsCount = activeReferrals,
                completedSurveysCount = surveys.size,
                rescanEligibility = eligibilityMessage,
                isLoading = false,
                error = null
            )
        }
    }
}

/**
 * Patient Detail State
 */
data class PatientDetailState(
    val patient: Patient? = null,
    val scans: List<Scan> = emptyList(),
    val referrals: List<Referral> = emptyList(),
    val activeReferralsCount: Int = 0,
    val completedSurveysCount: Int = 0,
    val rescanEligibility: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
