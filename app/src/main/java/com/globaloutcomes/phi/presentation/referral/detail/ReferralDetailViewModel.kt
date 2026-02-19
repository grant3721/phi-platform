package com.globaloutcomes.phi.presentation.referral.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Referral
import com.globaloutcomes.phi.domain.model.ReferralStatus
import com.globaloutcomes.phi.domain.model.ReferralTier
import com.globaloutcomes.phi.domain.model.Scan
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Referral Detail ViewModel
 * Manages individual referral viewing and status updates
 */
@HiltViewModel
class ReferralDetailViewModel @Inject constructor(
    private val referralRepository: ReferralRepository,
    private val patientRepository: PatientRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReferralDetailState())
    val state: StateFlow<ReferralDetailState> = _state.asStateFlow()

    fun loadReferral(referralId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Load referral
            val referralResult = referralRepository.getById(referralId)
            if (referralResult.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = referralResult.exceptionOrNull()?.message ?: "Failed to load referral"
                )
                return@launch
            }

            val referral = referralResult.getOrNull()
            if (referral == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Referral not found"
                )
                return@launch
            }

            // Load patient
            val patientResult = patientRepository.getById(referral.patientId)
            val patient = patientResult.getOrNull()

            // Load scan
            val scanResult = scanRepository.getById(referral.scanId)
            val scan = scanResult.getOrNull()

            _state.value = ReferralDetailState(
                referral = referral,
                patient = patient,
                scan = scan,
                isLoading = false,
                error = null
            )
        }
    }

    fun resolveReferral(notes: String) {
        viewModelScope.launch {
            val referral = _state.value.referral ?: return@launch

            val updatedReferral = referral.copy(
                status = ReferralStatus.RESOLVED,
                resolvedAt = System.currentTimeMillis(),
                resolvedBy = "current_bhw", // TODO: Get from auth
                resolutionNotes = notes
            )

            val result = referralRepository.update(updatedReferral)
            if (result.isSuccess) {
                _state.value = _state.value.copy(referral = updatedReferral)
            } else {
                _state.value = _state.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to update referral"
                )
            }
        }
    }

    fun escalateReferral(notes: String) {
        viewModelScope.launch {
            val referral = _state.value.referral ?: return@launch

            // Determine next tier
            val nextTier = when (referral.tier) {
                ReferralTier.BHW_TO_BHS -> ReferralTier.BHS_TO_RHU
                ReferralTier.BHS_TO_RHU -> ReferralTier.RHU_TO_HOSPITAL
                ReferralTier.RHU_TO_HOSPITAL -> {
                    _state.value = _state.value.copy(error = "Already at highest tier")
                    return@launch
                }
            }

            // Update current referral to ESCALATED
            val updatedReferral = referral.copy(
                status = ReferralStatus.ESCALATED,
                resolvedAt = System.currentTimeMillis(),
                resolvedBy = "current_bhw", // TODO: Get from auth
                resolutionNotes = "Escalated to ${formatTier(nextTier)}: $notes"
            )

            val result = referralRepository.update(updatedReferral)
            if (result.isSuccess) {
                // Create new referral at next tier
                val newReferral = referral.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    tier = nextTier,
                    status = ReferralStatus.PENDING,
                    referredAt = System.currentTimeMillis(),
                    dueBy = System.currentTimeMillis() + (48 * 60 * 60 * 1000), // 48 hours
                    notes = "Escalated from ${formatTier(referral.tier)}: $notes",
                    resolvedAt = null,
                    resolvedBy = null,
                    resolutionNotes = null
                )

                val createResult = referralRepository.insert(newReferral)
                if (createResult.isSuccess) {
                    _state.value = _state.value.copy(referral = updatedReferral)
                } else {
                    _state.value = _state.value.copy(
                        error = createResult.exceptionOrNull()?.message ?: "Failed to create escalated referral"
                    )
                }
            } else {
                _state.value = _state.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to update referral"
                )
            }
        }
    }

    private fun formatTier(tier: ReferralTier): String {
        return when (tier) {
            ReferralTier.BHW_TO_BHS -> "BHW → BHS"
            ReferralTier.BHS_TO_RHU -> "BHS → RHU"
            ReferralTier.RHU_TO_HOSPITAL -> "RHU → Hospital"
        }
    }
}

/**
 * Referral Detail State
 */
data class ReferralDetailState(
    val referral: Referral? = null,
    val patient: Patient? = null,
    val scan: Scan? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
