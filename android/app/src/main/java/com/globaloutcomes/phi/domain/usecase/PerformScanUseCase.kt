package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.ScanRepository
import com.globaloutcomes.phi.domain.scan.ScanConfig
import com.globaloutcomes.phi.domain.scan.ScanEngine
import com.globaloutcomes.phi.domain.scan.ScanResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to perform a scan and save results
 */
class PerformScanUseCase @Inject constructor(
    private val scanEngine: ScanEngine,
    private val scanRepository: ScanRepository,
    private val createReferralUseCase: CreateReferralUseCase
) {
    /**
     * Performs a scan for the given patient
     * @param patientId Patient ID to scan
     * @return Flow of ScanProgress updates
     */
    operator fun invoke(patientId: String): Flow<ScanProgress> {
        val config = ScanConfig(
            patientId = patientId,
            targetDuration = 30,
            qualityThreshold = 0.7f
        )

        return scanEngine.performScan(config).map { result ->
            when (result) {
                is ScanResult.Progress -> {
                    ScanProgress.InProgress(
                        elapsedSeconds = result.elapsedSeconds,
                        totalSeconds = result.totalSeconds,
                        progress = result.elapsedSeconds.toFloat() / result.totalSeconds.toFloat(),
                        signalQuality = result.signalQuality,
                        guidance = result.guidance
                    )
                }

                is ScanResult.Success -> {
                    // Create Scan domain model
                    val scan = createScanFromBiomarkers(
                        patientId = patientId,
                        biomarkers = result.biomarkers,
                        duration = result.duration,
                        quality = result.quality
                    )

                    // Save to database
                    val saveResult = scanRepository.insertScan(scan)

                    if (saveResult.isSuccess) {
                        // Create referral if high-risk (async, don't block)
                        if (scan.riskLevel == RiskLevel.ELEVATED || scan.riskLevel == RiskLevel.HIGH) {
                            CoroutineScope(Dispatchers.IO).launch {
                                createReferralUseCase(
                                    patientId = scan.patientId,
                                    scanId = scan.id,
                                    riskLevel = scan.riskLevel,
                                    riskFlags = scan.highRiskFlags
                                )
                            }
                        }

                        ScanProgress.Complete(
                            scanId = saveResult.getOrNull() ?: scan.id,
                            scan = scan
                        )
                    } else {
                        ScanProgress.Failed(
                            reason = "Failed to save scan: ${saveResult.exceptionOrNull()?.message}",
                            code = com.globaloutcomes.phi.domain.scan.FailureCode.CAMERA_ERROR
                        )
                    }
                }

                is ScanResult.Failure -> {
                    ScanProgress.Failed(
                        reason = result.reason,
                        code = result.code
                    )
                }
            }
        }
    }

    /**
     * Cancels the current scan
     */
    suspend fun cancel() {
        scanEngine.cancelScan()
    }

    private fun createScanFromBiomarkers(
        patientId: String,
        biomarkers: Map<String, Float>,
        duration: Int,
        quality: Float
    ): Scan {
        val scanTimestamp = System.currentTimeMillis()

        // Determine risk level based on biomarkers
        val (riskLevel, riskScore, highRiskFlags) = assessRisk(biomarkers)

        return Scan(
            id = UUID.randomUUID().toString(),
            patientId = patientId,
            scannedAt = scanTimestamp,
            scanDurationSeconds = duration,

            // Cardiovascular
            systolicBp = biomarkers["systolicBp"],
            diastolicBp = biomarkers["diastolicBp"],
            heartRate = biomarkers["heartRate"],
            heartRateVariability = biomarkers["heartRateVariability"],
            cardiacOutput = biomarkers["cardiacOutput"],
            strokeVolume = biomarkers["strokeVolume"],

            // Respiratory
            respiratoryRate = biomarkers["respiratoryRate"],
            spo2 = biomarkers["spo2"],
            perfusionIndex = biomarkers["perfusionIndex"],

            // Metabolic
            bloodGlucose = biomarkers["bloodGlucose"],
            hba1c = biomarkers["hba1c"],
            cholesterol = biomarkers["cholesterol"],
            triglycerides = biomarkers["triglycerides"],

            // Hematology
            hemoglobin = biomarkers["hemoglobin"],
            hematocrit = biomarkers["hematocrit"],

            // Renal
            bun = biomarkers["bun"],
            creatinine = biomarkers["creatinine"],

            // Vascular
            arterialStiffness = biomarkers["arterialStiffness"],
            vascularAge = biomarkers["vascularAge"],
            peripheralResistance = biomarkers["peripheralResistance"],
            meanArterialPressure = biomarkers["meanArterialPressure"],

            // Autonomic
            sympatheticTone = biomarkers["sympatheticTone"],
            parasympatheticTone = biomarkers["parasympatheticTone"],
            stressIndex = biomarkers["stressIndex"],

            // Body Composition
            bmi = biomarkers["bmi"],
            bodyFatPercentage = biomarkers["bodyFatPercentage"],
            visceralFatLevel = biomarkers["visceralFatLevel"],

            // Advanced Cardiac
            leftVentricularEjectionFraction = biomarkers["leftVentricularEjectionFraction"],
            systolicTimeIntervals = biomarkers["systolicTimeIntervals"],
            diastolicFunction = biomarkers["diastolicFunction"],
            qrsDuration = biomarkers["qrsDuration"],

            // Pulmonary
            lungCapacity = biomarkers["lungCapacity"],
            respiratoryEfficiency = biomarkers["respiratoryEfficiency"],
            oxygenSaturationVariability = biomarkers["oxygenSaturationVariability"],

            // Risk assessment
            riskScore = riskScore,
            riskLevel = riskLevel,
            highRiskFlags = highRiskFlags,

            // Quality
            signalQuality = if (quality >= 0.8f) SignalQuality.EXCELLENT
            else if (quality >= 0.6f) SignalQuality.GOOD
            else if (quality >= 0.4f) SignalQuality.FAIR
            else SignalQuality.POOR,
            qualityScore = quality,
            rejectionReason = null,

            // Full biomarkers as JSON
            biomarkersFull = biomarkers,

            // Timestamps
            createdAt = scanTimestamp,
            updatedAt = scanTimestamp,
            syncStatus = SyncStatus.PENDING,
            syncedAt = null,
            serverScanId = null
        )
    }

    private fun assessRisk(biomarkers: Map<String, Float>): Triple<RiskLevel, Float, List<String>> {
        val flags = mutableListOf<String>()
        var riskPoints = 0

        // Hypertension check
        biomarkers["systolicBp"]?.let { if (it >= 140f) { flags.add("Hypertension"); riskPoints += 3 } }
        biomarkers["diastolicBp"]?.let { if (it >= 90f) { flags.add("High Diastolic BP"); riskPoints += 3 } }

        // Hypotension check
        biomarkers["systolicBp"]?.let { if (it < 90f) { flags.add("Hypotension"); riskPoints += 2 } }

        // Tachycardia/Bradycardia
        biomarkers["heartRate"]?.let {
            when {
                it > 100f -> { flags.add("Tachycardia"); riskPoints += 2 }
                it < 50f -> { flags.add("Bradycardia"); riskPoints += 2 }
            }
        }

        // Low oxygen
        biomarkers["spo2"]?.let { if (it < 92f) { flags.add("Low Oxygen Saturation"); riskPoints += 4 } }

        // High blood glucose
        biomarkers["bloodGlucose"]?.let { if (it >= 126f) { flags.add("High Blood Glucose"); riskPoints += 3 } }

        // High cholesterol
        biomarkers["cholesterol"]?.let { if (it >= 240f) { flags.add("High Cholesterol"); riskPoints += 2 } }

        // Determine risk level
        val riskLevel = when {
            riskPoints >= 6 -> RiskLevel.HIGH
            riskPoints >= 3 -> RiskLevel.ELEVATED
            else -> RiskLevel.NORMAL
        }

        val riskScore = (riskPoints / 10f).coerceIn(0f, 1f)

        return Triple(riskLevel, riskScore, flags)
    }
}

/**
 * Sealed class for scan progress states
 */
sealed class ScanProgress {
    data class InProgress(
        val elapsedSeconds: Int,
        val totalSeconds: Int,
        val progress: Float,
        val signalQuality: Float,
        val guidance: String
    ) : ScanProgress()

    data class Complete(
        val scanId: String,
        val scan: Scan
    ) : ScanProgress()

    data class Failed(
        val reason: String,
        val code: com.globaloutcomes.phi.domain.scan.FailureCode
    ) : ScanProgress()
}
