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
 * Maps between ReferralEntity and Referral domain model
 */
class ReferralRepositoryImpl @Inject constructor(
    private val referralDao: ReferralDao,
    private val json: Json
) : ReferralRepository {

    override suspend fun insert(referral: Referral): Result<Unit> {
        return try {
            referralDao.insert(referral.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun update(referral: Referral): Result<Unit> {
        return try {
            referralDao.update(referral.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(referralId: String): Result<Unit> {
        return try {
            referralDao.deleteById(referralId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getById(referralId: String): Result<Referral?> {
        return try {
            val entity = referralDao.getById(referralId)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getByPatientId(patientId: String): Result<List<Referral>> {
        return try {
            val entities = referralDao.getByPatientId(patientId)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingReferrals(): Result<List<Referral>> {
        return try {
            val entities = referralDao.getByStatus(ReferralStatus.PENDING.name)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOverdueReferrals(): Result<List<Referral>> {
        return try {
            val now = System.currentTimeMillis()
            val entities = referralDao.getOverdue(now)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAll(): Flow<Result<List<Referral>>> {
        return referralDao.observeAll().map { entities ->
            try {
                Result.success(entities.map { it.toDomain() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun observeByStatus(status: ReferralStatus): Flow<Result<List<Referral>>> {
        return referralDao.observeByStatus(status.name).map { entities ->
            try {
                Result.success(entities.map { it.toDomain() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAll(): Result<List<Referral>> {
        return try {
            val entities = referralDao.getAll()
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Entity <-> Domain mapping
    private fun Referral.toEntity(): ReferralEntity {
        return ReferralEntity(
            id = id,
            patientId = patientId,
            scanId = scanId,
            tier = tier.name,
            status = status.name,
            riskFlags = json.encodeToString(riskFlags),
            notes = notes,
            referredBy = referredBy,
            referredAt = referredAt,
            dueBy = dueBy,
            resolvedAt = resolvedAt,
            resolvedBy = resolvedBy,
            resolutionNotes = resolutionNotes,
            syncStatus = syncStatus.name
        )
    }

    private fun ReferralEntity.toDomain(): Referral {
        return Referral(
            id = id,
            patientId = patientId,
            scanId = scanId,
            tier = ReferralTier.valueOf(tier),
            status = ReferralStatus.valueOf(status),
            riskFlags = try {
                json.decodeFromString<List<String>>(riskFlags)
            } catch (e: Exception) {
                emptyList()
            },
            notes = notes,
            referredBy = referredBy,
            referredAt = referredAt,
            dueBy = dueBy,
            resolvedAt = resolvedAt,
            resolvedBy = resolvedBy,
            resolutionNotes = resolutionNotes,
            syncStatus = SyncStatus.valueOf(syncStatus)
        )
    }
}
