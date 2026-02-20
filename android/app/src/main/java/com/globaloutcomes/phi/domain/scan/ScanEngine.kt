package com.globaloutcomes.phi.domain.scan

import kotlinx.coroutines.flow.Flow

/**
 * Interface for scan engine implementations
 * Abstracts BiosenseSignal SDK to allow stub implementation during development
 */
interface ScanEngine {
    /**
     * Performs a scan and emits progress updates
     * @param config Scan configuration
     * @return Flow of ScanResult updates
     */
    fun performScan(config: ScanConfig): Flow<ScanResult>

    /**
     * Cancels the current scan
     */
    suspend fun cancelScan()

    /**
     * Checks if face is detected and properly positioned
     */
    suspend fun checkFaceDetection(): FaceDetectionResult
}

/**
 * Configuration for a scan
 */
data class ScanConfig(
    val patientId: String,
    val targetDuration: Int = 30, // seconds
    val qualityThreshold: Float = 0.7f
)

/**
 * Result of a scan operation
 */
sealed class ScanResult {
    data class Progress(
        val elapsedSeconds: Int,
        val totalSeconds: Int,
        val signalQuality: Float,
        val guidance: String
    ) : ScanResult()

    data class Success(
        val biomarkers: Map<String, Float>,
        val duration: Int,
        val quality: Float
    ) : ScanResult()

    data class Failure(
        val reason: String,
        val code: FailureCode
    ) : ScanResult()
}

/**
 * Face detection result
 */
data class FaceDetectionResult(
    val faceDetected: Boolean,
    val faceBounds: android.graphics.RectF? = null,
    val guidance: String
)

/**
 * Failure codes for scan operations
 */
enum class FailureCode {
    NO_FACE_DETECTED,
    POOR_LIGHTING,
    EXCESSIVE_MOVEMENT,
    LOW_SIGNAL_QUALITY,
    USER_CANCELLED,
    TIMEOUT,
    CAMERA_ERROR
}
