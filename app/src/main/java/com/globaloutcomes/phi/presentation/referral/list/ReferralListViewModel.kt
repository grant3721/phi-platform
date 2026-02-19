package com.globaloutcomes.phi.presentation.referral.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.Referral
import com.globaloutcomes.phi.domain.model.ReferralStatus
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Referral List ViewModel
 * Loads and manages all referrals for current BHW
 * Sorts by priority: overdue first, then pending, then others
 */
@HiltViewModel
class ReferralListViewModel @Inject constructor(
    private val referralRepository: ReferralRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReferralListState())
    val state: StateFlow<ReferralListState> = _state.asStateFlow()

    init {
        loadReferrals()
    }

    private fun loadReferrals() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            referralRepository.observeAll().collect { result ->
                result.fold(
                    onSuccess = { referrals ->
                        val sorted = sortReferralsByPriority(referrals)
                        val overdueCount = referrals.count { isOverdue(it) }

                        _state.value = ReferralListState(
                            referrals = sorted,
                            overdueCount = overdueCount,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load referrals"
                        )
                    }
                )
            }
        }
    }

    /**
     * Sort referrals by priority:
     * 1. Overdue first
     * 2. Pending
     * 3. Others by most recent first
     */
    private fun sortReferralsByPriority(referrals: List<Referral>): List<Referral> {
        return referrals.sortedWith(
            compareBy(
                // Primary sort: overdue status (overdue first)
                { !isOverdue(it) },
                // Secondary sort: pending status (pending before resolved)
                { it.status != ReferralStatus.PENDING },
                // Tertiary sort: most recent first
                { -it.referredAt }
            )
        )
    }

    /**
     * Check if referral is overdue
     */
    private fun isOverdue(referral: Referral): Boolean {
        return referral.status == ReferralStatus.OVERDUE ||
                (referral.status == ReferralStatus.PENDING && referral.dueBy < System.currentTimeMillis())
    }

    fun refresh() {
        loadReferrals()
    }
}

/**
 * Referral List State
 */
data class ReferralListState(
    val referrals: List<Referral> = emptyList(),
    val overdueCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)
