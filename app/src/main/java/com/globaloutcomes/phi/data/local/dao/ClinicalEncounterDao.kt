package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.ClinicalEncounterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Clinical Encounter Data Access Object
 */
@Dao
interface ClinicalEncounterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(encounter: ClinicalEncounterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(encounters: List<ClinicalEncounterEntity>)

    @Update
    suspend fun update(encounter: ClinicalEncounterEntity)

    @Query("SELECT * FROM clinical_encounters WHERE id = :encounterId LIMIT 1")
    suspend fun getById(encounterId: String): ClinicalEncounterEntity?

    @Query("SELECT * FROM clinical_encounters WHERE patientId = :patientId ORDER BY encounterDate DESC")
    fun getEncountersForPatient(patientId: String): Flow<List<ClinicalEncounterEntity>>

    @Query("SELECT * FROM clinical_encounters WHERE patientId = :patientId ORDER BY encounterDate DESC LIMIT 1")
    suspend fun getLatestEncounter(patientId: String): ClinicalEncounterEntity?

    @Query("SELECT * FROM clinical_encounters WHERE encounterType = :type ORDER BY encounterDate DESC")
    fun getEncountersByType(type: String): Flow<List<ClinicalEncounterEntity>>

    @Query("SELECT * FROM clinical_encounters WHERE syncStatus = :status")
    fun getEncountersBySyncStatus(status: String): Flow<List<ClinicalEncounterEntity>>

    @Query("UPDATE clinical_encounters SET syncStatus = :status WHERE id = :encounterId")
    suspend fun updateSyncStatus(encounterId: String, status: String)
}
