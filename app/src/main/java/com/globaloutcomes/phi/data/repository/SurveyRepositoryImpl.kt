package com.globaloutcomes.phi.data.repository

import com.globaloutcomes.phi.data.local.dao.SurveyDao
import com.globaloutcomes.phi.data.local.entities.SurveyEntity
import com.globaloutcomes.phi.domain.model.Survey
import com.globaloutcomes.phi.domain.model.SurveyType
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.SurveyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Survey Repository Implementation
 * Maps between SurveyEntity and Survey domain model
 */
class SurveyRepositoryImpl @Inject constructor(
    private val surveyDao: SurveyDao,
    private val json: Json
) : SurveyRepository {

    override suspend fun insert(survey: Survey): Result<Unit> {
        return try {
            surveyDao.insert(survey.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun update(survey: Survey): Result<Unit> {
        return try {
            surveyDao.update(survey.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(surveyId: String): Result<Unit> {
        return try {
            surveyDao.deleteById(surveyId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getById(surveyId: String): Result<Survey?> {
        return try {
            val entity = surveyDao.getById(surveyId)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getByScanId(scanId: String): Result<List<Survey>> {
        return try {
            val entities = surveyDao.getByScanId(scanId)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getByPatientId(patientId: String): Result<List<Survey>> {
        return try {
            val entities = surveyDao.getByPatientId(patientId)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getByType(surveyType: SurveyType): Result<List<Survey>> {
        return try {
            val entities = surveyDao.getByType(surveyType.name)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAll(): Flow<Result<List<Survey>>> {
        return surveyDao.observeAll().map { entities ->
            try {
                Result.success(entities.map { it.toDomain() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun observeByScanId(scanId: String): Flow<Result<List<Survey>>> {
        return surveyDao.observeByScanId(scanId).map { entities ->
            try {
                Result.success(entities.map { it.toDomain() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAll(): Result<List<Survey>> {
        return try {
            val entities = surveyDao.getAll()
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Entity <-> Domain mapping
    private fun Survey.toEntity(): SurveyEntity {
        return SurveyEntity(
            id = id,
            scanId = scanId,
            patientId = patientId,
            surveyType = surveyType.name,
            responses = json.encodeToString(responses),
            completedAt = completedAt,
            syncStatus = syncStatus.name
        )
    }

    private fun SurveyEntity.toDomain(): Survey {
        return Survey(
            id = id,
            scanId = scanId,
            patientId = patientId,
            surveyType = SurveyType.valueOf(surveyType),
            responses = try {
                json.decodeFromString<Map<String, Any>>(responses)
            } catch (e: Exception) {
                emptyMap()
            },
            completedAt = completedAt,
            syncStatus = SyncStatus.valueOf(syncStatus)
        )
    }
}
