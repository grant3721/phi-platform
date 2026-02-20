package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.model.Referral
import com.globaloutcomes.phi.domain.model.ReferralStatus
import com.globaloutcomes.phi.domain.model.ReferralTier
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import javax.inject.Inject

/**
 * Use case to update referral status and escalate to next tier
 */
class UpdateReferralUseCase @Inject constructor(
    private val referralRepository: ReferralRepository
) {
    /**
     * Updates referral status
     */
    suspend fun updateStatus(
        referralId: String,
        status: ReferralStatus,
        notes: String? = null,
        resolvedBy: String? = null
    ): Result<Unit> {
        return try {
            val referral = referralRepository.getReferralById(referralId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Referral not found"))

            val currentTime = System.currentTimeMillis()

            val updatedReferral = referral.copy(
                status = status,
                resolvedAt = if (status == ReferralStatus.RESOLVED) currentTime else referral.resolvedAt,
                resolvedBy = resolvedBy ?: referral.resolvedBy,
                resolutionNotes = notes ?: referral.resolutionNotes,
                updatedAt = currentTime
            )

            referralRepository.updateReferral(updatedReferral)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Escalates referral to next tier (BHS → RHU)
     */
    suspend fun escalateToRhu(
        referralId: String
    ): Result<Unit> {
        return try {
            val referral = referralRepository.getReferralById(referralId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Referral not found"))

            if (referral.tier != ReferralTier.BHW_TO_BHS) {
                return Result.failure(IllegalArgumentException("Can only escalate BHW_TO_BHS referrals"))
            }

            val currentTime = System.currentTimeMillis()
            val dueBy = currentTime + (48 * 60 * 60 * 1000) // 48 hours from now

            val updatedReferral = referral.copy(
                tier = ReferralTier.BHS_TO_RHU,
                status = ReferralStatus.CONFIRMED, // Confirmed for escalation
                dueBy = dueBy,
                updatedAt = currentTime
            )

            referralRepository.updateReferral(updatedReferral)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marks referral as resolved
     */
    suspend fun resolveReferral(
        referralId: String,
        resolutionNotes: String,
        resolvedBy: String
    ): Result<Unit> {
        return try {
            val referral = referralRepository.getReferralById(referralId).getOrNull()
                ?: return Result.failure(IllegalArgumentException("Referral not found"))

            val currentTime = System.currentTimeMillis()

            val updatedReferral = referral.copy(
                status = ReferralStatus.RESOLVED,
                resolvedAt = currentTime,
                resolvedBy = resolvedBy,
                resolutionNotes = resolutionNotes,
                updatedAt = currentTime
            )

            referralRepository.updateReferral(updatedReferral)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
