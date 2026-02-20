package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.ConfigThresholdEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigThresholdDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(threshold: ConfigThresholdEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(thresholds: List<ConfigThresholdEntity>)

    @Query("SELECT * FROM config_thresholds WHERE active = 1")
    fun getActiveThresholdsFlow(): Flow<List<ConfigThresholdEntity>>

    @Query("SELECT * FROM config_thresholds WHERE parameterName = :parameterName AND active = 1")
    suspend fun getByParameter(parameterName: String): ConfigThresholdEntity?

    @Query("DELETE FROM config_thresholds")
    suspend fun deleteAll()
}
