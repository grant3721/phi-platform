package com.globaloutcomes.phi.domain.repository

import com.globaloutcomes.phi.domain.model.Patient
import kotlinx.coroutines.flow.Flow

/**
 * Patient Repository Interface - Clean Architecture
 * Defines contract for patient data operations
 */
interface PatientRepository {

    suspend fun insert(patient: Patient): Result<Long>

    suspend fun update(patient: Patient): Result<Unit>

    suspend fun getById(patientId: String): Result<Patient?>

    fun getByIdFlow(patientId: String): Flow<Patient?>

    suspend fun findByPhone(phoneNumber: String): Result<Patient?>

    fun getAllPatients(): Flow<List<Patient>>

    fun getHighRiskPatients(): Flow<List<Patient>>

    fun getPregnantPatients(): Flow<List<Patient>>

    fun searchPatients(query: String): Flow<List<Patient>>

    suspend fun updateSyncStatus(patientId: String, syncStatus: String): Result<Unit>

    fun getTotalPatientCount(): Flow<Int>
}
