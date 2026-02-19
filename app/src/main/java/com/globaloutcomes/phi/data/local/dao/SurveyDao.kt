package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.SurveyEntity
import kotlinx.coroutines.flow.Flow

/**
 * Survey Data Access Object
 */
@Dao
interface SurveyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(survey: SurveyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(surveys: List<SurveyEntity>)

    @Update
    suspend fun update(survey: SurveyEntity)

    @Query("SELECT * FROM surveys WHERE id = :surveyId LIMIT 1")
    suspend fun getById(surveyId: String): SurveyEntity?

    @Query("SELECT * FROM surveys WHERE scanId = :scanId")
    fun getSurveysForScan(scanId: String): Flow<List<SurveyEntity>>

    @Query("SELECT * FROM surveys WHERE patientId = :patientId ORDER BY completedAt DESC")
    fun getSurveysForPatient(patientId: String): Flow<List<SurveyEntity>>

    @Query("SELECT * FROM surveys WHERE patientId = :patientId AND surveyType = :type ORDER BY completedAt DESC LIMIT 1")
    suspend fun getLatestSurveyByType(patientId: String, type: String): SurveyEntity?

    @Query("SELECT * FROM surveys WHERE syncStatus = :status")
    fun getSurveysBySyncStatus(status: String): Flow<List<SurveyEntity>>

    @Query("UPDATE surveys SET syncStatus = :status WHERE id = :surveyId")
    suspend fun updateSyncStatus(surveyId: String, status: String)
}
