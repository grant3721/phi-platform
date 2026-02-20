package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.data.local.dao.BhwIncentiveDao
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case for checking if BHW has reached daily cap
 * ₱3.00 per scan, ₱150.00 daily cap (50 scans maximum)
 */
class CheckDailyCapUseCase @Inject constructor(
    private val bhwIncentiveDao: BhwIncentiveDao
) {
    suspend operator fun invoke(bhwId: String): Result<DailyCapResult> {
        return try {
            val startOfDay = getStartOfDay()
            val scansToday = bhwIncentiveDao.getEarnedCountForDay(bhwId, startOfDay)
            val amountToday = bhwIncentiveDao.getTotalEarnedForDay(bhwId, startOfDay) ?: 0f

            val isAtCap = scansToday >= MAX_SCANS_PER_DAY
            val remainingScans = if (isAtCap) 0 else MAX_SCANS_PER_DAY - scansToday
            val remainingAmount = if (isAtCap) 0f else MAX_DAILY_AMOUNT - amountToday

            Result.success(
                DailyCapResult(
                    scansToday = scansToday,
                    amountToday = amountToday,
                    isAtCap = isAtCap,
                    remainingScans = remainingScans,
                    remainingAmount = remainingAmount,
                    message = if (isAtCap) {
                        "Daily cap reached (₱150.00 / 50 scans)"
                    } else {
                        "$remainingScans scans remaining (₱${"%.2f".format(remainingAmount)} left)"
                    }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    companion object {
        const val MAX_SCANS_PER_DAY = 50
        const val MAX_DAILY_AMOUNT = 150.00f
        const val AMOUNT_PER_SCAN = 3.00f
    }
}

data class DailyCapResult(
    val scansToday: Int,
    val amountToday: Float,
    val isAtCap: Boolean,
    val remainingScans: Int,
    val remainingAmount: Float,
    val message: String
)
