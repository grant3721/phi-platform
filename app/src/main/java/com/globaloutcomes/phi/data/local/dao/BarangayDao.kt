package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.BarangayEntity
import kotlinx.coroutines.flow.Flow

/**
 * Barangay Data Access Object
 * Philippine administrative division data
 */
@Dao
interface BarangayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(barangay: BarangayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(barangays: List<BarangayEntity>)

    @Query("SELECT * FROM barangays WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): BarangayEntity?

    @Query("SELECT * FROM barangays WHERE code = :code LIMIT 1")
    fun getByCodeFlow(code: String): Flow<BarangayEntity?>

    @Query("SELECT * FROM barangays WHERE municipalityCode = :municipalityCode ORDER BY name ASC")
    fun getBarangaysByMunicipality(municipalityCode: String): Flow<List<BarangayEntity>>

    @Query("SELECT * FROM barangays ORDER BY name ASC")
    fun getAllBarangays(): Flow<List<BarangayEntity>>

    @Query("SELECT DISTINCT municipalityCode, municipalityName FROM barangays ORDER BY municipalityName ASC")
    fun getAllMunicipalities(): Flow<List<BarangayEntity>>

    @Query("SELECT * FROM barangays WHERE name LIKE '%' || :query || '%' OR municipalityName LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchBarangays(query: String): Flow<List<BarangayEntity>>

    @Query("SELECT COUNT(*) FROM barangays")
    suspend fun getCount(): Int

    @Query("DELETE FROM barangays")
    suspend fun deleteAll()
}
