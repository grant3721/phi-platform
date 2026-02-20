package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.ClinicalEncounterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClinicalEncounterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(encounter: ClinicalEncounterEntity): Long

    @Query("SELECT * FROM clinical_encounters WHERE patientId = :patientId ORDER BY encounterDate DESC")
    fun getByPatientFlow(patientId: String): Flow<List<ClinicalEncounterEntity>>

    @Query("SELECT * FROM clinical_encounters WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: String): List<ClinicalEncounterEntity>

    @Query("DELETE FROM clinical_encounters")
    suspend fun deleteAll()
}
