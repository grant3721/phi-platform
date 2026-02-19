package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.ScanEntity
import kotlinx.coroutines.flow.Flow

/**
 * Scan Data Access Object
 * Provides database operations for scan records
 */
@Dao
interface ScanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: ScanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scans: List<ScanEntity>)

    @Update
    suspend fun update(scan: ScanEntity)

    @Delete
    suspend fun delete(scan: ScanEntity)

    @Query("SELECT * FROM scans WHERE id = :scanId LIMIT 1")
    suspend fun getById(scanId: String): ScanEntity?

    @Query("SELECT * FROM scans WHERE patientId = :patientId ORDER BY scannedAt DESC")
    fun getScansForPatient(patientId: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE patientId = :patientId ORDER BY scannedAt DESC LIMIT 1")
    suspend fun getLatestScan(patientId: String): ScanEntity?

    @Query("SELECT * FROM scans WHERE bhwId = :bhwId AND validated = 1 AND date(scannedAt/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    fun getTodayValidatedCount(bhwId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM scans WHERE bhwId = :bhwId AND validated = 1 AND date(scannedAt/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    suspend fun getTodayValidatedCountSync(bhwId: String): Int

    @Query("SELECT COUNT(*) FROM scans WHERE validated = 1")
    fun getTotalValidatedScans(): Flow<Int>

    @Query("SELECT * FROM scans WHERE syncStatus = :status")
    fun getScansBySyncStatus(status: String): Flow<List<ScanEntity>>

    @Query("UPDATE scans SET syncStatus = :status WHERE id = :scanId")
    suspend fun updateSyncStatus(scanId: String, status: String)

    @Query("SELECT * FROM scans WHERE validated = 1 AND scannedAt >= :startDate ORDER BY scannedAt DESC")
    fun getValidatedScansSince(startDate: Long): Flow<List<ScanEntity>>

    @Query("DELETE FROM scans WHERE patientId = :patientId")
    suspend fun deleteAllForPatient(patientId: String)
}
