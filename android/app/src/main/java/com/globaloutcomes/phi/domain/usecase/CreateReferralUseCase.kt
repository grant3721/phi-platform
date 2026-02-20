package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to create a referral for a high-risk scan
 */
class CreateReferralUseCase @Inject constructor(
    private val referralRepository: ReferralRepository,
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(
        patientId: String,
        scanId: String,
        riskLevel: RiskLevel,
        riskFlags: List<String>
    ): Result<String> {
        return try {
            // Only create referrals for elevated or high risk
            if (riskLevel == RiskLevel.NORMAL) {
                return Result.failure(IllegalArgumentException("Normal risk scans do not require referrals"))
            }

            val currentTime = System.currentTimeMillis()
            val dueBy = currentTime + (48 * 60 * 60 * 1000) // 48 hours from now

            val referral = Referral(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                scanId = scanId,
                tier = ReferralTier.BHW_TO_BHS, // Always starts at BHW → BHS
                status = ReferralStatus.PENDING,
                referredAt = currentTime,
                dueBy = dueBy,
                riskLevel = riskLevel,
                riskFlags = riskFlags,
                resolvedAt = null,
                resolvedBy = null,
                resolutionNotes = null,
                createdAt = currentTime,
                updatedAt = currentTime,
                syncStatus = SyncStatus.PENDING,
                syncedAt = null
            )

            // Save referral
            val result = referralRepository.insertReferral(referral)
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Failed to save referral"))
            }

            // Update patient risk status
            patientRepository.updateRiskStatus(
                patientId = patientId,
                highRiskFlag = true,
                highRiskReasons = riskFlags,
                timestamp = currentTime
            )

            Result.success(referral.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
