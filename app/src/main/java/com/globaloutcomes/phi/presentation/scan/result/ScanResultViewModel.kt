package com.globaloutcomes.phi.presentation.scan.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.Scan
import com.globaloutcomes.phi.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Scan Result Screen ViewModel
 * Loads and displays completed scan with all 34 biomarkers
 */
@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scanId: String = checkNotNull(savedStateHandle["scanId"])

    private val _state = MutableStateFlow(ScanResultState())
    val state: StateFlow<ScanResultState> = _state.asStateFlow()

    init {
        loadScan()
    }

    private fun loadScan() {
        viewModelScope.launch {
            scanRepository.getById(scanId)
                .onSuccess { scan ->
                    if (scan != null) {
                        _state.update {
                            it.copy(
                                scan = scan,
                                isLoading = false,
                                isHighRisk = scan.isHighRisk
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Scan not found"
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load scan"
                        )
                    }
                }
        }
    }
}

/**
 * Scan Result Screen State
 */
data class ScanResultState(
    val scan: Scan? = null,
    val isLoading: Boolean = true,
    val isHighRisk: Boolean = false,
    val error: String? = null
)
