package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.PatientEntity
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM patients WHERE id = :patientId")
    suspend fun getById(patientId: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE id = :patientId")
    fun getByIdFlow(patientId: String): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getByPhoneNumber(phoneNumber: String): PatientEntity?

    @Query("SELECT * FROM patients ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE highRiskFlag = 1 ORDER BY lastRiskAssessment DESC")
    fun getHighRiskPatientsFlow(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE isPregnant = 1 ORDER BY gestationalAgeWeeks DESC")
    fun getPregnantPatientsFlow(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE barangayId = :barangayId ORDER BY lastName, firstName")
    fun getByBarangayFlow(barangayId: String): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: String): List<PatientEntity>

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM patients WHERE highRiskFlag = 1")
    suspend fun getHighRiskCount(): Int

    @Query("UPDATE patients SET syncStatus = :status, syncedAt = :syncedAt, serverPatientId = :serverId WHERE id = :patientId")
    suspend fun updateSyncStatus(patientId: String, status: String, syncedAt: Long, serverId: String?)

    @Query("UPDATE patients SET highRiskFlag = :flag, highRiskReasons = :reasons, lastRiskAssessment = :timestamp WHERE id = :patientId")
    suspend fun updateRiskStatus(patientId: String, flag: Boolean, reasons: String?, timestamp: Long)

    @Query("DELETE FROM patients WHERE id IN (:patientIds)")
    suspend fun deleteByIds(patientIds: List<String>)

    @Query("DELETE FROM patients")
    suspend fun deleteAll()
}
