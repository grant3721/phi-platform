package com.globaloutcomes.phi.domain.usecase.scan

import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import javax.inject.Inject
import kotlin.math.ceil

/**
 * Check Rescan Eligibility Use Case
 *
 * Implements the exact rescan eligibility decision tree from BUILD_INSTRUCTIONS:
 *
 * 1. ALLOWED_BASELINE - New patient (no previous scans)
 * 2. ALLOWED_HIGH_RISK - Active high-risk flag (can rescan anytime)
 * 3. ALLOWED_MATERNAL_HIGH_RISK - Pregnant + high-risk (can rescan anytime)
 * 4. ALLOWED_MATERNAL - Pregnant, not high-risk (6-month interval)
 * 5. ALLOWED_ANNUAL - Not pregnant, not high-risk (12-month interval)
 * 6. REJECTED - None of above conditions met
 *
 * This is critical business logic for:
 * - Preventing fraud (too-frequent scans)
 * - Ensuring clinical appropriateness
 * - Controlling BHW incentive costs
 */
class CheckRescanEligibilityUseCase @Inject constructor(
    private val patientRepository: PatientRepository,
    private val scanRepository: ScanRepository
) {

    suspend operator fun invoke(patientId: String): Result<RescanEligibility> {
        // Load patient
        val patientResult = patientRepository.getById(patientId)
        if (patientResult.isFailure) {
            return Result.failure(patientResult.exceptionOrNull()!!)
        }

        val patient = patientResult.getOrNull()
            ?: return Result.failure(IllegalArgumentException("Patient not found"))

        // Load all scans for this patient
        val scansResult = scanRepository.getByPatientId(patientId)
        if (scansResult.isFailure) {
            return Result.failure(scansResult.exceptionOrNull()!!)
        }

        val scans = scansResult.getOrThrow()

        // RULE 1: ALLOWED_BASELINE - New patient (no previous scans)
        if (scans.isEmpty()) {
            return Result.success(
                RescanEligibility.Allowed(
                    reason = RescanReason.BASELINE,
                    message = "First scan for this patient"
                )
            )
        }

        val lastScan = scans.maxByOrNull { it.scannedAt }!!
        val daysSinceLastScan = calculateDaysSince(lastScan.scannedAt)

        // RULE 2: ALLOWED_HIGH_RISK - Active high-risk flag (can rescan anytime)
        if (patient.highRiskFlag && !patient.pregnant) {
            return Result.success(
                RescanEligibility.Allowed(
                    reason = RescanReason.HIGH_RISK,
                    message = "Patient has active high-risk flag"
                )
            )
        }

        // RULE 3: ALLOWED_MATERNAL_HIGH_RISK - Pregnant + high-risk (can rescan anytime)
        if (patient.pregnant && patient.maternalHighRisk) {
            return Result.success(
                RescanEligibility.Allowed(
                    reason = RescanReason.MATERNAL_HIGH_RISK,
                    message = "Pregnant patient with high-risk flag"
                )
            )
        }

        // RULE 4: ALLOWED_MATERNAL - Pregnant, not high-risk (6-month interval = 183 days)
        if (patient.pregnant && !patient.maternalHighRisk) {
            val maternalIntervalDays = 183 // 6 months
            if (daysSinceLastScan >= maternalIntervalDays) {
                return Result.success(
                    RescanEligibility.Allowed(
                        reason = RescanReason.MATERNAL,
                        message = "Maternal follow-up scan (6-month interval met)"
                    )
                )
            } else {
                val daysRemaining = maternalIntervalDays - daysSinceLastScan
                return Result.success(
                    RescanEligibility.Rejected(
                        reason = "Pregnant patients without high-risk can rescan every 6 months",
                        daysUntilEligible = daysRemaining,
                        nextEligibleDate = calculateFutureDate(lastScan.scannedAt, maternalIntervalDays)
                    )
                )
            }
        }

        // RULE 5: ALLOWED_ANNUAL - Not pregnant, not high-risk (12-month interval = 365 days)
        val annualIntervalDays = 365 // 12 months
        if (daysSinceLastScan >= annualIntervalDays) {
            return Result.success(
                RescanEligibility.Allowed(
                    reason = RescanReason.ANNUAL,
                    message = "Annual checkup scan (12-month interval met)"
                )
            )
        }

        // RULE 6: REJECTED - None of the above conditions met
        val daysRemaining = annualIntervalDays - daysSinceLastScan
        return Result.success(
            RescanEligibility.Rejected(
                reason = "Standard patients can rescan once per year",
                daysUntilEligible = daysRemaining,
                nextEligibleDate = calculateFutureDate(lastScan.scannedAt, annualIntervalDays)
            )
        )
    }

    private fun calculateDaysSince(timestampMillis: Long): Int {
        val currentMillis = System.currentTimeMillis()
        val diffMillis = currentMillis - timestampMillis
        return (diffMillis / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun calculateFutureDate(fromMillis: Long, daysToAdd: Int): Long {
        return fromMillis + (daysToAdd * 24 * 60 * 60 * 1000L)
    }
}

/**
 * Rescan Eligibility Result
 */
sealed class RescanEligibility {
    data class Allowed(
        val reason: RescanReason,
        val message: String
    ) : RescanEligibility()

    data class Rejected(
        val reason: String,
        val daysUntilEligible: Int,
        val nextEligibleDate: Long
    ) : RescanEligibility()
}

/**
 * Rescan Reasons (for allowed scans)
 */
enum class RescanReason {
    BASELINE,                // First scan ever
    HIGH_RISK,              // Active high-risk flag
    MATERNAL_HIGH_RISK,     // Pregnant + high-risk
    MATERNAL,               // Pregnant (6-month interval)
    ANNUAL                  // Standard annual checkup
}
