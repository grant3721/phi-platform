package com.globaloutcomes.phi.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home Screen ViewModel
 * Provides real-time stats from Room database
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            // Combine multiple flows for reactive updates
            combine(
                scanRepository.getScansToday(),
                patientRepository.getAll(),
                scanRepository.getAll() // For total scans calculation
            ) { scansToday, patients, allScans ->
                val scansCount = scansToday.getOrNull()?.size ?: 0
                val earnings = calculateEarnings(scansCount)

                HomeState(
                    scansToday = scansCount,
                    earningsToday = earnings,
                    totalPatients = patients.getOrNull()?.size ?: 0,
                    pendingReferrals = 0, // Will be implemented in Phase 8
                    isLoading = false
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun calculateEarnings(scansCount: Int): Double {
        // ₱3.00 per scan, max 50 scans (₱150 daily cap)
        val validScans = scansCount.coerceAtMost(50)
        return validScans * 3.0
    }
}

/**
 * Home Screen State
 */
data class HomeState(
    val scansToday: Int = 0,
    val earningsToday: Double = 0.0,
    val totalPatients: Int = 0,
    val pendingReferrals: Int = 0,
    val isLoading: Boolean = true
)
