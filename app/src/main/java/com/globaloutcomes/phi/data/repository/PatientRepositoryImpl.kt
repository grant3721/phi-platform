package com.globaloutcomes.phi.data.repository

import com.globaloutcomes.phi.data.local.dao.PatientDao
import com.globaloutcomes.phi.data.local.entities.PatientEntity
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.PatientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Patient Repository Implementation
 * Maps between PatientEntity (data layer) and Patient (domain layer)
 */
class PatientRepositoryImpl @Inject constructor(
    private val patientDao: PatientDao
) : PatientRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun insert(patient: Patient): Result<Long> = runCatching {
        patientDao.insert(patient.toEntity())
    }

    override suspend fun update(patient: Patient): Result<Unit> = runCatching {
        patientDao.update(patient.toEntity())
    }

    override suspend fun getById(patientId: String): Result<Patient?> = runCatching {
        patientDao.getById(patientId)?.toDomain()
    }

    override fun getByIdFlow(patientId: String): Flow<Patient?> {
        return patientDao.getByIdFlow(patientId).map { it?.toDomain() }
    }

    override suspend fun findByPhone(phoneNumber: String): Result<Patient?> = runCatching {
        patientDao.findByPhone(phoneNumber)?.toDomain()
    }

    override fun getAllPatients(): Flow<List<Patient>> {
        return patientDao.getAllPatients().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHighRiskPatients(): Flow<List<Patient>> {
        return patientDao.getHighRiskPatients().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPregnantPatients(): Flow<List<Patient>> {
        return patientDao.getPregnantPatients().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchPatients(query: String): Flow<List<Patient>> {
        return patientDao.searchPatients(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateSyncStatus(patientId: String, syncStatus: String): Result<Unit> = runCatching {
        patientDao.updateSyncStatus(patientId, syncStatus)
    }

    override fun getTotalPatientCount(): Flow<Int> {
        return patientDao.getTotalPatientCount()
    }

    // Mappers
    private fun PatientEntity.toDomain(): Patient {
        return Patient(
            id = id,
            phoneNumber = phoneNumber,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            sex = sex,
            barangayCode = barangayCode,
            municipalityCode = municipalityCode,
            philhealthNumber = philhealthNumber,
            philsysId = philsysId,
            pregnant = pregnant,
            gestationalAgeWeeks = gestationalAgeWeeks,
            highRiskFlag = highRiskFlag,
            highRiskReasons = highRiskReasons?.let {
                try {
                    json.decodeFromString<List<String>>(it)
                } catch (e: Exception) {
                    emptyList()
                }
            },
            maternalHighRisk = maternalHighRisk,
            lastScanDate = lastScanDate,
            totalScans = totalScans,
            messengerOptIn = messengerOptIn,
            messengerContactMethod = messengerContactMethod,
            messengerContactValue = messengerContactValue,
            messengerPsid = messengerPsid,
            createdAt = createdAt,
            updatedAt = updatedAt,
            syncStatus = when (syncStatus) {
                "SYNCED" -> SyncStatus.SYNCED
                "FAILED" -> SyncStatus.FAILED
                else -> SyncStatus.PENDING
            }
        )
    }

    private fun Patient.toEntity(): PatientEntity {
        return PatientEntity(
            id = id,
            phoneNumber = phoneNumber,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            sex = sex,
            barangayCode = barangayCode,
            municipalityCode = municipalityCode,
            philhealthNumber = philhealthNumber,
            philsysId = philsysId,
            pregnant = pregnant,
            gestationalAgeWeeks = gestationalAgeWeeks,
            highRiskFlag = highRiskFlag,
            highRiskReasons = highRiskReasons?.let { json.encodeToString(it) },
            maternalHighRisk = maternalHighRisk,
            lastScanDate = lastScanDate,
            totalScans = totalScans,
            messengerOptIn = messengerOptIn,
            messengerContactMethod = messengerContactMethod,
            messengerContactValue = messengerContactValue,
            messengerPsid = messengerPsid,
            createdAt = createdAt,
            updatedAt = updatedAt,
            syncStatus = syncStatus.name
        )
    }
}
