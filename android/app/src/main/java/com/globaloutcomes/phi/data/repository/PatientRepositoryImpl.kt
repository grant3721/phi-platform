package com.globaloutcomes.phi.data.repository

import com.globaloutcomes.phi.data.local.dao.PatientDao
import com.globaloutcomes.phi.data.local.entities.PatientEntity
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Sex
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.PatientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Implementation of PatientRepository
 * Maps between PatientEntity (data layer) and Patient (domain layer)
 */
class PatientRepositoryImpl @Inject constructor(
    private val patientDao: PatientDao
) : PatientRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun insertPatient(patient: Patient): Result<String> = runCatching {
        patientDao.insert(patient.toEntity())
        patient.id
    }

    override suspend fun updatePatient(patient: Patient): Result<Unit> = runCatching {
        patientDao.update(patient.toEntity())
    }

    override suspend fun getPatientById(patientId: String): Result<Patient?> = runCatching {
        patientDao.getById(patientId)?.toDomain()
    }

    override fun getPatientByIdFlow(patientId: String): Flow<Patient?> {
        return patientDao.getByIdFlow(patientId).map { it?.toDomain() }
    }

    override suspend fun getPatientByPhoneNumber(phoneNumber: String): Result<Patient?> = runCatching {
        patientDao.getByPhoneNumber(phoneNumber)?.toDomain()
    }

    override fun getAllPatientsFlow(): Flow<List<Patient>> {
        return patientDao.getAllFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHighRiskPatientsFlow(): Flow<List<Patient>> {
        return patientDao.getHighRiskPatientsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPregnantPatientsFlow(): Flow<List<Patient>> {
        return patientDao.getPregnantPatientsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPatientCount(): Result<Int> = runCatching {
        patientDao.getCount()
    }

    override suspend fun getHighRiskCount(): Result<Int> = runCatching {
        patientDao.getHighRiskCount()
    }

    override suspend fun updateRiskStatus(
        patientId: String,
        highRiskFlag: Boolean,
        highRiskReasons: List<String>,
        timestamp: Long
    ): Result<Unit> = runCatching {
        val reasonsJson = json.encodeToString(highRiskReasons)
        patientDao.updateRiskStatus(patientId, highRiskFlag, reasonsJson, timestamp)
    }

    override suspend fun getPatientsBySyncStatus(status: SyncStatus): Result<List<Patient>> = runCatching {
        patientDao.getBySyncStatus(status.name).map { it.toDomain() }
    }

    override suspend fun updateSyncStatus(patientId: String, status: SyncStatus, syncedAt: Long): Result<Unit> = runCatching {
        patientDao.updateSyncStatus(patientId, status.name, syncedAt, null)
    }

    override suspend fun deletePatient(patientId: String): Result<Unit> = runCatching {
        patientDao.getById(patientId)?.let { patientDao.delete(it) }
    }

    // Mapping functions
    private fun Patient.toEntity() = PatientEntity(
        id = id,
        phoneNumber = phoneNumber,
        firstName = firstName,
        lastName = lastName,
        birthdate = birthdate,
        sex = sex.name,
        barangayId = barangayId,
        philHealthNumber = philHealthNumber,
        philSysNumber = philSysNumber,
        isPregnant = isPregnant,
        gestationalAgeWeeks = gestationalAgeWeeks,
        lastMenstrualPeriod = lastMenstrualPeriod,
        messengerOptIn = messengerOptIn,
        messengerUserId = messengerUserId,
        messengerName = messengerName,
        highRiskFlag = highRiskFlag,
        highRiskReasons = if (highRiskReasons.isNotEmpty()) json.encodeToString(highRiskReasons) else null,
        maternalHighRisk = maternalHighRisk,
        lastRiskAssessment = lastRiskAssessment,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus.name,
        syncedAt = syncedAt,
        serverPatientId = serverPatientId
    )

    private fun PatientEntity.toDomain() = Patient(
        id = id,
        phoneNumber = phoneNumber,
        firstName = firstName,
        lastName = lastName,
        birthdate = birthdate,
        sex = Sex.valueOf(sex),
        barangayId = barangayId,
        philHealthNumber = philHealthNumber,
        philSysNumber = philSysNumber,
        isPregnant = isPregnant,
        gestationalAgeWeeks = gestationalAgeWeeks,
        lastMenstrualPeriod = lastMenstrualPeriod,
        messengerOptIn = messengerOptIn,
        messengerUserId = messengerUserId,
        messengerName = messengerName,
        highRiskFlag = highRiskFlag,
        highRiskReasons = highRiskReasons?.let {
            try {
                json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList(),
        maternalHighRisk = maternalHighRisk,
        lastRiskAssessment = lastRiskAssessment,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.valueOf(syncStatus),
        syncedAt = syncedAt,
        serverPatientId = serverPatientId
    )
}
