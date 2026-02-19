package com.globaloutcomes.phi.domain.usecase.referral

import com.globaloutcomes.phi.domain.model.Referral
import com.globaloutcomes.phi.domain.model.ReferralStatus
import com.globaloutcomes.phi.domain.model.ReferralTier
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Create Referral Use Case
 *
 * Automatically creates referral when high-risk scan is detected.
 * Should be called after AssessRiskUseCase determines patient is high-risk.
 *
 * Business Rules:
 * - Initial referral tier: BHW_TO_BHS
 * - Initial status: PENDING
 * - Due date: 48 hours from referral time
 * - Risk flags copied from scan
 * - Referred by: Current BHW (from scan.bhwId)
 */
class CreateReferralUseCase @Inject constructor(
    private val referralRepository: ReferralRepository,
    private val scanRepository: ScanRepository
) {

    companion object {
        const val DUE_HOURS = 48 // 48 hours to respond
    }

    suspend operator fun invoke(
        scanId: String,
        riskFlags: List<String>,
        notes: String? = null
    ): Result<Referral> {
        // Load scan to get patient and BHW info
        val scanResult = scanRepository.getById(scanId)
        if (scanResult.isFailure) {
            return Result.failure(scanResult.exceptionOrNull()!!)
        }

        val scan = scanResult.getOrNull()
            ?: return Result.failure(IllegalArgumentException("Scan not found"))

        // Create referral
        val now = System.currentTimeMillis()
        val dueBy = now + (DUE_HOURS * 60 * 60 * 1000) // 48 hours from now

        val referral = Referral(
            id = UUID.randomUUID().toString(),
            patientId = scan.patientId,
            scanId = scanId,
            tier = ReferralTier.BHW_TO_BHS,
            status = ReferralStatus.PENDING,
            riskFlags = riskFlags,
            notes = notes,
            referredBy = scan.bhwId,
            referredAt = now,
            dueBy = dueBy,
            resolvedAt = null,
            resolvedBy = null,
            resolutionNotes = null,
            syncStatus = SyncStatus.PENDING
        )

        // Save referral
        return referralRepository.insert(referral).map { referral }
    }
}
