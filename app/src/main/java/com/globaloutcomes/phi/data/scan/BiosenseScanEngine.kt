package com.globaloutcomes.phi.data.scan

import android.content.Context
import com.biosensesignal.sdk.api.HealthMonitorException
import com.biosensesignal.sdk.api.alerts.ErrorData
import com.biosensesignal.sdk.api.alerts.WarningData
import com.biosensesignal.sdk.api.images.ImageData
import com.biosensesignal.sdk.api.images.ImageListener
import com.biosensesignal.sdk.api.images.ImageValidity
import com.biosensesignal.sdk.api.license.LicenseDetails
import com.biosensesignal.sdk.api.session.Session
import com.biosensesignal.sdk.api.session.SessionInfoListener
import com.biosensesignal.sdk.api.session.SessionState
import com.biosensesignal.sdk.api.vital_signs.VitalSign
import com.biosensesignal.sdk.api.vital_signs.VitalSignTypes
import com.biosensesignal.sdk.api.vital_signs.VitalSignsListener
import com.biosensesignal.sdk.api.vital_signs.VitalSignsResults
import com.biosensesignal.sdk.session.FaceSessionBuilder
import com.globaloutcomes.phi.domain.model.Biomarkers
import com.globaloutcomes.phi.domain.scan.ScanConfig
import com.globaloutcomes.phi.domain.scan.ScanEngine
import com.globaloutcomes.phi.domain.scan.ScanSessionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Real BiosenseSignal SDK Scan Engine
 *
 * IMPORTANT: Requires license key to be configured
 * Contact BiosenseSignal for production license key
 *
 * SDK Documentation: https://docs.biosensesignal.com
 */
class BiosenseScanEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : ScanEngine {

    companion object {
        // TODO: Replace with actual license key from BiosenseSignal
        private const val LICENSE_KEY = "<ENTER_YOUR_LICENSE_KEY>"
        private const val MEASUREMENT_DURATION = 50L // SDK recommends 50-60 seconds for accuracy
    }

    private var session: Session? = null
    private var isScanning = false

