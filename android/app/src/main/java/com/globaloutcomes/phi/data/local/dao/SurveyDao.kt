package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.SurveyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(survey: SurveyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(surveys: List<SurveyEntity>)

    @Query("SELECT * FROM surveys WHERE id = :surveyId")
    suspend fun getById(surveyId: String): SurveyEntity?

    @Query("SELECT * FROM surveys WHERE scanId = :scanId")
    fun getByScanFlow(scanId: String): Flow<List<SurveyEntity>>

    @Query("SELECT * FROM surveys WHERE patientId = :patientId ORDER BY completedAt DESC")
    fun getByPatientFlow(patientId: String): Flow<List<SurveyEntity>>

    @Query("SELECT * FROM surveys WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: String): List<SurveyEntity>

    @Query("UPDATE surveys SET syncStatus = :status, syncedAt = :syncedAt WHERE id = :surveyId")
    suspend fun updateSyncStatus(surveyId: String, status: String, syncedAt: Long)

    @Query("DELETE FROM surveys")
    suspend fun deleteAll()
}
