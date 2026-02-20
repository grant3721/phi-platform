package com.globaloutcomes.phi.presentation.survey

sealed class SurveyEvent {
    data class ResponseChanged(val questionId: String, val answer: Any) : SurveyEvent()
    object NextSection : SurveyEvent()
    object PreviousSection : SurveyEvent()
    object SubmitSurvey : SurveyEvent()
    object ClearError : SurveyEvent()
}
