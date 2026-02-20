package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.model.Survey
import com.globaloutcomes.phi.domain.model.SurveyType
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.SurveyRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to save survey responses
 */
class SaveSurveyUseCase @Inject constructor(
    private val surveyRepository: SurveyRepository
) {
    suspend operator fun invoke(
        patientId: String,
        scanId: String?,
        surveyType: SurveyType,
        responses: Map<String, Any>
    ): Result<String> {
        return try {
            // Validate responses
            if (responses.isEmpty()) {
                return Result.failure(IllegalArgumentException("Survey responses cannot be empty"))
            }

            val currentTime = System.currentTimeMillis()

            val survey = Survey(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                scanId = scanId,
                surveyType = surveyType,
                completedAt = currentTime,
                responses = responses,
                createdAt = currentTime,
                updatedAt = currentTime,
                syncStatus = SyncStatus.PENDING,
                syncedAt = null,
                serverSurveyId = null
            )

            surveyRepository.insertSurvey(survey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
