package com.globaloutcomes.phi.presentation.referral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferralListViewModel @Inject constructor(
    private val referralRepository: ReferralRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReferralListState())
    val state: StateFlow<ReferralListState> = _state.asStateFlow()

    init {
        loadReferrals()
    }

    fun onEvent(event: ReferralListEvent) {
        when (event) {
            is ReferralListEvent.FilterByStatus -> {
                _state.value = _state.value.copy(filterStatus = event.status)
            }

            ReferralListEvent.Refresh -> {
                loadReferrals()
            }

            ReferralListEvent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }

            else -> {
                // Navigation events handled in UI
            }
        }
    }

    private fun loadReferrals() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            referralRepository.getAllReferralsFlow()
                .catch { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load referrals: ${error.message}"
                    )
                }
                .collect { referrals ->
                    _state.value = _state.value.copy(
                        referrals = referrals,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }
}
