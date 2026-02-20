package com.globaloutcomes.phi.data.scan

import android.graphics.RectF
import com.globaloutcomes.phi.domain.scan.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

/**
 * Stub implementation of ScanEngine for development
 * Simulates realistic scan behavior without actual SDK
 */
class StubScanEngine @Inject constructor() : ScanEngine {

    private var isCancelled = false

    override fun performScan(config: ScanConfig): Flow<ScanResult> = flow {
        isCancelled = false
        val startTime = System.currentTimeMillis()
        val targetDuration = config.targetDuration

        try {
            // Simulate scan progression
            for (second in 0..targetDuration) {
                if (isCancelled) {
                    emit(ScanResult.Failure("User cancelled scan", FailureCode.USER_CANCELLED))
                    return@flow
                }

                val progress = second.toFloat() / targetDuration.toFloat()
                val signalQuality = calculateSignalQuality(progress)
                val guidance = getGuidanceMessage(progress, signalQuality)

                // Emit progress
                emit(
                    ScanResult.Progress(
                        elapsedSeconds = second,
                        totalSeconds = targetDuration,
                        signalQuality = signalQuality,
                        guidance = guidance
                    )
                )

                // Simulate occasional quality issues
                if (signalQuality < config.qualityThreshold && second > 5) {
                    if (Random.nextFloat() < 0.1f) { // 10% chance of quality failure
                        emit(
                            ScanResult.Failure(
                                "Signal quality too low. Please hold still.",
                                FailureCode.LOW_SIGNAL_QUALITY
                            )
                        )
                        return@flow
                    }
                }

                delay(1000) // 1 second intervals
            }

            // Scan complete - generate biomarkers
            val biomarkers = generateRealisticBiomarkers()
            val actualDuration = ((System.currentTimeMillis() - startTime) / 1000).toInt()

            emit(
                ScanResult.Success(
                    biomarkers = biomarkers,
                    duration = actualDuration,
                    quality = 0.85f
                )
            )

        } catch (e: Exception) {
            emit(ScanResult.Failure("Scan error: ${e.message}", FailureCode.CAMERA_ERROR))
        }
    }

    override suspend fun cancelScan() {
        isCancelled = true
    }

    override suspend fun checkFaceDetection(): FaceDetectionResult {
        // Simulate face detection
        val faceDetected = Random.nextFloat() > 0.2f // 80% chance of detecting face

        return if (faceDetected) {
            FaceDetectionResult(
                faceDetected = true,
                faceBounds = RectF(100f, 200f, 300f, 400f), // Dummy bounds
                guidance = "Face detected. Hold still."
            )
        } else {
            FaceDetectionResult(
                faceDetected = false,
                faceBounds = null,
                guidance = "No face detected. Please position your face in the frame."
            )
        }
    }

    private fun calculateSignalQuality(progress: Float): Float {
        // Simulate signal quality that improves over time
        val baseQuality = 0.6f + (progress * 0.3f) // 0.6 to 0.9
        val noise = (Random.nextFloat() - 0.5f) * 0.1f // ±0.05 noise
        return (baseQuality + noise).coerceIn(0.5f, 1.0f)
    }

    private fun getGuidanceMessage(progress: Float, quality: Float): String {
        return when {
            progress < 0.1f -> "Hold still and look at the camera"
            progress < 0.3f -> "Scan in progress... Keep your face in frame"
            progress < 0.5f -> "Good. Continue holding still"
            progress < 0.7f -> "Almost there... Keep steady"
            progress < 0.9f -> "Final moments... Hold still"
            quality < 0.7f -> "Signal quality low. Please hold still"
            else -> "Scan complete!"
        }
    }

    private fun generateRealisticBiomarkers(): Map<String, Float> {
        // Generate realistic vital signs with some variation
        return mapOf(
            // Cardiovascular (6)
            "systolicBp" to randomInRange(110f, 140f),
            "diastolicBp" to randomInRange(70f, 90f),
            "heartRate" to randomInRange(60f, 90f),
            "heartRateVariability" to randomInRange(40f, 80f),
            "cardiacOutput" to randomInRange(4.5f, 6.5f),
            "strokeVolume" to randomInRange(60f, 100f),

            // Respiratory (3)
            "respiratoryRate" to randomInRange(12f, 18f),
            "spo2" to randomInRange(95f, 99f),
            "perfusionIndex" to randomInRange(1.0f, 8.0f),

            // Metabolic (4)
            "bloodGlucose" to randomInRange(80f, 120f),
            "hba1c" to randomInRange(4.5f, 6.0f),
            "cholesterol" to randomInRange(150f, 220f),
            "triglycerides" to randomInRange(80f, 150f),

            // Hematology (2)
            "hemoglobin" to randomInRange(12f, 16f),
            "hematocrit" to randomInRange(36f, 48f),

            // Renal (2)
            "bun" to randomInRange(8f, 20f),
            "creatinine" to randomInRange(0.7f, 1.3f),

            // Vascular (4)
            "arterialStiffness" to randomInRange(6f, 10f),
            "vascularAge" to randomInRange(25f, 55f),
            "peripheralResistance" to randomInRange(900f, 1500f),
            "meanArterialPressure" to randomInRange(70f, 100f),

            // Autonomic (3)
            "sympatheticTone" to randomInRange(40f, 70f),
            "parasympatheticTone" to randomInRange(30f, 60f),
            "stressIndex" to randomInRange(3f, 8f),

            // Body Composition (3)
            "bmi" to randomInRange(20f, 28f),
            "bodyFatPercentage" to randomInRange(15f, 30f),
            "visceralFatLevel" to randomInRange(5f, 12f),

            // Advanced Cardiac (4)
            "leftVentricularEjectionFraction" to randomInRange(50f, 70f),
            "systolicTimeIntervals" to randomInRange(250f, 350f),
            "diastolicFunction" to randomInRange(6f, 12f),
            "qrsDuration" to randomInRange(80f, 120f),

            // Pulmonary (3)
            "lungCapacity" to randomInRange(3.5f, 5.5f),
            "respiratoryEfficiency" to randomInRange(0.7f, 0.95f),
            "oxygenSaturationVariability" to randomInRange(0.5f, 2.0f)
        )
    }

    private fun randomInRange(min: Float, max: Float): Float {
        return min + (Random.nextFloat() * (max - min))
    }
}
