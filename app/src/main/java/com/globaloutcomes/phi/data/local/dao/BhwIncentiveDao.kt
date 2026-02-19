package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.BhwIncentiveEntity
import kotlinx.coroutines.flow.Flow

/**
 * BHW Incentive Data Access Object
 * Critical for daily cap enforcement (₱150 max)
 */
@Dao
interface BhwIncentiveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(incentive: BhwIncentiveEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(incentives: List<BhwIncentiveEntity>)

    @Query("SELECT * FROM bhw_incentives WHERE id = :incentiveId LIMIT 1")
    suspend fun getById(incentiveId: String): BhwIncentiveEntity?

    @Query("SELECT * FROM bhw_incentives WHERE bhwId = :bhwId ORDER BY date DESC")
    fun getIncentivesForBhw(bhwId: String): Flow<List<BhwIncentiveEntity>>

    @Query("SELECT SUM(amount) FROM bhw_incentives WHERE bhwId = :bhwId AND date = :todayEpoch")
    suspend fun getDailyTotal(bhwId: String, todayEpoch: Long): Double?

    @Query("SELECT SUM(amount) FROM bhw_incentives WHERE bhwId = :bhwId AND date = :todayEpoch")
    fun getDailyTotalFlow(bhwId: String, todayEpoch: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM bhw_incentives WHERE bhwId = :bhwId AND date >= :monthStartEpoch")
    suspend fun getMonthlyTotal(bhwId: String, monthStartEpoch: Long): Double?

    @Query("SELECT SUM(amount) FROM bhw_incentives WHERE bhwId = :bhwId AND date >= :monthStartEpoch")
    fun getMonthlyTotalFlow(bhwId: String, monthStartEpoch: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM bhw_incentives WHERE bhwId = :bhwId AND date = :todayEpoch AND status = 'earned'")
    suspend fun getTodayEarnedCount(bhwId: String, todayEpoch: Long): Int

    @Query("SELECT COUNT(*) FROM bhw_incentives WHERE bhwId = :bhwId AND date = :todayEpoch AND status = 'earned'")
    fun getTodayEarnedCountFlow(bhwId: String, todayEpoch: Long): Flow<Int>

    @Query("SELECT * FROM bhw_incentives WHERE bhwId = :bhwId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getIncentivesInDateRange(bhwId: String, startDate: Long, endDate: Long): Flow<List<BhwIncentiveEntity>>

    @Query("SELECT date, SUM(amount) as total FROM bhw_incentives WHERE bhwId = :bhwId AND date >= :monthStartEpoch GROUP BY date ORDER BY date ASC")
    fun getDailyTotalsForMonth(bhwId: String, monthStartEpoch: Long): Flow<Map<Long, Double>>
}
