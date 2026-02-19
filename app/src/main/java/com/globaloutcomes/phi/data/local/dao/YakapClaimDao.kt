package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.YakapClaimEntity
import kotlinx.coroutines.flow.Flow

/**
 * YAKAP Claim Data Access Object
 */
@Dao
interface YakapClaimDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(claim: YakapClaimEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(claims: List<YakapClaimEntity>)

    @Update
    suspend fun update(claim: YakapClaimEntity)

    @Query("SELECT * FROM yakap_claims WHERE id = :claimId LIMIT 1")
    suspend fun getById(claimId: String): YakapClaimEntity?

    @Query("SELECT * FROM yakap_claims WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getClaimsForPatient(patientId: String): Flow<List<YakapClaimEntity>>

    @Query("SELECT * FROM yakap_claims WHERE encounterId = :encounterId LIMIT 1")
    suspend fun getClaimForEncounter(encounterId: String): YakapClaimEntity?

    @Query("SELECT * FROM yakap_claims WHERE status = :status ORDER BY createdAt DESC")
    fun getClaimsByStatus(status: String): Flow<List<YakapClaimEntity>>

    @Query("SELECT * FROM yakap_claims WHERE status IN ('draft', 'ready') ORDER BY createdAt DESC")
    fun getDraftAndReadyClaims(): Flow<List<YakapClaimEntity>>

    @Query("SELECT * FROM yakap_claims WHERE syncStatus = :status")
    fun getClaimsBySyncStatus(status: String): Flow<List<YakapClaimEntity>>

    @Query("UPDATE yakap_claims SET syncStatus = :status WHERE id = :claimId")
    suspend fun updateSyncStatus(claimId: String, status: String)
}
