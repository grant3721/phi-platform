package com.globaloutcomes.phi.domain.repository

import com.globaloutcomes.phi.domain.model.Survey
import com.globaloutcomes.phi.domain.model.SyncStatus

/**
 * Survey Repository Interface
 */
interface SurveyRepository {

    suspend fun insertSurvey(survey: Survey): Result<String>

    suspend fun getSurveyById(surveyId: String): Result<Survey?>

    suspend fun getSurveysByScanId(scanId: String): Result<List<Survey>>

    suspend fun getSurveysByPatientId(patientId: String): Result<List<Survey>>

    suspend fun getSurveysBySyncStatus(status: SyncStatus): Result<List<Survey>>

    suspend fun updateSyncStatus(surveyId: String, status: SyncStatus, syncedAt: Long): Result<Unit>
}
