package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.ConfigThresholdEntity
import kotlinx.coroutines.flow.Flow

/**
 * Config Threshold Data Access Object
 * Server-managed risk thresholds
 */
@Dao
interface ConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(threshold: ConfigThresholdEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(thresholds: List<ConfigThresholdEntity>)

    @Update
    suspend fun update(threshold: ConfigThresholdEntity)

    @Query("SELECT * FROM config_thresholds WHERE key = :key LIMIT 1")
    suspend fun getByKey(key: String): ConfigThresholdEntity?

    @Query("SELECT * FROM config_thresholds WHERE key = :key LIMIT 1")
    fun getByKeyFlow(key: String): Flow<ConfigThresholdEntity?>

    @Query("SELECT value FROM config_thresholds WHERE key = :key LIMIT 1")
    suspend fun getValue(key: String): Double?

    @Query("SELECT * FROM config_thresholds ORDER BY key ASC")
    fun getAllThresholds(): Flow<List<ConfigThresholdEntity>>

    @Query("DELETE FROM config_thresholds")
    suspend fun deleteAll()
}
