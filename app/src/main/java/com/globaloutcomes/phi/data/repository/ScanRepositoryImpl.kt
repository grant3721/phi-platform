package com.globaloutcomes.phi.data.repository

import com.globaloutcomes.phi.data.local.dao.ScanDao
import com.globaloutcomes.phi.data.local.entities.ScanEntity
import com.globaloutcomes.phi.domain.model.Biomarkers
import com.globaloutcomes.phi.domain.model.Scan
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Scan Repository Implementation
 */
class ScanRepositoryImpl @Inject constructor(
    private val scanDao: ScanDao
) : ScanRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun insert(scan: Scan): Result<Long> = runCatching {
        scanDao.insert(scan.toEntity())
    }

    override suspend fun update(scan: Scan): Result<Unit> = runCatching {
        scanDao.update(scan.toEntity())
    }

    override suspend fun getById(scanId: String): Result<Scan?> = runCatching {
        scanDao.getById(scanId)?.toDomain()
    }

    override fun getScansForPatient(patientId: String): Flow<List<Scan>> {
        return scanDao.getScansForPatient(patientId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLatestScan(patientId: String): Result<Scan?> = runCatching {
        scanDao.getLatestScan(patientId)?.toDomain()
    }

    override fun getTodayValidatedCount(bhwId: String): Flow<Int> {
        return scanDao.getTodayValidatedCount(bhwId)
    }

    override suspend fun getTodayValidatedCountSync(bhwId: String): Result<Int> = runCatching {
        scanDao.getTodayValidatedCountSync(bhwId)
    }

    override fun getTotalValidatedScans(): Flow<Int> {
        return scanDao.getTotalValidatedScans()
    }

    override suspend fun updateSyncStatus(scanId: String, syncStatus: String): Result<Unit> = runCatching {
        scanDao.updateSyncStatus(scanId, syncStatus)
    }

    // Mappers
    private fun ScanEntity.toDomain(): Scan {
        return Scan(
            id = id,
            patientId = patientId,
            bhwId = bhwId,
            scanType = scanType,
            validated = validated,
            rejectionReason = rejectionReason,
            biomarkers = Biomarkers(
                pulseRate = pulseRate,
                bpSystolic = bpSystolic,
                bpDiastolic = bpDiastolic,
                meanArterialPressure = meanArterialPressure,
                pulsePressure = pulsePressure,
                cardiacWorkload = cardiacWorkload,
                heartAge = heartAge,
                respirationRate = respirationRate,
                oxygenSaturation = oxygenSaturation,
                hemoglobin = hemoglobin,
                hemoglobinA1c = hemoglobinA1c,
                ascvdRisk = ascvdRisk,
                ascvdRiskLevel = ascvdRiskLevel,
                highBloodPressureRisk = highBloodPressureRisk,
                highFastingGlucoseRisk = highFastingGlucoseRisk,
                highHemoglobinA1cRisk = highHemoglobinA1cRisk,
                highTotalCholesterolRisk = highTotalCholesterolRisk,
                lowHemoglobinRisk = lowHemoglobinRisk,
                meanRri = meanRri,
                rri = rri,
                sdnn = sdnn,
                rmssd = rmssd,
                sd1 = sd1,
                sd2 = sd2,
                prq = prq,
                lfhf = lfhf,
                pnsIndex = pnsIndex,
                pnsZone = pnsZone,
                snsIndex = snsIndex,
                snsZone = snsZone,
                stressLevel = stressLevel,
                stressIndex = stressIndex,
                normalizedStressIndex = normalizedStressIndex,
                wellnessIndex = wellnessIndex,
                wellnessLevel = wellnessLevel
            ),
            riskFlags = riskFlags?.let {
                try {
                    json.decodeFromString<List<String>>(it)
                } catch (e: Exception) {
                    emptyList()
                }
            },
            overallRiskScore = overallRiskScore,
            scanDurationMs = scanDurationMs,
            signalQuality = signalQuality,
            gpsLat = gpsLat,
            gpsLon = gpsLon,
            deviceId = deviceId,
            appVersion = appVersion,
            scannedAt = scannedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            syncStatus = when (syncStatus) {
                "SYNCED" -> SyncStatus.SYNCED
                "FAILED" -> SyncStatus.FAILED
                else -> SyncStatus.PENDING
            }
        )
    }

    private fun Scan.toEntity(): ScanEntity {
        return ScanEntity(
            id = id,
            patientId = patientId,
            bhwId = bhwId,
            scanType = scanType,
            validated = validated,
            rejectionReason = rejectionReason,
            pulseRate = biomarkers.pulseRate,
            bpSystolic = biomarkers.bpSystolic,
            bpDiastolic = biomarkers.bpDiastolic,
            meanArterialPressure = biomarkers.meanArterialPressure,
            pulsePressure = biomarkers.pulsePressure,
            cardiacWorkload = biomarkers.cardiacWorkload,
            heartAge = biomarkers.heartAge,
            respirationRate = biomarkers.respirationRate,
            oxygenSaturation = biomarkers.oxygenSaturation,
            hemoglobin = biomarkers.hemoglobin,
            hemoglobinA1c = biomarkers.hemoglobinA1c,
            ascvdRisk = biomarkers.ascvdRisk,
            ascvdRiskLevel = biomarkers.ascvdRiskLevel,
            highBloodPressureRisk = biomarkers.highBloodPressureRisk,
            highFastingGlucoseRisk = biomarkers.highFastingGlucoseRisk,
            highHemoglobinA1cRisk = biomarkers.highHemoglobinA1cRisk,
            highTotalCholesterolRisk = biomarkers.highTotalCholesterolRisk,
            lowHemoglobinRisk = biomarkers.lowHemoglobinRisk,
            meanRri = biomarkers.meanRri,
            rri = biomarkers.rri,
            sdnn = biomarkers.sdnn,
            rmssd = biomarkers.rmssd,
            sd1 = biomarkers.sd1,
            sd2 = biomarkers.sd2,
            prq = biomarkers.prq,
            lfhf = biomarkers.lfhf,
            pnsIndex = biomarkers.pnsIndex,
            pnsZone = biomarkers.pnsZone,
            snsIndex = biomarkers.snsIndex,
            snsZone = biomarkers.snsZone,
            stressLevel = biomarkers.stressLevel,
            stressIndex = biomarkers.stressIndex,
            normalizedStressIndex = biomarkers.normalizedStressIndex,
            wellnessIndex = biomarkers.wellnessIndex,
            wellnessLevel = biomarkers.wellnessLevel,
            riskFlags = riskFlags?.let { json.encodeToString(it) },
            overallRiskScore = overallRiskScore,
            scanDurationMs = scanDurationMs,
            signalQuality = signalQuality,
            gpsLat = gpsLat,
            gpsLon = gpsLon,
            deviceId = deviceId,
            appVersion = appVersion,
            scannedAt = scannedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            syncStatus = syncStatus.name
        )
    }
}
