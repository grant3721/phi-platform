package com.globaloutcomes.phi.domain.usecase.scan

import com.globaloutcomes.phi.domain.model.Biomarkers
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Scan
import com.globaloutcomes.phi.domain.repository.ConfigThresholdRepository
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import javax.inject.Inject

/**
 * Assess Risk Use Case
 *
 * Evaluates scan biomarkers against ConfigThresholds to determine high-risk status.
 * Updates patient's highRiskFlag and highRiskReasons based on findings.
 *
 * High-risk criteria (from spec ConfigThresholds):
 * - BP Systolic > threshold (e.g., 140 mmHg for hypertension, 180 for crisis)
 * - BP Diastolic > threshold (e.g., 90 mmHg for hypertension, 120 for crisis)
 * - Oxygen Saturation < threshold (e.g., 90% for hypoxia)
 * - Hemoglobin < threshold (e.g., 12 g/dL for anemia)
 * - Risk scores > threshold (ASCVD, diabetes, etc.)
 *
 * This use case should be called after every scan to:
 * 1. Flag high-risk patients
 * 2. Update patient record with risk reasons
 * 3. Trigger automatic referral generation
 */
class AssessRiskUseCase @Inject constructor(
    private val configThresholdRepository: ConfigThresholdRepository,
    private val patientRepository: PatientRepository,
    private val scanRepository: ScanRepository
) {

    suspend operator fun invoke(scanId: String): Result<RiskAssessment> {
        // Load scan
        val scanResult = scanRepository.getById(scanId)
        if (scanResult.isFailure) {
            return Result.failure(scanResult.exceptionOrNull()!!)
        }

        val scan = scanResult.getOrNull()
            ?: return Result.failure(IllegalArgumentException("Scan not found"))

        // Load patient
        val patientResult = patientRepository.getById(scan.patientId)
        if (patientResult.isFailure) {
            return Result.failure(patientResult.exceptionOrNull()!!)
        }

        val patient = patientResult.getOrNull()
            ?: return Result.failure(IllegalArgumentException("Patient not found"))

        // Load thresholds
        val thresholdsResult = configThresholdRepository.getAll()
        if (thresholdsResult.isFailure) {
            return Result.failure(thresholdsResult.exceptionOrNull()!!)
        }

        val thresholds = thresholdsResult.getOrThrow()

        // Assess risk based on biomarkers
        val riskReasons = mutableListOf<String>()
        val biomarkers = scan.biomarkers

        // Cardiovascular risk assessment
        assessBloodPressure(biomarkers, thresholds, riskReasons)
        assessOxygenSaturation(biomarkers, thresholds, riskReasons)
        assessPulseRate(biomarkers, thresholds, riskReasons)

        // Metabolic risk assessment
        assessHemoglobin(biomarkers, thresholds, riskReasons)
        assessHemoglobinA1c(biomarkers, thresholds, riskReasons)

        // Risk scores
        assessRiskScores(biomarkers, thresholds, riskReasons)

        // Determine if high-risk
        val isHighRisk = riskReasons.isNotEmpty()

        // Maternal high-risk (if pregnant + has risk factors)
        val isMaternalHighRisk = patient.pregnant && isHighRisk

        // Update patient record
        val updatedPatient = patient.copy(
            highRiskFlag = isHighRisk,
            highRiskReasons = riskReasons,
            maternalHighRisk = isMaternalHighRisk,
            lastScanDate = scan.scannedAt,
            totalScans = patient.totalScans + 1
        )

        patientRepository.update(updatedPatient)

        // Update scan with risk flags
        val updatedScan = scan.copy(
            riskFlags = riskReasons
        )

        scanRepository.update(updatedScan)

        return Result.success(
            RiskAssessment(
                isHighRisk = isHighRisk,
                isMaternalHighRisk = isMaternalHighRisk,
                riskReasons = riskReasons,
                requiresReferral = isHighRisk
            )
        )
    }

    private fun assessBloodPressure(
        biomarkers: Biomarkers,
        thresholds: List<com.globaloutcomes.phi.domain.model.ConfigThreshold>,
        riskReasons: MutableList<String>
    ) {
        val systolic = biomarkers.bpSystolic ?: return
        val diastolic = biomarkers.bpDiastolic ?: return

        // Hypertensive Crisis (>= 180/120)
        val crisisSystolicThreshold = thresholds.find { it.code == "BP_SYSTOLIC_CRISIS" }?.value ?: 180.0
        val crisisDiastolicThreshold = thresholds.find { it.code == "BP_DIASTOLIC_CRISIS" }?.value ?: 120.0

        if (systolic >= crisisSystolicThreshold || diastolic >= crisisDiastolicThreshold) {
            riskReasons.add("Hypertensive Crisis (BP: $systolic/$diastolic mmHg)")
            return
        }

        // Stage 2 Hypertension (>= 140/90)
        val stage2SystolicThreshold = thresholds.find { it.code == "BP_SYSTOLIC_STAGE2" }?.value ?: 140.0
        val stage2DiastolicThreshold = thresholds.find { it.code == "BP_DIASTOLIC_STAGE2" }?.value ?: 90.0

        if (systolic >= stage2SystolicThreshold || diastolic >= stage2DiastolicThreshold) {
            riskReasons.add("Stage 2 Hypertension (BP: $systolic/$diastolic mmHg)")
        }
    }

    private fun assessOxygenSaturation(
        biomarkers: Biomarkers,
        thresholds: List<com.globaloutcomes.phi.domain.model.ConfigThreshold>,
        riskReasons: MutableList<String>
    ) {
        val spo2 = biomarkers.oxygenSaturation ?: return

        val threshold = thresholds.find { it.code == "SPO2_LOW" }?.value ?: 90.0

        if (spo2 < threshold) {
            riskReasons.add("Low Oxygen Saturation (SpO2: ${spo2}%)")
        }
    }

    private fun assessPulseRate(
        biomarkers: Biomarkers,
        thresholds: List<com.globaloutcomes.phi.domain.model.ConfigThreshold>,
        riskReasons: MutableList<String>
    ) {
        val pulseRate = biomarkers.pulseRate ?: return

        val tachycardiaThreshold = thresholds.find { it.code == "PULSE_RATE_HIGH" }?.value ?: 100.0
        val bradycardiaThreshold = thresholds.find { it.code == "PULSE_RATE_LOW" }?.value ?: 60.0

        if (pulseRate > tachycardiaThreshold) {
            riskReasons.add("Tachycardia (Pulse: $pulseRate bpm)")
        } else if (pulseRate < bradycardiaThreshold) {
            riskReasons.add("Bradycardia (Pulse: $pulseRate bpm)")
        }
    }

    private fun assessHemoglobin(
        biomarkers: Biomarkers,
        thresholds: List<com.globaloutcomes.phi.domain.model.ConfigThreshold>,
        riskReasons: MutableList<String>
    ) {
        val hemoglobin = biomarkers.hemoglobin ?: return

        val threshold = thresholds.find { it.code == "HEMOGLOBIN_LOW" }?.value ?: 12.0

        if (hemoglobin < threshold) {
            riskReasons.add("Anemia (Hemoglobin: ${hemoglobin} g/dL)")
        }
    }

    private fun assessHemoglobinA1c(
        biomarkers: Biomarkers,
        thresholds: List<com.globaloutcomes.phi.domain.model.ConfigThreshold>,
        riskReasons: MutableList<String>
    ) {
        val hba1c = biomarkers.hemoglobinA1c ?: return

        val diabetesThreshold = thresholds.find { it.code == "HBA1C_DIABETES" }?.value ?: 6.5
        val prediabetesThreshold = thresholds.find { it.code == "HBA1C_PREDIABETES" }?.value ?: 5.7

        if (hba1c >= diabetesThreshold) {
            riskReasons.add("Diabetes Range (HbA1c: ${hba1c}%)")
        } else if (hba1c >= prediabetesThreshold) {
            riskReasons.add("Prediabetes Range (HbA1c: ${hba1c}%)")
        }
    }

    private fun assessRiskScores(
        biomarkers: Biomarkers,
        thresholds: List<com.globaloutcomes.phi.domain.model.ConfigThreshold>,
        riskReasons: MutableList<String>
    ) {
        // ASCVD Risk
        val ascvdRisk = biomarkers.ascvdRisk ?: 0.0
        val ascvdThreshold = thresholds.find { it.code == "ASCVD_RISK_HIGH" }?.value ?: 0.2

        if (ascvdRisk >= ascvdThreshold) {
            riskReasons.add("High ASCVD Risk (${(ascvdRisk * 100).toInt()}%)")
        }

        // High Blood Pressure Risk
        val bpRisk = biomarkers.highBloodPressureRisk ?: 0.0
        val bpRiskThreshold = thresholds.find { it.code == "BP_RISK_HIGH" }?.value ?: 0.7

        if (bpRisk >= bpRiskThreshold) {
            riskReasons.add("High Blood Pressure Risk (${(bpRisk * 100).toInt()}%)")
        }

        // Diabetes Risk
        val diabetesRisk = biomarkers.highFastingGlucoseRisk ?: 0.0
        val diabetesRiskThreshold = thresholds.find { it.code == "DIABETES_RISK_HIGH" }?.value ?: 0.7

        if (diabetesRisk >= diabetesRiskThreshold) {
            riskReasons.add("High Diabetes Risk (${(diabetesRisk * 100).toInt()}%)")
        }
    }
}

/**
 * Risk Assessment Result
 */
data class RiskAssessment(
    val isHighRisk: Boolean,
    val isMaternalHighRisk: Boolean,
    val riskReasons: List<String>,
    val requiresReferral: Boolean
)
