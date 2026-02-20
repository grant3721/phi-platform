package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.YakapClaimEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface YakapClaimDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(claim: YakapClaimEntity): Long

    @Query("SELECT * FROM yakap_claims WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getByPatientFlow(patientId: String): Flow<List<YakapClaimEntity>>

    @Query("SELECT * FROM yakap_claims WHERE claimStatus = :status")
    fun getByStatusFlow(status: String): Flow<List<YakapClaimEntity>>

    @Query("SELECT * FROM yakap_claims WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: String): List<YakapClaimEntity>

    @Query("DELETE FROM yakap_claims")
    suspend fun deleteAll()
}
