package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.BarangayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BarangayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(barangays: List<BarangayEntity>)

    @Query("SELECT * FROM barangays ORDER BY name")
    fun getAllFlow(): Flow<List<BarangayEntity>>

    @Query("SELECT * FROM barangays WHERE province = :province ORDER BY name")
    fun getByProvinceFlow(province: String): Flow<List<BarangayEntity>>

    @Query("SELECT * FROM barangays WHERE id = :barangayId")
    suspend fun getById(barangayId: String): BarangayEntity?

    @Query("DELETE FROM barangays")
    suspend fun deleteAll()
}
