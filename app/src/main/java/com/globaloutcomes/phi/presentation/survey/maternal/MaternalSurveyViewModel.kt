package com.globaloutcomes.phi.presentation.survey.maternal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Maternal Survey ViewModel
 * Manages state for Maternal Health survey
 */
@HiltViewModel
class MaternalSurveyViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scanId: String = checkNotNull(savedStateHandle["scanId"])

    private val _state = MutableStateFlow(MaternalSurveyState())
    val state: StateFlow<MaternalSurveyState> = _state.asStateFlow()

    // Pregnancy Info
    fun onLmpDateSelected(dateMillis: Long) {
        _state.update { it.copy(lmpDate = dateMillis) }
        // Auto-calculate gestational age from LMP
        val weeksFromLmp = calculateGestationalAgeFromLmp(dateMillis)
        _state.update { it.copy(gestationalAgeWeeks = weeksFromLmp) }
        updateProgress()
    }

    fun onGestationalAgeChanged(weeks: Int) {
        _state.update { it.copy(gestationalAgeWeeks = weeks) }
        updateProgress()
    }

    fun onGravidityChanged(count: Int) {
        _state.update { it.copy(gravidity = count) }
        updateProgress()
    }

    fun onParityChanged(count: Int) {
        _state.update { it.copy(parity = count) }
        updateProgress()
    }

    fun onAbortionsChanged(count: Int) {
        _state.update { it.copy(abortions = count) }
    }

    // Complications
    fun onToggleComplication(complication: MaternalComplication) {
        val current = _state.value.previousComplications.toMutableList()
        if (current.contains(complication)) {
            current.remove(complication)
        } else {
            current.add(complication)
        }
        _state.update { it.copy(previousComplications = current) }
    }

    // Symptoms
    fun onToggleSymptom(symptom: MaternalSymptom) {
        val current = _state.value.currentSymptoms.toMutableList()
        if (current.contains(symptom)) {
            current.remove(symptom)
        } else {
            current.add(symptom)
        }
        _state.update { it.copy(currentSymptoms = current) }
    }

    // Prenatal Care
    fun onPrenatalVisitsChanged(count: Int) {
        _state.update { it.copy(prenatalVisits = count) }
        updateProgress()
    }

    fun onLastVisitDateSelected(dateMillis: Long) {
        _state.update { it.copy(lastPrenatalVisit = dateMillis) }
        updateProgress()
    }

    // Supplementation
    fun onTakingIronChanged(value: Boolean?) {
        _state.update { it.copy(takingIronSupplements = value) }
        updateProgress()
    }

    fun onTakingFolicAcidChanged(value: Boolean?) {
        _state.update { it.copy(takingFolicAcid = value) }
        updateProgress()
    }

    fun onTakingCalciumChanged(value: Boolean?) {
        _state.update { it.copy(takingCalcium = value) }
        updateProgress()
    }

    // Immunization
    fun onTetanusDosesChanged(count: Int) {
        _state.update { it.copy(tetanusToxoidDoses = count) }
        updateProgress()
    }

    // Risk Factors
    fun onMultiplePregnancyChanged(value: Boolean?) {
        _state.update { it.copy(multiplePregnancy = value) }
        updateProgress()
    }

    fun onGestationalDiabetesChanged(value: Boolean?) {
        _state.update { it.copy(gestationalDiabetes = value) }
        updateProgress()
    }

    fun onPreeclampsiaChanged(value: Boolean?) {
        _state.update { it.copy(preeclampsia = value) }
        updateProgress()
    }

    private fun calculateGestationalAgeFromLmp(lmpMillis: Long): Int {
        val currentMillis = System.currentTimeMillis()
        val diffMillis = currentMillis - lmpMillis
        val weeks = (diffMillis / (7 * 24 * 60 * 60 * 1000)).toInt()
        return weeks.coerceIn(4, 42) // Reasonable range
    }

    private fun updateProgress() {
        val totalRequired = 10 // Required fields
        var completed = 0

        val currentState = _state.value

        if (currentState.lmpDate != null || currentState.gestationalAgeWeeks > 0) completed++
        if (currentState.gravidity > 0) completed++
        if (currentState.parity >= 0) completed++ // Parity can be 0 (first pregnancy)
        if (currentState.prenatalVisits >= 0) completed++
        if (currentState.lastPrenatalVisit != null) completed++
        if (currentState.takingIronSupplements != null) completed++
        if (currentState.takingFolicAcid != null) completed++
        if (currentState.takingCalcium != null) completed++
        if (currentState.multiplePregnancy != null) completed++
        if (currentState.gestationalDiabetes != null) completed++

        val progress = completed.toFloat() / totalRequired
        val canSubmit = completed >= totalRequired

        _state.update {
            it.copy(
                progress = progress,
                canSubmit = canSubmit
            )
        }
    }

    fun submitSurvey() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            try {
                val currentState = _state.value

                // Create Maternal survey response
                val response = MaternalSurveyResponse(
                    lmpDate = currentState.lmpDate,
                    gestationalAgeWeeks = currentState.gestationalAgeWeeks,
                    gravidity = currentState.gravidity,
                    parity = currentState.parity,
                    abortions = currentState.abortions,
                    previousComplications = currentState.previousComplications,
                    currentSymptoms = currentState.currentSymptoms,
                    prenatalVisits = currentState.prenatalVisits,
                    lastPrenatalVisit = currentState.lastPrenatalVisit,
                    takingIronSupplements = currentState.takingIronSupplements,
                    takingFolicAcid = currentState.takingFolicAcid,
                    takingCalcium = currentState.takingCalcium,
                    tetanusToxoidDoses = currentState.tetanusToxoidDoses,
                    multiplePregnancy = currentState.multiplePregnancy,
                    gestationalDiabetes = currentState.gestationalDiabetes,
                    preeclampsia = currentState.preeclampsia,
                    maternalAge = null // Will be calculated from patient DOB
                )

                // TODO: Save survey to database via repository
                // For now, just mark as complete
                _state.update {
                    it.copy(
                        isSaving = false,
                        isComplete = true
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save survey"
                    )
                }
            }
        }
    }
}

/**
 * Maternal Survey State
 */
data class MaternalSurveyState(
    // Pregnancy Info
    val lmpDate: Long? = null,
    val gestationalAgeWeeks: Int = 0,
    val gravidity: Int = 0,
    val parity: Int = 0,
    val abortions: Int = 0,

    // Complications & Symptoms
    val previousComplications: List<MaternalComplication> = emptyList(),
    val currentSymptoms: List<MaternalSymptom> = emptyList(),

    // Prenatal Care
    val prenatalVisits: Int = 0,
    val lastPrenatalVisit: Long? = null,

    // Supplementation
    val takingIronSupplements: Boolean? = null,
    val takingFolicAcid: Boolean? = null,
    val takingCalcium: Boolean? = null,

    // Immunization
    val tetanusToxoidDoses: Int = 0,

    // Risk Factors
    val multiplePregnancy: Boolean? = null,
    val gestationalDiabetes: Boolean? = null,
    val preeclampsia: Boolean? = null,

    // UI State
    val progress: Float = 0f,
    val canSubmit: Boolean = false,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)
