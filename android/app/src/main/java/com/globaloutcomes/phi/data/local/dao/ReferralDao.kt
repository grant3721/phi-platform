package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.ReferralEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferralDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(referral: ReferralEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(referrals: List<ReferralEntity>)

    @Update
    suspend fun update(referral: ReferralEntity)

    @Query("SELECT * FROM referrals WHERE id = :referralId")
    suspend fun getById(referralId: String): ReferralEntity?

    @Query("SELECT * FROM referrals WHERE patientId = :patientId ORDER BY referredAt DESC")
    fun getByPatientFlow(patientId: String): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals WHERE status IN ('PENDING', 'CONFIRMED') ORDER BY dueBy ASC")
    fun getActiveReferralsFlow(): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals WHERE status = 'PENDING' AND dueBy < :currentTime")
    suspend fun getOverdueReferrals(currentTime: Long): List<ReferralEntity>

    @Query("UPDATE referrals SET status = :status, updatedAt = :timestamp WHERE id = :referralId")
    suspend fun updateStatus(referralId: String, status: String, timestamp: Long)

    @Query("SELECT * FROM referrals WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: String): List<ReferralEntity>

    @Query("UPDATE referrals SET syncStatus = :status, syncedAt = :syncedAt WHERE id = :referralId")
    suspend fun updateSyncStatus(referralId: String, status: String, syncedAt: Long)

    @Query("DELETE FROM referrals")
    suspend fun deleteAll()
}
