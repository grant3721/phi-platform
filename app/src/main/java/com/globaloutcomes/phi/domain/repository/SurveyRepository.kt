package com.globaloutcomes.phi.domain.repository

import com.globaloutcomes.phi.domain.model.Survey
import com.globaloutcomes.phi.domain.model.SurveyType
import kotlinx.coroutines.flow.Flow

/**
 * Survey Repository Interface
 */
interface SurveyRepository {

    suspend fun insert(survey: Survey): Result<Unit>

    suspend fun update(survey: Survey): Result<Unit>

    suspend fun delete(surveyId: String): Result<Unit>

    suspend fun getById(surveyId: String): Result<Survey?>

    suspend fun getByScanId(scanId: String): Result<List<Survey>>

    suspend fun getByPatientId(patientId: String): Result<List<Survey>>

    suspend fun getByType(surveyType: SurveyType): Result<List<Survey>>

    fun observeAll(): Flow<Result<List<Survey>>>

    fun observeByScanId(scanId: String): Flow<Result<List<Survey>>>

    suspend fun getAll(): Result<List<Survey>>
}
