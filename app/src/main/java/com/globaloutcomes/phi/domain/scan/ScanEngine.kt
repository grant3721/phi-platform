package com.globaloutcomes.phi.domain.scan

import com.globaloutcomes.phi.domain.model.Biomarkers
import kotlinx.coroutines.flow.Flow

/**
 * Scan Engine Interface - Abstraction for BiosenseSignal SDK
 * Allows swapping between StubScanEngine (dev) and BiosenseScanEngine (production)
 */
interface ScanEngine {

    /**
     * Start a scan session
     * @param config Scan configuration
     * @return Flow of scan session states
     */
    suspend fun startSession(config: ScanConfig): Flow<ScanSessionState>

    /**
     * Stop the current scan session
     */
    fun stopSession()

    /**
     * Check if a scan is currently in progress
     */
    fun isScanning(): Boolean
}

/**
 * Scan Configuration
 */
data class ScanConfig(
    val patientId: String,
    val bhwId: String,
    val scanType: String = "baseline",
    val measurementDuration: Int = 30,  // seconds (30 for stub, 50 for real SDK)
    val userInfo: UserInfo? = null
)

/**
 * User Info for SDK (improves accuracy)
 */
data class UserInfo(
    val age: Int,
    val sex: String,
    val weightKg: Double? = null,
    val heightCm: Double? = null
)

/**
 * Scan Session State - Real-time updates during scan
 */
sealed class ScanSessionState {
    object Initializing : ScanSessionState()

    data class Measuring(
        val progress: Float,           // 0.0 to 1.0
        val timeRemaining: Int,        // seconds
        val currentVital: String? = null
    ) : ScanSessionState()

    data class ImageValidity(
        val isValid: Boolean,
        val guidance: String?          // "Position your face", "Hold still", etc.
    ) : ScanSessionState()

    data class VitalSignUpdate(
        val vitalSign: String,
        val value: Double,
        val confidence: Double = 1.0
    ) : ScanSessionState()

    data class Complete(
        val biomarkers: Biomarkers,
        val signalQuality: Double
    ) : ScanSessionState()

    data class Error(
        val code: String,
        val message: String
    ) : ScanSessionState()
}
