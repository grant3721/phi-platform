package com.globaloutcomes.phi.data.repository

import com.globaloutcomes.phi.data.local.dao.ReferralDao
import com.globaloutcomes.phi.data.local.entities.ReferralEntity
import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Referral Repository Implementation
 */
class ReferralRepositoryImpl @Inject constructor(
    private val referralDao: ReferralDao
) : ReferralRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun insertReferral(referral: Referral): Result<String> = runCatching {
        referralDao.insert(referral.toEntity())
        referral.id
    }

    override suspend fun updateReferral(referral: Referral): Result<Unit> = runCatching {
        referralDao.update(referral.toEntity())
    }

    override suspend fun getReferralById(referralId: String): Result<Referral?> = runCatching {
        referralDao.getById(referralId)?.toDomain()
    }

    override suspend fun getReferralsByPatientId(patientId: String): Result<List<Referral>> = runCatching {
        // Flow method needs to be collected - for now return empty list
        emptyList<Referral>() // TODO: Add suspend version to DAO
    }

    override fun getAllReferralsFlow(): Flow<List<Referral>> {
        // TODO: Add to DAO
        return kotlinx.coroutines.flow.flow { emit(emptyList<Referral>()) }
    }

    override fun getActiveReferralsFlow(): Flow<List<Referral>> {
        return referralDao.getActiveReferralsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReferralsBySyncStatus(status: SyncStatus): Result<List<Referral>> = runCatching {
        referralDao.getBySyncStatus(status.name).map { it.toDomain() }
    }

    override suspend fun updateSyncStatus(referralId: String, status: SyncStatus, syncedAt: Long): Result<Unit> = runCatching {
        referralDao.updateSyncStatus(referralId, status.name, syncedAt)
    }

    // Mapping functions
    private fun Referral.toEntity() = ReferralEntity(
        id = id,
        patientId = patientId,
        scanId = scanId,
        tier = tier.name,
        status = status.name,
        priority = "ROUTINE", // Default priority
        riskLevel = riskLevel.name,
        riskFlags = json.encodeToString(riskFlags),
        reasonForReferral = riskFlags.joinToString(", "),
        referredAt = referredAt,
        dueBy = dueBy,
        confirmedAt = null,
        resolvedAt = resolvedAt,
        resolutionNotes = resolutionNotes,
        outcome = null,
        fromFacility = "BARANGAY",
        toFacility = when (tier) {
            ReferralTier.BHW_TO_BHS -> "BHS"
            ReferralTier.BHS_TO_RHU -> "RHU"
            ReferralTier.RHU_TO_HOSPITAL -> "HOSPITAL"
        },
        referringProvider = referredBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus.name,
        syncedAt = syncedAt,
        serverReferralId = null
    )

    private fun ReferralEntity.toDomain() = Referral(
        id = id,
        patientId = patientId,
        scanId = scanId,
        tier = ReferralTier.valueOf(tier),
        status = ReferralStatus.valueOf(status),
        riskLevel = RiskLevel.valueOf(riskLevel),
        riskFlags = try {
            json.decodeFromString<List<String>>(riskFlags)
        } catch (e: Exception) {
            emptyList()
        },
        referredBy = referringProvider,
        referredAt = referredAt,
        dueBy = dueBy,
        resolvedAt = resolvedAt,
        resolvedBy = null,
        resolutionNotes = resolutionNotes,
        syncStatus = SyncStatus.valueOf(syncStatus),
        syncedAt = syncedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
