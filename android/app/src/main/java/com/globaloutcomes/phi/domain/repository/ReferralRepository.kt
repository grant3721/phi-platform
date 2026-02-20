package com.globaloutcomes.phi.domain.repository

import com.globaloutcomes.phi.domain.model.Referral
import com.globaloutcomes.phi.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Referral Repository Interface
 */
interface ReferralRepository {

    suspend fun insertReferral(referral: Referral): Result<String>

    suspend fun updateReferral(referral: Referral): Result<Unit>

    suspend fun getReferralById(referralId: String): Result<Referral?>

    suspend fun getReferralsByPatientId(patientId: String): Result<List<Referral>>

    fun getAllReferralsFlow(): Flow<List<Referral>>

    fun getActiveReferralsFlow(): Flow<List<Referral>>

    suspend fun getReferralsBySyncStatus(status: SyncStatus): Result<List<Referral>>

    suspend fun updateSyncStatus(referralId: String, status: SyncStatus, syncedAt: Long): Result<Unit>
}
