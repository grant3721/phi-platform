package com.globaloutcomes.phi.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.usecase.CheckDailyCapUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val checkDailyCapUseCase: CheckDailyCapUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> {
                loadDashboardData()
            }

            HomeEvent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }

            else -> {
                // Navigation events handled in UI
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                // Load daily cap status
                // TODO: Get actual BHW ID from auth/settings
                val bhwId = "bhw_001"
                val capResult = checkDailyCapUseCase(bhwId)

                if (capResult.isSuccess) {
                    val capData = capResult.getOrNull()!!
                    _state.value = _state.value.copy(
                        scansToday = capData.scansToday,
                        earningsToday = capData.amountToday,
                        dailyCapReached = capData.isAtCap,
                        remainingScans = capData.remainingScans,
                        remainingAmount = capData.remainingAmount
                    )
                }

                // Load patient statistics
                val totalPatientsResult = patientRepository.getPatientCount()
                if (totalPatientsResult.isSuccess) {
                    _state.value = _state.value.copy(
                        totalPatients = totalPatientsResult.getOrNull() ?: 0
                    )
                }

                val highRiskCountResult = patientRepository.getHighRiskCount()
                if (highRiskCountResult.isSuccess) {
                    _state.value = _state.value.copy(
                        totalHighRiskPatients = highRiskCountResult.getOrNull() ?: 0
                    )
                }

                // Load high-risk patients (reactive with Flow)
                patientRepository.getHighRiskPatientsFlow()
                    .catch { error ->
                        _state.value = _state.value.copy(
                            errorMessage = "Failed to load high-risk patients: ${error.message}"
                        )
                    }
                    .collect { patients ->
                        _state.value = _state.value.copy(
                            highRiskPatients = patients.take(5), // Show top 5
                            isLoading = false
                        )
                    }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load dashboard: ${e.message}"
                )
            }
        }
    }
}
