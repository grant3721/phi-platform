package com.globaloutcomes.phi.domain.model

data class Survey(
    val id: String,
    val patientId: String,
    val scanId: String?,
    val surveyType: SurveyType,
    val responses: Map<String, Any>,
    val completedAt: Long,
    val durationSeconds: Int = 0,
    val isComplete: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val syncedAt: Long? = null,
    val serverSurveyId: String? = null
)

enum class SurveyType {
    NCD,
    MATERNAL,
    INFECTIOUS_DISEASE,
    MENTAL_HEALTH
}
