package com.globaloutcomes.phi.presentation.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.model.SurveyType
import com.globaloutcomes.phi.domain.usecase.SaveSurveyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val saveSurveyUseCase: SaveSurveyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SurveyState())
    val state: StateFlow<SurveyState> = _state.asStateFlow()

    fun initialize(
        patientId: String,
        scanId: String?,
        surveyType: SurveyType,
        totalSections: Int
    ) {
        _state.value = _state.value.copy(
            patientId = patientId,
            scanId = scanId,
            surveyType = surveyType,
            totalSections = totalSections,
            currentSection = 0,
            responses = emptyMap()
        )
    }

    fun onEvent(event: SurveyEvent) {
        when (event) {
            is SurveyEvent.ResponseChanged -> {
                val updatedResponses = _state.value.responses.toMutableMap()
                updatedResponses[event.questionId] = event.answer
                _state.value = _state.value.copy(
                    responses = updatedResponses,
                    validationErrors = emptyMap()
                )
            }

            SurveyEvent.NextSection -> {
                if (_state.value.canProceed) {
                    _state.value = _state.value.copy(
                        currentSection = _state.value.currentSection + 1
                    )
                }
            }

            SurveyEvent.PreviousSection -> {
                if (_state.value.currentSection > 0) {
                    _state.value = _state.value.copy(
                        currentSection = _state.value.currentSection - 1
                    )
                }
            }

            SurveyEvent.SubmitSurvey -> {
                submitSurvey()
            }

            SurveyEvent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }
        }
    }

    private fun submitSurvey() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)

            val result = saveSurveyUseCase(
                patientId = _state.value.patientId,
                scanId = _state.value.scanId,
                surveyType = _state.value.surveyType,
                responses = _state.value.responses
            )

            result.onSuccess { surveyId ->
                _state.value = _state.value.copy(
                    isSaving = false,
                    isComplete = true,
                    savedSurveyId = surveyId
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save survey: ${error.message}"
                )
            }
        }
    }
}
