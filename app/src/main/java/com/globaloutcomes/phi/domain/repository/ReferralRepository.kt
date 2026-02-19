package com.globaloutcomes.phi.domain.repository

import com.globaloutcomes.phi.domain.model.Referral
import com.globaloutcomes.phi.domain.model.ReferralStatus
import kotlinx.coroutines.flow.Flow

/**
 * Referral Repository Interface
 */
interface ReferralRepository {

    suspend fun insert(referral: Referral): Result<Unit>

    suspend fun update(referral: Referral): Result<Unit>

    suspend fun delete(referralId: String): Result<Unit>

    suspend fun getById(referralId: String): Result<Referral?>

    suspend fun getByPatientId(patientId: String): Result<List<Referral>>

    suspend fun getPendingReferrals(): Result<List<Referral>>

    suspend fun getOverdueReferrals(): Result<List<Referral>>

    fun observeAll(): Flow<Result<List<Referral>>>

    fun observeByStatus(status: ReferralStatus): Flow<Result<List<Referral>>>

    suspend fun getAll(): Result<List<Referral>>
}
