package com.globaloutcomes.phi.presentation.survey

import com.globaloutcomes.phi.domain.model.SurveyType

data class SurveyState(
    val patientId: String = "",
    val scanId: String? = null,
    val surveyType: SurveyType = SurveyType.NCD,
    val responses: Map<String, Any> = emptyMap(),
    val currentSection: Int = 0,
    val totalSections: Int = 5,
    val validationErrors: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
    val errorMessage: String? = null,
    val savedSurveyId: String? = null
) {
    val canProceed: Boolean
        get() = validationErrors.isEmpty() && responses.isNotEmpty()
}