    override suspend fun startSession(config: ScanConfig): Flow<ScanSessionState> = callbackFlow {
        isScanning = true

        // Listener implementations
        val imageListener = object : ImageListener {
            override fun onImage(imageData: ImageData) {
                // Emit image validity guidance
                if (imageData.imageValidity != ImageValidity.VALID) {
                    val guidance = when (imageData.imageValidity) {
                        ImageValidity.INVALID_DEVICE_ORIENTATION -> "Hold phone vertically"
                        ImageValidity.INVALID_ROI -> "Position your face in the oval"
                        ImageValidity.TILTED_HEAD -> "Keep your head straight"
                        ImageValidity.FACE_TOO_FAR -> "Move closer to camera"
                        ImageValidity.UNEVEN_LIGHT -> "Find better lighting"
                        else -> "Adjust position"
                    }
                    trySend(ScanSessionState.ImageValidity(false, guidance))
                } else {
                    trySend(ScanSessionState.ImageValidity(true, null))
                }

                // Note: imageData.image contains Bitmap for camera preview
                // imageData.roi contains face detection rectangle
                // These can be used for UI visualization
            }
        }

        val vitalSignsListener = object : VitalSignsListener {
            override fun onVitalSign(vitalSign: VitalSign) {
                // Real-time vital sign updates during scan
                val vitalName = vitalSign.javaClass.simpleName.replace("VitalSign", "")
                val value = getVitalSignValue(vitalSign)

                if (value != null) {
                    trySend(
                        ScanSessionState.VitalSignUpdate(
                            vitalSign = vitalName,
                            value = value,
                            confidence = 1.0 // SDK doesn't provide confidence per vital
                        )
                    )
                }
            }

            override fun onFinalResults(finalResults: VitalSignsResults) {
                // Scan complete - extract all biomarkers
                val biomarkers = extractBiomarkers(finalResults)

                // Calculate signal quality (SDK doesn't provide this directly)
                // We approximate from data completeness
                val signalQuality = calculateSignalQuality(biomarkers)

                trySend(ScanSessionState.Complete(biomarkers, signalQuality))
                isScanning = false
            }

            override fun onWarning(warningData: WarningData) {
                // SDK warnings (non-fatal)
                trySend(
                    ScanSessionState.ImageValidity(
                        false,
                        "Warning: ${warningData.code}"
                    )
                )
            }

            override fun onError(errorData: ErrorData) {
                // SDK errors (scan failed)
                trySend(
                    ScanSessionState.Error(
                        code = errorData.code.toString(),
                        message = errorData.message ?: "Scan failed"
                    )
                )
                isScanning = false
            }
        }

        val sessionInfoListener = object : SessionInfoListener {
            override fun onSessionStateChange(sessionState: SessionState) {
                when (sessionState) {
                    SessionState.INITIALIZING -> {
                        trySend(ScanSessionState.Initializing)
                    }
                    SessionState.READY -> {
                        // Session ready, start measurement
                        try {
                            session?.start(MEASUREMENT_DURATION)
                        } catch (e: HealthMonitorException) {
                            trySend(
                                ScanSessionState.Error(
                                    code = e.errorCode.toString(),
                                    message = e.message ?: "Failed to start scan"
                                )
                            )
                        }
                    }
                    SessionState.PROCESSING -> {
                        // Measuring - emit progress updates
                        // Note: SDK doesn't provide progress directly
                        // We approximate from elapsed time
                        trySend(
                            ScanSessionState.Measuring(
                                progress = 0f,
                                timeRemaining = MEASUREMENT_DURATION.toInt(),
                                currentVital = "Measuring..."
                            )
                        )
                    }
                    else -> {
                        // Other states (TERMINATED, etc.)
                    }
                }
            }

            // Required overrides for SessionInfoListener
            override fun onLicenseInfo(licenseInfo: com.biosensesignal.sdk.api.license.LicenseInfo) {
                // Log license status if needed
            }

            override fun onEnabledVitalSigns(sessionEnabledVitalSigns: com.biosensesignal.sdk.api.SessionEnabledVitalSigns) {
                // Log which vital signs are enabled for this license
            }
        }

        try {
            // Create session with license
            val licenseDetails = LicenseDetails(LICENSE_KEY)
            session = FaceSessionBuilder(context)
                .withImageListener(imageListener)
                .withVitalSignsListener(vitalSignsListener)
                .withSessionInfoListener(sessionInfoListener)
                .build(licenseDetails)

            // Session will auto-start via onSessionStateChange callback

        } catch (e: HealthMonitorException) {
            trySend(
                ScanSessionState.Error(
                    code = e.errorCode.toString(),
                    message = "SDK initialization failed: ${e.message}"
                )
            )
            isScanning = false
        }

        awaitClose {
            session?.terminate()
            session = null
            isScanning = false
        }
    }

    override fun stopSession() {
        try {
            session?.stop()
        } catch (e: HealthMonitorException) {
            // Ignore errors on stop
        }
        isScanning = false
    }

    override fun isScanning(): Boolean = isScanning

