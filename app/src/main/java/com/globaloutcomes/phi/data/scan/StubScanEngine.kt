package com.globaloutcomes.phi.data.scan

import com.globaloutcomes.phi.domain.model.Biomarkers
import com.globaloutcomes.phi.domain.scan.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

/**
 * Stub Scan Engine - For development and testing
 * Generates realistic fake biomarkers without actual BiosenseSignal SDK
 */
class StubScanEngine @Inject constructor() : ScanEngine {

    private var isScanning = false

    override suspend fun startSession(config: ScanConfig): Flow<ScanSessionState> = flow {
        isScanning = true

        try {
            // Initializing
            emit(ScanSessionState.Initializing)
            delay(1000)

            // Image validity checks (first 3 seconds)
            emit(ScanSessionState.ImageValidity(false, "Position your face in the oval"))
            delay(1000)
            emit(ScanSessionState.ImageValidity(false, "Hold still"))
            delay(1000)
            emit(ScanSessionState.ImageValidity(true, null))
            delay(500)

            // Measuring phase (30 seconds)
            val duration = config.measurementDuration
            for (second in 1..duration) {
                val progress = second.toFloat() / duration
                val timeRemaining = duration - second

                emit(
                    ScanSessionState.Measuring(
                        progress = progress,
                        timeRemaining = timeRemaining,
                        currentVital = when {
                            second < 10 -> "Pulse Rate"
                            second < 15 -> "Blood Pressure"
                            second < 20 -> "Oxygen Saturation"
                            second < 25 -> "Stress Index"
                            else -> "Wellness Index"
                        }
                    )
                )

                // Emit vital sign updates sporadically
                if (second % 5 == 0) {
                    emit(
                        ScanSessionState.VitalSignUpdate(
                            vitalSign = "Pulse Rate",
                            value = Random.nextDouble(60.0, 100.0),
                            confidence = Random.nextDouble(0.8, 1.0)
                        )
                    )
                }

                delay(1000)

                if (!isScanning) {
                    emit(ScanSessionState.Error("CANCELLED", "Scan cancelled by user"))
                    return@flow
                }
            }

            // Generate complete biomarkers
            val biomarkers = generateRealisticBiomarkers(config)
            val signalQuality = Random.nextDouble(0.85, 0.98)

            emit(ScanSessionState.Complete(biomarkers, signalQuality))

        } catch (e: Exception) {
            emit(ScanSessionState.Error("EXCEPTION", e.message ?: "Unknown error"))
        } finally {
            isScanning = false
        }
    }

    override fun stopSession() {
        isScanning = false
    }

    override fun isScanning(): Boolean = isScanning

    /**
     * Generate realistic fake biomarkers
     * Values are within normal ranges with some variation
     */
    private fun generateRealisticBiomarkers(config: ScanConfig): Biomarkers {
        // Introduce 10% chance of high-risk values for testing
        val isHighRisk = Random.nextFloat() < 0.1f

        return Biomarkers(
            // Cardiovascular
            pulseRate = if (isHighRisk && Random.nextBoolean()) {
                Random.nextDouble(120.0, 140.0) // High
            } else {
                Random.nextDouble(60.0, 90.0) // Normal
            },
            bpSystolic = if (isHighRisk && Random.nextBoolean()) {
                Random.nextDouble(160.0, 180.0) // Crisis
            } else {
                Random.nextDouble(110.0, 130.0) // Normal
            },
            bpDiastolic = if (isHighRisk && Random.nextBoolean()) {
                Random.nextDouble(100.0, 110.0) // Crisis
            } else {
                Random.nextDouble(70.0, 85.0) // Normal
            },
            meanArterialPressure = Random.nextDouble(70.0, 100.0),
            pulsePressure = Random.nextDouble(35.0, 50.0),
            cardiacWorkload = Random.nextDouble(0.5, 1.5),
            heartAge = config.userInfo?.age?.toDouble()?.let { it + Random.nextDouble(-5.0, 10.0) },

            // Respiratory
            respirationRate = Random.nextDouble(12.0, 20.0),
            oxygenSaturation = if (isHighRisk && Random.nextBoolean()) {
                Random.nextDouble(88.0, 93.0) // Low
            } else {
                Random.nextDouble(95.0, 100.0) // Normal
            },

            // Bloodless Blood Tests
            hemoglobin = Random.nextDouble(12.0, 16.0),
            hemoglobinA1c = Random.nextDouble(4.5, 5.5),

            // Risk Indicators
            ascvdRisk = Random.nextDouble(0.1, 0.5),
            ascvdRiskLevel = if (isHighRisk) "High" else "Low",
            highBloodPressureRisk = if (isHighRisk) Random.nextDouble(0.7, 0.9) else Random.nextDouble(0.2, 0.6),
            highFastingGlucoseRisk = Random.nextDouble(0.1, 0.5),
            highHemoglobinA1cRisk = Random.nextDouble(0.1, 0.5),
            highTotalCholesterolRisk = Random.nextDouble(0.2, 0.6),
            lowHemoglobinRisk = Random.nextDouble(0.1, 0.4),

            // HRV
            meanRri = Random.nextDouble(750.0, 1000.0),
            rri = Random.nextDouble(750.0, 1000.0),
            sdnn = Random.nextDouble(30.0, 80.0),
            rmssd = Random.nextDouble(20.0, 60.0),
            sd1 = Random.nextDouble(15.0, 45.0),
            sd2 = Random.nextDouble(40.0, 100.0),
            prq = Random.nextDouble(0.5, 1.5),
            lfhf = Random.nextDouble(0.5, 3.0),

            // ANS
            pnsIndex = Random.nextDouble(-2.0, 2.0),
            pnsZone = listOf("Very Low", "Low", "Balanced", "High", "Very High").random(),
            snsIndex = Random.nextDouble(-2.0, 2.0),
            snsZone = listOf("Very Low", "Low", "Balanced", "High", "Very High").random(),

            // Stress
            stressLevel = listOf("Low", "Normal", "Elevated", "High").random(),
            stressIndex = if (isHighRisk) Random.nextDouble(4.0, 7.0) else Random.nextDouble(1.0, 3.5),
            normalizedStressIndex = Random.nextDouble(0.3, 0.8),

            // Wellness
            wellnessIndex = if (isHighRisk) Random.nextDouble(50.0, 70.0) else Random.nextDouble(70.0, 95.0),
            wellnessLevel = if (isHighRisk) "Low" else "Good"
        )
    }
}
