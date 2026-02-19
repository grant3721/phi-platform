package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.PatientEntity
import kotlinx.coroutines.flow.Flow

/**
 * Patient Data Access Object
 * Provides database operations for patient records
 */
@Dao
interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: PatientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<PatientEntity>)

    @Update
    suspend fun update(patient: PatientEntity)

    @Delete
    suspend fun delete(patient: PatientEntity)

    @Query("SELECT * FROM patients WHERE id = :patientId LIMIT 1")
    suspend fun getById(patientId: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE id = :patientId LIMIT 1")
    fun getByIdFlow(patientId: String): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE phoneNumber = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE highRiskFlag = 1")
    fun getHighRiskPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE pregnant = 1")
    fun getPregnantPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE pregnant = 1 AND maternalHighRisk = 1")
    fun getMaternalHighRiskPatients(): Flow<List<PatientEntity>>

    @Query("SELECT COUNT(*) FROM patients")
    fun getTotalPatientCount(): Flow<Int>

    @Query("SELECT * FROM patients ORDER BY createdAt DESC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE syncStatus = :status")
    fun getPatientsBySync Status(status: String): Flow<List<PatientEntity>>

    @Query("UPDATE patients SET syncStatus = :status WHERE id = :patientId")
    suspend fun updateSyncStatus(patientId: String, status: String)

    @Query("SELECT * FROM patients WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%'")
    fun searchPatients(query: String): Flow<List<PatientEntity>>
}
