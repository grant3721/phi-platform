package com.globaloutcomes.phi.data.repository

import com.globaloutcomes.phi.data.local.dao.ScanDao
import com.globaloutcomes.phi.data.local.entities.ScanEntity
import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ScanRepositoryImpl @Inject constructor(
    private val scanDao: ScanDao
) : ScanRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun insertScan(scan: Scan): Result<String> = runCatching {
        scanDao.insert(scan.toEntity())
        scan.id
    }

    override suspend fun getScanById(scanId: String): Result<Scan?> = runCatching {
        scanDao.getById(scanId)?.toDomain()
    }

    override fun getScanByIdFlow(scanId: String): Flow<Scan?> {
        return scanDao.getByIdFlow(scanId).map { it?.toDomain() }
    }

    override fun getScansByPatientFlow(patientId: String): Flow<List<Scan>> {
        return scanDao.getByPatientFlow(patientId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getScansForPatient(patientId: String): Result<List<Scan>> = runCatching {
        scanDao.getByPatient(patientId).map { it.toDomain() }
    }

    override suspend fun getLatestScanForPatient(patientId: String): Result<Scan?> = runCatching {
        scanDao.getLatestForPatient(patientId)?.toDomain()
    }

    override suspend fun getScansSince(patientId: String, since: Long): Result<List<Scan>> = runCatching {
        scanDao.getByPatientSince(patientId, since).map { it.toDomain() }
    }

    override suspend fun getScansForDay(startOfDay: Long, endOfDay: Long): Result<List<Scan>> = runCatching {
        scanDao.getScansForDay(startOfDay, endOfDay).map { it.toDomain() }
    }

    override suspend fun getScanCountForDay(startOfDay: Long, endOfDay: Long): Result<Int> = runCatching {
        scanDao.getCountForDay(startOfDay, endOfDay)
    }

    override fun getHighRiskScansFlow(): Flow<List<Scan>> {
        return scanDao.getHighRiskScansFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getScansBySyncStatus(status: SyncStatus): Result<List<Scan>> = runCatching {
        scanDao.getBySyncStatus(status.name).map { it.toDomain() }
    }

    override suspend fun updateSyncStatus(scanId: String, status: SyncStatus, syncedAt: Long): Result<Unit> = runCatching {
        scanDao.updateSyncStatus(scanId, status.name, syncedAt, null)
    }

    override suspend fun deleteScan(scanId: String): Result<Unit> = runCatching {
        scanDao.getById(scanId)?.let { scanDao.delete(it) }
    }

    // Mapping functions
    private fun Scan.toEntity() = ScanEntity(
        id = id,
        patientId = patientId,
        scannedAt = scannedAt,
        scanDurationSeconds = scanDurationSeconds,
        systolicBp = systolicBp,
        diastolicBp = diastolicBp,
        heartRate = heartRate,
        heartRateVariability = heartRateVariability,
        cardiacOutput = cardiacOutput,
        strokeVolume = strokeVolume,
        respiratoryRate = respiratoryRate,
        spo2 = spo2,
        perfusionIndex = perfusionIndex,
        bloodGlucose = bloodGlucose,
        hba1c = hba1c,
        cholesterol = cholesterol,
        triglycerides = triglycerides,
        hemoglobin = hemoglobin,
        hematocrit = hematocrit,
        bun = bun,
        creatinine = creatinine,
        arterialStiffness = arterialStiffness,
        vascularAge = vascularAge,
        peripheralResistance = peripheralResistance,
        meanArterialPressure = meanArterialPressure,
        sympatheticTone = sympatheticTone,
        parasympatheticTone = parasympatheticTone,
        stressIndex = stressIndex,
        bmi = bmi,
        bodyFatPercentage = bodyFatPercentage,
        visceralFatLevel = visceralFatLevel,
        leftVentricularEjectionFraction = leftVentricularEjectionFraction,
        systolicTimeIntervals = systolicTimeIntervals,
        diastolicFunction = diastolicFunction,
        qrsDuration = qrsDuration,
        lungCapacity = lungCapacity,
        respiratoryEfficiency = respiratoryEfficiency,
        oxygenSaturationVariability = oxygenSaturationVariability,
        riskScore = riskScore,
        riskLevel = riskLevel.name,
        highRiskFlags = if (highRiskFlags.isNotEmpty()) json.encodeToString(highRiskFlags) else null,
        signalQuality = signalQuality.name,
        qualityScore = qualityScore,
        rejectionReason = rejectionReason,
        biomarkersFull = json.encodeToString(biomarkersFull),
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus.name,
        syncedAt = syncedAt,
        serverScanId = serverScanId
    )

    private fun ScanEntity.toDomain() = Scan(
        id = id,
        patientId = patientId,
        scannedAt = scannedAt,
        scanDurationSeconds = scanDurationSeconds,
        systolicBp = systolicBp,
        diastolicBp = diastolicBp,
        heartRate = heartRate,
        heartRateVariability = heartRateVariability,
        cardiacOutput = cardiacOutput,
        strokeVolume = strokeVolume,
        respiratoryRate = respiratoryRate,
        spo2 = spo2,
        perfusionIndex = perfusionIndex,
        bloodGlucose = bloodGlucose,
        hba1c = hba1c,
        cholesterol = cholesterol,
        triglycerides = triglycerides,
        hemoglobin = hemoglobin,
        hematocrit = hematocrit,
        bun = bun,
        creatinine = creatinine,
        arterialStiffness = arterialStiffness,
        vascularAge = vascularAge,
        peripheralResistance = peripheralResistance,
        meanArterialPressure = meanArterialPressure,
        sympatheticTone = sympatheticTone,
        parasympatheticTone = parasympatheticTone,
        stressIndex = stressIndex,
        bmi = bmi,
        bodyFatPercentage = bodyFatPercentage,
        visceralFatLevel = visceralFatLevel,
        leftVentricularEjectionFraction = leftVentricularEjectionFraction,
        systolicTimeIntervals = systolicTimeIntervals,
        diastolicFunction = diastolicFunction,
        qrsDuration = qrsDuration,
        lungCapacity = lungCapacity,
        respiratoryEfficiency = respiratoryEfficiency,
        oxygenSaturationVariability = oxygenSaturationVariability,
        riskScore = riskScore,
        riskLevel = RiskLevel.valueOf(riskLevel),
        highRiskFlags = highRiskFlags?.let {
            try {
                json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList(),
        signalQuality = SignalQuality.valueOf(signalQuality),
        qualityScore = qualityScore,
        rejectionReason = rejectionReason,
        biomarkersFull = try {
            json.decodeFromString<Map<String, Any>>(biomarkersFull)
        } catch (e: Exception) {
            emptyMap()
        },
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.valueOf(syncStatus),
        syncedAt = syncedAt,
        serverScanId = serverScanId
    )
}
