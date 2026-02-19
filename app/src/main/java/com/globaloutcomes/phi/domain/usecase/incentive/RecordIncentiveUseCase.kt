package com.globaloutcomes.phi.domain.usecase.incentive

import com.globaloutcomes.phi.domain.repository.ScanRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Record Incentive Use Case
 *
 * Records BHW incentive earnings for validated scans:
 * - ₱3.00 per validated scan
 * - ₱150.00 daily cap (50 scans maximum)
 *
 * Business Rules:
 * 1. Only VALIDATED scans earn incentives
 * 2. Maximum 50 scans per day per BHW
 * 3. Earnings cap at ₱150.00 per day
 * 4. Scans beyond cap still recorded but earn ₱0.00
 * 5. Status: "earned" (counted toward cap) or "rejected" (over cap)
 *
 * This use case is called after scan sync/validation from server
 */
class RecordIncentiveUseCase @Inject constructor(
    private val scanRepository: ScanRepository,
    private val checkDailyCapUseCase: CheckDailyCapUseCase
) {

    companion object {
        const val AMOUNT_PER_SCAN = 3.0  // ₱3.00
        const val DAILY_CAP = 150.0      // ₱150.00
        const val MAX_SCANS_PER_DAY = 50  // 150 / 3 = 50 scans
    }

    suspend operator fun invoke(
        scanId: String,
        bhwId: String,
        isValidated: Boolean
    ): Result<IncentiveRecord> {
        // Only validated scans earn incentives
        if (!isValidated) {
            return Result.success(
                IncentiveRecord(
                    scanId = scanId,
                    bhwId = bhwId,
                    amount = 0.0,
                    status = IncentiveStatus.REJECTED,
                    reason = "Scan not validated"
                )
            )
        }

        // Check if BHW has reached daily cap
        val capCheck = checkDailyCapUseCase(bhwId)
        if (capCheck.isFailure) {
            return Result.failure(capCheck.exceptionOrNull()!!)
        }

        val capStatus = capCheck.getOrThrow()

        return if (capStatus.hasReachedCap) {
            // Over daily cap - record but don't pay
            Result.success(
                IncentiveRecord(
                    scanId = scanId,
                    bhwId = bhwId,
                    amount = 0.0,
                    status = IncentiveStatus.REJECTED,
                    reason = "Daily cap reached (${MAX_SCANS_PER_DAY} scans / ₱${DAILY_CAP})"
                )
            )
        } else {
            // Within cap - award incentive
            Result.success(
                IncentiveRecord(
                    scanId = scanId,
                    bhwId = bhwId,
                    amount = AMOUNT_PER_SCAN,
                    status = IncentiveStatus.EARNED,
                    reason = "Scan ${capStatus.scansToday + 1} of ${MAX_SCANS_PER_DAY} today"
                )
            )
        }
    }
}

/**
 * Incentive Record Result
 */
data class IncentiveRecord(
    val scanId: String,
    val bhwId: String,
    val amount: Double,
    val status: IncentiveStatus,
    val reason: String
)

enum class IncentiveStatus {
    EARNED,    // Counted toward cap and paid
    REJECTED   // Not paid (validation failed or over cap)
}
