package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.repository.ScanRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Use case for checking if patient is eligible for rescan
 * Implements business rules:
 * - ALLOWED_BASELINE: New patient (no previous scans)
 * - ALLOWED_HIGH_RISK: Patient has active high-risk flag
 * - ALLOWED_MATERNAL_HIGH_RISK: Pregnant + high-risk
 * - ALLOWED_MATERNAL: Pregnant, 6-month interval
 * - ALLOWED_ANNUAL: 12-month interval
 * - REJECTED: Does not meet criteria
 */
class CheckRescanEligibilityUseCase @Inject constructor(
    private val scanRepository: ScanRepository
) {
    suspend operator fun invoke(patient: Patient): Result<EligibilityResult> {
        return try {
            // Get latest scan for patient
            val latestScan = scanRepository.getLatestScanForPatient(patient.id).getOrNull()

            // ALLOWED_BASELINE: New patient, no previous scans
            if (latestScan == null) {
                return Result.success(
                    EligibilityResult(
                        isEligible = true,
                        reason = EligibilityReason.BASELINE,
                        message = "New patient - baseline scan allowed",
                        daysUntilEligible = 0
                    )
                )
            }

            val now = System.currentTimeMillis()
            val daysSinceLastScan = TimeUnit.MILLISECONDS.toDays(now - latestScan.scannedAt).toInt()

            // ALLOWED_HIGH_RISK: Active high-risk flag (can rescan immediately)
            if (patient.highRiskFlag) {
                return Result.success(
                    EligibilityResult(
                        isEligible = true,
                        reason = EligibilityReason.HIGH_RISK,
                        message = "High-risk patient - immediate rescan allowed",
                        daysUntilEligible = 0
                    )
                )
            }

            // ALLOWED_MATERNAL_HIGH_RISK: Pregnant + high-risk (immediate)
            if (patient.isPregnant && patient.maternalHighRisk) {
                return Result.success(
                    EligibilityResult(
                        isEligible = true,
                        reason = EligibilityReason.MATERNAL_HIGH_RISK,
                        message = "Pregnant high-risk patient - immediate rescan allowed",
                        daysUntilEligible = 0
                    )
                )
            }

            // ALLOWED_MATERNAL: Pregnant, 6-month (180 days) interval
            if (patient.isPregnant && daysSinceLastScan >= 180) {
                return Result.success(
                    EligibilityResult(
                        isEligible = true,
                        reason = EligibilityReason.MATERNAL,
                        message = "Pregnant patient - 6-month interval met",
                        daysUntilEligible = 0
                    )
                )
            }

            // Check if pregnant but not yet 6 months
            if (patient.isPregnant && daysSinceLastScan < 180) {
                return Result.success(
                    EligibilityResult(
                        isEligible = false,
                        reason = EligibilityReason.MATERNAL_TOO_SOON,
                        message = "Pregnant patient must wait 6 months between scans",
                        daysUntilEligible = 180 - daysSinceLastScan
                    )
                )
            }

            // ALLOWED_ANNUAL: 12-month (365 days) interval
            if (daysSinceLastScan >= 365) {
                return Result.success(
                    EligibilityResult(
                        isEligible = true,
                        reason = EligibilityReason.ANNUAL,
                        message = "Annual scan interval met",
                        daysUntilEligible = 0
                    )
                )
            }

            // REJECTED: Does not meet any criteria
            Result.success(
                EligibilityResult(
                    isEligible = false,
                    reason = EligibilityReason.TOO_SOON,
                    message = "Must wait 12 months since last scan",
                    daysUntilEligible = 365 - daysSinceLastScan
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class EligibilityResult(
    val isEligible: Boolean,
    val reason: EligibilityReason,
    val message: String,
    val daysUntilEligible: Int
)

enum class EligibilityReason {
    BASELINE,                // New patient
    HIGH_RISK,              // High-risk flag
    MATERNAL_HIGH_RISK,     // Pregnant + high-risk
    MATERNAL,               // Pregnant, 6-month met
    ANNUAL,                 // 12-month met
    MATERNAL_TOO_SOON,      // Pregnant but < 6 months
    TOO_SOON                // < 12 months
}
