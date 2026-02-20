package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.BhwIncentiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BhwIncentiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(incentive: BhwIncentiveEntity): Long

    @Query("SELECT * FROM bhw_incentives WHERE bhwId = :bhwId AND earnedDate = :date ORDER BY earnedAt")
    suspend fun getForDay(bhwId: String, date: Long): List<BhwIncentiveEntity>

    @Query("SELECT SUM(amount) FROM bhw_incentives WHERE bhwId = :bhwId AND earnedDate = :date AND status = 'EARNED'")
    suspend fun getTotalEarnedForDay(bhwId: String, date: Long): Float?

    @Query("SELECT COUNT(*) FROM bhw_incentives WHERE bhwId = :bhwId AND earnedDate = :date AND status = 'EARNED'")
    suspend fun getEarnedCountForDay(bhwId: String, date: Long): Int

    @Query("SELECT * FROM bhw_incentives WHERE bhwId = :bhwId ORDER BY earnedAt DESC")
    fun getAllForBhwFlow(bhwId: String): Flow<List<BhwIncentiveEntity>>

    @Query("DELETE FROM bhw_incentives")
    suspend fun deleteAll()
}
