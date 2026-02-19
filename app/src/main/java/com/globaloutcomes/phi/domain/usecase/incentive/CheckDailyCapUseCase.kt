package com.globaloutcomes.phi.domain.usecase.incentive

import com.globaloutcomes.phi.domain.repository.ScanRepository
import javax.inject.Inject

/**
 * Check Daily Cap Use Case
 *
 * Checks if BHW has reached daily incentive cap:
 * - Maximum 50 validated scans per day
 * - Maximum ₱150.00 earnings per day
 *
 * Returns current count and whether cap is reached
 */
class CheckDailyCapUseCase @Inject constructor(
    private val scanRepository: ScanRepository
) {

    companion object {
        const val MAX_SCANS_PER_DAY = 50
        const val DAILY_CAP = 150.0
    }

    suspend operator fun invoke(bhwId: String): Result<DailyCapStatus> {
        // Get today's scans for this BHW
        val scansResult = scanRepository.getScansToday()
        if (scansResult.isFailure) {
            return Result.failure(scansResult.exceptionOrNull()!!)
        }

        val todaysScans = scansResult.getOrThrow()

        // Count only validated scans by this BHW
        val validatedScans = todaysScans.filter {
            it.bhwId == bhwId && it.validated
        }

        val scansToday = validatedScans.size
        val earningsToday = scansToday * 3.0 // ₱3 per scan
        val hasReachedCap = scansToday >= MAX_SCANS_PER_DAY

        return Result.success(
            DailyCapStatus(
                scansToday = scansToday,
                earningsToday = earningsToday.coerceAtMost(DAILY_CAP),
                hasReachedCap = hasReachedCap,
                scansRemaining = if (hasReachedCap) 0 else (MAX_SCANS_PER_DAY - scansToday)
            )
        )
    }
}

/**
 * Daily Cap Status
 */
data class DailyCapStatus(
    val scansToday: Int,
    val earningsToday: Double,
    val hasReachedCap: Boolean,
    val scansRemaining: Int
)