    /**
     * Extract all biomarkers from SDK results
     * Maps SDK VitalSign objects to our Biomarkers domain model
     */
    private fun extractBiomarkers(results: VitalSignsResults): Biomarkers {
        // Helper to safely get vital sign values
        fun <T : VitalSign> getVital(type: VitalSignTypes): Double? {
            return try {
                val vital = results.getResult(type) as? T
                getVitalSignValue(vital)
            } catch (e: Exception) {
                null
            }
        }

        return Biomarkers(
            // Cardiovascular
            pulseRate = getVital(VitalSignTypes.PULSE_RATE),
            bpSystolic = getVital(VitalSignTypes.BP_SYSTOLIC),
            bpDiastolic = getVital(VitalSignTypes.BP_DIASTOLIC),
            meanArterialPressure = getVital(VitalSignTypes.MEAN_ARTERIAL_PRESSURE),
            pulsePressure = getVital(VitalSignTypes.PULSE_PRESSURE),
            cardiacWorkload = getVital(VitalSignTypes.CARDIAC_WORKLOAD),
            heartAge = getVital(VitalSignTypes.HEART_AGE),

            // Respiratory
            respirationRate = getVital(VitalSignTypes.RESPIRATION_RATE),
            oxygenSaturation = getVital(VitalSignTypes.OXYGEN_SATURATION),

            // Bloodless Blood Tests
            hemoglobin = getVital(VitalSignTypes.HEMOGLOBIN),
            hemoglobinA1c = getVital(VitalSignTypes.HEMOGLOBIN_A1C),

            // Risk Indicators (SDK provides these as numeric scores)
            ascvdRisk = getVital(VitalSignTypes.ASCVD_RISK),
            ascvdRiskLevel = null, // We'll calculate this from score
            highBloodPressureRisk = getVital(VitalSignTypes.HIGH_BLOOD_PRESSURE_RISK),
            highFastingGlucoseRisk = getVital(VitalSignTypes.HIGH_FASTING_GLUCOSE_RISK),
            highHemoglobinA1cRisk = getVital(VitalSignTypes.HIGH_HEMOGLOBIN_A1C_RISK),
            highTotalCholesterolRisk = getVital(VitalSignTypes.HIGH_TOTAL_CHOLESTEROL_RISK),
            lowHemoglobinRisk = getVital(VitalSignTypes.LOW_HEMOGLOBIN_RISK),

            // HRV
            meanRri = getVital(VitalSignTypes.MEAN_RRI),
            rri = getVital(VitalSignTypes.RRI),
            sdnn = getVital(VitalSignTypes.SDNN),
            rmssd = getVital(VitalSignTypes.RMSSD),
            sd1 = getVital(VitalSignTypes.SD1),
            sd2 = getVital(VitalSignTypes.SD2),
            prq = getVital(VitalSignTypes.PRQ),
            lfhf = getVital(VitalSignTypes.LFHF),

            // ANS
            pnsIndex = getVital(VitalSignTypes.PNS_INDEX),
            pnsZone = null, // Would need to map from index
            snsIndex = getVital(VitalSignTypes.SNS_INDEX),
            snsZone = null, // Would need to map from index

            // Stress
            stressLevel = null, // Would need to map from index
            stressIndex = getVital(VitalSignTypes.STRESS_INDEX),
            normalizedStressIndex = getVital(VitalSignTypes.NORMALIZED_STRESS_INDEX),

            // Wellness
            wellnessIndex = getVital(VitalSignTypes.WELLNESS_INDEX),
            wellnessLevel = null // Would need to map from index
        )
    }

    /**
     * Extract value from VitalSign object
     * SDK uses different property names for different vital signs
     */
    private fun getVitalSignValue(vitalSign: VitalSign?): Double? {
        if (vitalSign == null) return null

        return try {
            // Most vital signs have a 'value' property
            val valueField = vitalSign.javaClass.getDeclaredField("value")
            valueField.isAccessible = true
            when (val value = valueField.get(vitalSign)) {
                is Double -> value
                is Float -> value.toDouble()
                is Int -> value.toDouble()
                is Long -> value.toDouble()
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate signal quality from biomarker completeness
     * SDK doesn't provide signal quality directly
     */
    private fun calculateSignalQuality(biomarkers: Biomarkers): Double {
        // Count non-null vital signs
        val fields = Biomarkers::class.java.declaredFields
        var total = 0
        var nonNull = 0

        fields.forEach { field ->
            if (field.type == Double::class.java || field.type == Double::class.javaObjectType) {
                total++
                field.isAccessible = true
                if (field.get(biomarkers) != null) {
                    nonNull++
                }
            }
        }

        return if (total > 0) nonNull.toDouble() / total else 0.0
    }
}
