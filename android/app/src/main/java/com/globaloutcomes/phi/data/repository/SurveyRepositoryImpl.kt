package com.globaloutcomes.phi.data.repository

import com.globaloutcomes.phi.data.local.dao.SurveyDao
import com.globaloutcomes.phi.data.local.entities.SurveyEntity
import com.globaloutcomes.phi.domain.model.Survey
import com.globaloutcomes.phi.domain.model.SurveyType
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.SurveyRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Survey Repository Implementation
 */
class SurveyRepositoryImpl @Inject constructor(
    private val surveyDao: SurveyDao
) : SurveyRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun insertSurvey(survey: Survey): Result<String> = runCatching {
        surveyDao.insert(survey.toEntity())
        survey.id
    }

    override suspend fun getSurveyById(surveyId: String): Result<Survey?> = runCatching {
        surveyDao.getById(surveyId)?.toDomain()
    }

    override suspend fun getSurveysByScanId(scanId: String): Result<List<Survey>> = runCatching {
        surveyDao.getByScanFlow(scanId).let { emptyList<Survey>() } // TODO: Use suspend function from dao
    }

    override suspend fun getSurveysByPatientId(patientId: String): Result<List<Survey>> = runCatching {
        surveyDao.getByPatientFlow(patientId).let { emptyList<Survey>() } // TODO: Use suspend function from dao
    }

    override suspend fun getSurveysBySyncStatus(status: SyncStatus): Result<List<Survey>> = runCatching {
        surveyDao.getBySyncStatus(status.name).map { it.toDomain() }
    }

    override suspend fun updateSyncStatus(surveyId: String, status: SyncStatus, syncedAt: Long): Result<Unit> = runCatching {
        surveyDao.updateSyncStatus(surveyId, status.name, syncedAt)
    }

    // Mapping functions
    private fun Survey.toEntity() = SurveyEntity(
        id = id,
        patientId = patientId,
        scanId = scanId,
        surveyType = surveyType.name,
        completedAt = completedAt,
        durationSeconds = durationSeconds,
        isComplete = isComplete,
        responses = json.encodeToString(responses),
        syncStatus = syncStatus.name,
        syncedAt = syncedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun SurveyEntity.toDomain() = Survey(
        id = id,
        patientId = patientId,
        scanId = scanId,
        surveyType = SurveyType.valueOf(surveyType),
        completedAt = completedAt,
        durationSeconds = durationSeconds,
        isComplete = isComplete,
        responses = try {
            json.decodeFromString<Map<String, Any>>(responses)
        } catch (e: Exception) {
            emptyMap()
        },
        syncStatus = SyncStatus.valueOf(syncStatus),
        syncedAt = syncedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
