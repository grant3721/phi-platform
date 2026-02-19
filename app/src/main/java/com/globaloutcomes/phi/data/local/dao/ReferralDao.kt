package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.ReferralEntity
import kotlinx.coroutines.flow.Flow

/**
 * Referral Data Access Object
 */
@Dao
interface ReferralDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(referral: ReferralEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(referrals: List<ReferralEntity>)

    @Update
    suspend fun update(referral: ReferralEntity)

    @Query("SELECT * FROM referrals WHERE id = :referralId LIMIT 1")
    suspend fun getById(referralId: String): ReferralEntity?

    @Query("SELECT * FROM referrals WHERE id = :referralId LIMIT 1")
    fun getByIdFlow(referralId: String): Flow<ReferralEntity?>

    @Query("SELECT * FROM referrals WHERE patientId = :patientId ORDER BY referredAt DESC")
    fun getReferralsForPatient(patientId: String): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals WHERE status = 'pending' AND tier = :tier ORDER BY referredAt ASC")
    fun getPendingByTier(tier: String): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals WHERE status = 'pending' AND dueBy < :now")
    fun getOverdueReferrals(now: Long): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals WHERE status = 'pending'")
    fun getAllPendingReferrals(): Flow<List<ReferralEntity>>

    @Query("SELECT COUNT(*) FROM referrals WHERE status = 'pending' AND tier = :tier")
    fun getPendingCountByTier(tier: String): Flow<Int>

    @Query("SELECT * FROM referrals WHERE syncStatus = :status")
    fun getReferralsBySyncStatus(status: String): Flow<List<ReferralEntity>>

    @Query("UPDATE referrals SET syncStatus = :status WHERE id = :referralId")
    suspend fun updateSyncStatus(referralId: String, status: String)

    @Query("UPDATE referrals SET status = 'overdue' WHERE status = 'pending' AND dueBy < :now")
    suspend fun markOverdueReferrals(now: Long): Int
}
