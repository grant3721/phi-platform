package com.globaloutcomes.phi.domain.repository

import com.globaloutcomes.phi.domain.model.Patient
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Patient operations
 * Abstracts data source (Room, Remote API)
 */
interface PatientRepository {

    suspend fun insertPatient(patient: Patient): Result<String>

    suspend fun updatePatient(patient: Patient): Result<Unit>

    suspend fun getPatientById(patientId: String): Result<Patient?>

    fun getPatientByIdFlow(patientId: String): Flow<Patient?>

    suspend fun getPatientByPhoneNumber(phoneNumber: String): Result<Patient?>

    fun getAllPatientsFlow(): Flow<List<Patient>>

    fun getHighRiskPatientsFlow(): Flow<List<Patient>>

    fun getPregnantPatientsFlow(): Flow<List<Patient>>

    suspend fun getPatientCount(): Result<Int>

    suspend fun getHighRiskCount(): Result<Int>

    suspend fun updateRiskStatus(
        patientId: String,
        highRiskFlag: Boolean,
        highRiskReasons: List<String>,
        timestamp: Long
    ): Result<Unit>

    suspend fun getPatientsBySyncStatus(status: com.globaloutcomes.phi.domain.model.SyncStatus): Result<List<Patient>>

    suspend fun updateSyncStatus(patientId: String, status: com.globaloutcomes.phi.domain.model.SyncStatus, syncedAt: Long): Result<Unit>

    suspend fun deletePatient(patientId: String): Result<Unit>
}
