package com.globaloutcomes.phi.presentation.survey.mental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.MentalHealthSurveyResponse
import com.globaloutcomes.phi.domain.model.PhqSeverity
import com.globaloutcomes.phi.domain.model.Survey
import com.globaloutcomes.phi.domain.model.SurveyType
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.ScanRepository
import com.globaloutcomes.phi.domain.repository.SurveyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Mental Health Survey ViewModel (PHQ-9)
 * Patient Health Questionnaire for depression screening
 */
@HiltViewModel
class MentalHealthSurveyViewModel @Inject constructor(
    private val surveyRepository: SurveyRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MentalHealthSurveyState())
    val state: StateFlow<MentalHealthSurveyState> = _state.asStateFlow()

    fun initSurvey(scanId: String) {
        _state.update { it.copy(scanId = scanId) }
        loadScan(scanId)
    }

    private fun loadScan(scanId: String) {
        viewModelScope.launch {
            scanRepository.getById(scanId)
                .onSuccess { scan ->
                    _state.update {
                        it.copy(
                            patientId = scan?.patientId ?: "",
                            isLoading = false
                        )
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

    // PHQ-9 Question Updates
    fun updateLittleInterest(score: Int) {
        _state.update { it.copy(littleInterest = score) }
        recalculateScore()
    }

    fun updateFeelingDown(score: Int) {
        _state.update { it.copy(feelingDown = score) }
        recalculateScore()
    }

    fun updateTroubleSleeping(score: Int) {
        _state.update { it.copy(troubleSleeping = score) }
        recalculateScore()
    }

    fun updateFeelingTired(score: Int) {
        _state.update { it.copy(feelingTired = score) }
        recalculateScore()
    }

    fun updatePoorAppetite(score: Int) {
        _state.update { it.copy(poorAppetite = score) }
        recalculateScore()
    }

    fun updateFeelingBad(score: Int) {
        _state.update { it.copy(feelingBad = score) }
        recalculateScore()
    }

    fun updateTroubleConcentrating(score: Int) {
        _state.update { it.copy(troubleConcentrating = score) }
        recalculateScore()
    }

    fun updateMovingSlow(score: Int) {
        _state.update { it.copy(movingSlow = score) }
        recalculateScore()
    }

    fun updateThoughtsHurting(score: Int) {
        _state.update { it.copy(thoughtsHurting = score) }
        recalculateScore()
    }

    private fun recalculateScore() {
        val current = _state.value

        // Count answered questions
        val scores = listOf(
            current.littleInterest,
            current.feelingDown,
            current.troubleSleeping,
            current.feelingTired,
            current.poorAppetite,
            current.feelingBad,
            current.troubleConcentrating,
            current.movingSlow,
            current.thoughtsHurting
        )

        val answeredCount = scores.count { it != null }
        val progress = answeredCount.toFloat() / 9f
        val canSubmit = answeredCount == 9

        // Calculate total score
        val totalScore = if (canSubmit) {
            scores.filterNotNull().sum()
        } else {
            null
        }

        // Determine severity level
        val severityLevel = totalScore?.let { calculateSeverity(it) }

        _state.update {
            it.copy(
                totalScore = totalScore,
                severityLevel = severityLevel,
                progress = progress,
                canSubmit = canSubmit
            )
        }
    }

    /**
     * Calculate PHQ-9 severity level based on total score
     * Score ranges:
     * - 0-4: Minimal depression
     * - 5-9: Mild depression
     * - 10-14: Moderate depression
     * - 15-19: Moderately severe depression
     * - 20-27: Severe depression
     */
    private fun calculateSeverity(totalScore: Int): PhqSeverity {
        return when {
            totalScore in 0..4 -> PhqSeverity.MINIMAL
            totalScore in 5..9 -> PhqSeverity.MILD
            totalScore in 10..14 -> PhqSeverity.MODERATE
            totalScore in 15..19 -> PhqSeverity.MODERATELY_SEVERE
            totalScore >= 20 -> PhqSeverity.SEVERE
            else -> PhqSeverity.MINIMAL
        }
    }

    fun submitSurvey() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            val current = _state.value

            if (!current.canSubmit) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = "Please answer all questions"
                    )
                }
                return@launch
            }

            val response = MentalHealthSurveyResponse(
                littleInterest = current.littleInterest,
                feelingDown = current.feelingDown,
                troubleSleeping = current.troubleSleeping,
                feelingTired = current.feelingTired,
                poorAppetite = current.poorAppetite,
                feelingBad = current.feelingBad,
                troubleConcentrating = current.troubleConcentrating,
                movingSlow = current.movingSlow,
                thoughtsHurting = current.thoughtsHurting,
                totalScore = current.totalScore,
                severityLevel = current.severityLevel
            )

            val survey = Survey(
                id = java.util.UUID.randomUUID().toString(),
                scanId = current.scanId,
                patientId = current.patientId,
                surveyType = SurveyType.MENTAL_HEALTH,
                responses = mapOf("mentalHealth" to response), // Will be serialized to JSON
                completedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING
            )

            surveyRepository.insert(survey)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            submitSuccess = true
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = error.message ?: "Failed to save survey"
                        )
                    }
                }
        }
    }
}

/**
 * Mental Health Survey State (PHQ-9)
 */
data class MentalHealthSurveyState(
    val scanId: String = "",
    val patientId: String = "",
    val isLoading: Boolean = true,

    // PHQ-9 Questions (0-3 scale)
    val littleInterest: Int? = null,
    val feelingDown: Int? = null,
    val troubleSleeping: Int? = null,
    val feelingTired: Int? = null,
    val poorAppetite: Int? = null,
    val feelingBad: Int? = null,
    val troubleConcentrating: Int? = null,
    val movingSlow: Int? = null,
    val thoughtsHurting: Int? = null,

    // Calculated Results
    val totalScore: Int? = null,
    val severityLevel: PhqSeverity? = null,

    // UI State
    val progress: Float = 0f,
    val canSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null
)
