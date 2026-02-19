package com.globaloutcomes.phi.presentation.survey.ncd

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
 * NCD Survey ViewModel
 * Manages state for Non-Communicable Disease survey
 */
@HiltViewModel
class NcdSurveyViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scanId: String = checkNotNull(savedStateHandle["scanId"])

    private val _state = MutableStateFlow(NcdSurveyState())
    val state: StateFlow<NcdSurveyState> = _state.asStateFlow()

    // Diabetes
    fun onHasDiabetesChanged(value: Boolean?) {
        _state.update { it.copy(hasDiabetes = value) }
        updateProgress()
    }

    fun onDiabetesFamilyHistoryChanged(value: Boolean?) {
        _state.update { it.copy(diabetesFamilyHistory = value) }
        updateProgress()
    }

    fun onFrequentUrinationChanged(value: Boolean?) {
        _state.update { it.copy(frequentUrination = value) }
        updateProgress()
    }

    fun onExcessiveThirstChanged(value: Boolean?) {
        _state.update { it.copy(excessiveThirst = value) }
        updateProgress()
    }

    fun onUnexplainedWeightLossChanged(value: Boolean?) {
        _state.update { it.copy(unexplainedWeightLoss = value) }
        updateProgress()
    }

    // Hypertension
    fun onHasHypertensionChanged(value: Boolean?) {
        _state.update { it.copy(hasHypertension = value) }
        updateProgress()
    }

    fun onHypertensionFamilyHistoryChanged(value: Boolean?) {
        _state.update { it.copy(hypertensionFamilyHistory = value) }
        updateProgress()
    }

    fun onFrequentHeadachesChanged(value: Boolean?) {
        _state.update { it.copy(frequentHeadaches = value) }
        updateProgress()
    }

    fun onDizzinessChanged(value: Boolean?) {
        _state.update { it.copy(dizziness = value) }
        updateProgress()
    }

    // Cardiovascular
    fun onHeartDiseaseFamilyHistoryChanged(value: Boolean?) {
        _state.update { it.copy(heartDiseaseFamilyHistory = value) }
        updateProgress()
    }

    fun onChestPainChanged(value: Boolean?) {
        _state.update { it.copy(chestPain = value) }
        updateProgress()
    }

    fun onShortnessOfBreathChanged(value: Boolean?) {
        _state.update { it.copy(shortnessOfBreath = value) }
        updateProgress()
    }

    // Lifestyle
    fun onSmokingStatusChanged(status: SmokingStatus) {
        _state.update { it.copy(smokingStatus = status) }
        if (status != SmokingStatus.CURRENT) {
            _state.update { it.copy(cigarettesPerDay = 0) }
        }
        updateProgress()
    }

    fun onCigarettesPerDayChanged(count: Int) {
        _state.update { it.copy(cigarettesPerDay = count) }
    }

    fun onAlcoholConsumptionChanged(consumption: AlcoholConsumption) {
        _state.update { it.copy(alcoholConsumption = consumption) }
        updateProgress()
    }

    fun onPhysicalActivityChanged(level: Int) {
        _state.update { it.copy(physicalActivityLevel = level) }
    }

    fun onDietQualityChanged(quality: Int) {
        _state.update { it.copy(dietQuality = quality) }
    }

    // Medications
    fun onAddMedication(medication: String) {
        val currentMeds = _state.value.currentMedications.toMutableList()
        if (!currentMeds.contains(medication)) {
            currentMeds.add(medication)
            _state.update { it.copy(currentMedications = currentMeds) }
        }
    }

    fun onRemoveMedication(medication: String) {
        val currentMeds = _state.value.currentMedications.toMutableList()
        currentMeds.remove(medication)
        _state.update { it.copy(currentMedications = currentMeds) }
    }

    private fun updateProgress() {
        val totalQuestions = 16 // Core yes/no questions + lifestyle selections
        var answeredQuestions = 0

        val currentState = _state.value

        // Count answered questions
        if (currentState.hasDiabetes != null) answeredQuestions++
        if (currentState.diabetesFamilyHistory != null) answeredQuestions++
        if (currentState.frequentUrination != null) answeredQuestions++
        if (currentState.excessiveThirst != null) answeredQuestions++
        if (currentState.unexplainedWeightLoss != null) answeredQuestions++
        if (currentState.hasHypertension != null) answeredQuestions++
        if (currentState.hypertensionFamilyHistory != null) answeredQuestions++
        if (currentState.frequentHeadaches != null) answeredQuestions++
        if (currentState.dizziness != null) answeredQuestions++
        if (currentState.heartDiseaseFamilyHistory != null) answeredQuestions++
        if (currentState.chestPain != null) answeredQuestions++
        if (currentState.shortnessOfBreath != null) answeredQuestions++
        if (currentState.smokingStatus != null) answeredQuestions++
        if (currentState.alcoholConsumption != null) answeredQuestions++
        answeredQuestions++ // Physical activity always has default
        answeredQuestions++ // Diet quality always has default

        val progress = answeredQuestions.toFloat() / totalQuestions
        val canSubmit = answeredQuestions == totalQuestions

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

                // Create NCD survey response
                val response = NcdSurveyResponse(
                    hasDiabetes = currentState.hasDiabetes,
                    diabetesFamilyHistory = currentState.diabetesFamilyHistory,
                    frequentUrination = currentState.frequentUrination,
                    excessiveThirst = currentState.excessiveThirst,
                    unexplainedWeightLoss = currentState.unexplainedWeightLoss,
                    hasHypertension = currentState.hasHypertension,
                    hypertensionFamilyHistory = currentState.hypertensionFamilyHistory,
                    frequentHeadaches = currentState.frequentHeadaches,
                    dizziness = currentState.dizziness,
                    heartDiseaseFamilyHistory = currentState.heartDiseaseFamilyHistory,
                    chestPain = currentState.chestPain,
                    shortnessOfBreath = currentState.shortnessOfBreath,
                    smokingStatus = currentState.smokingStatus,
                    cigarettesPerDay = currentState.cigarettesPerDay,
                    alcoholConsumption = currentState.alcoholConsumption,
                    physicalActivityLevel = currentState.physicalActivityLevel,
                    dietQuality = currentState.dietQuality,
                    currentMedications = currentState.currentMedications
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
 * NCD Survey State
 */
data class NcdSurveyState(
    // Diabetes
    val hasDiabetes: Boolean? = null,
    val diabetesFamilyHistory: Boolean? = null,
    val frequentUrination: Boolean? = null,
    val excessiveThirst: Boolean? = null,
    val unexplainedWeightLoss: Boolean? = null,

    // Hypertension
    val hasHypertension: Boolean? = null,
    val hypertensionFamilyHistory: Boolean? = null,
    val frequentHeadaches: Boolean? = null,
    val dizziness: Boolean? = null,

    // Cardiovascular
    val heartDiseaseFamilyHistory: Boolean? = null,
    val chestPain: Boolean? = null,
    val shortnessOfBreath: Boolean? = null,

    // Lifestyle
    val smokingStatus: SmokingStatus? = null,
    val cigarettesPerDay: Int = 0,
    val alcoholConsumption: AlcoholConsumption? = null,
    val physicalActivityLevel: Int = 3, // Default: Moderate
    val dietQuality: Int = 3, // Default: Average

    // Medications
    val currentMedications: List<String> = emptyList(),

    // UI State
    val progress: Float = 0f,
    val canSubmit: Boolean = false,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)
