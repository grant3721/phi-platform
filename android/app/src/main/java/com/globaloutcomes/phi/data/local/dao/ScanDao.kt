package com.globaloutcomes.phi.data.local.dao

import androidx.room.*
import com.globaloutcomes.phi.data.local.entities.ScanEntity
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM scans WHERE id = :scanId")
    suspend fun getById(scanId: String): ScanEntity?

    @Query("SELECT * FROM scans WHERE id = :scanId")
    fun getByIdFlow(scanId: String): Flow<ScanEntity?>

    @Query("SELECT * FROM scans WHERE patientId = :patientId ORDER BY scannedAt DESC")
    fun getByPatientFlow(patientId: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE patientId = :patientId ORDER BY scannedAt DESC")
    suspend fun getByPatient(patientId: String): List<ScanEntity>

    @Query("SELECT * FROM scans WHERE patientId = :patientId AND scannedAt >= :since ORDER BY scannedAt DESC")
    suspend fun getByPatientSince(patientId: String, since: Long): List<ScanEntity>

    @Query("SELECT * FROM scans WHERE patientId = :patientId ORDER BY scannedAt DESC LIMIT 1")
    suspend fun getLatestForPatient(patientId: String): ScanEntity?

    @Query("SELECT * FROM scans WHERE riskLevel IN ('ELEVATED', 'HIGH') ORDER BY scannedAt DESC")
    fun getHighRiskScansFlow(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE scannedAt >= :startOfDay AND scannedAt < :endOfDay")
    suspend fun getScansForDay(startOfDay: Long, endOfDay: Long): List<ScanEntity>

    @Query("SELECT COUNT(*) FROM scans WHERE scannedAt >= :startOfDay AND scannedAt < :endOfDay")
    suspend fun getCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT * FROM scans WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: String): List<ScanEntity>

    @Query("UPDATE scans SET syncStatus = :status, syncedAt = :syncedAt, serverScanId = :serverId WHERE id = :scanId")
    suspend fun updateSyncStatus(scanId: String, status: String, syncedAt: Long, serverId: String?)

    @Query("DELETE FROM scans WHERE id IN (:scanIds)")
    suspend fun deleteByIds(scanIds: List<String>)

    @Query("DELETE FROM scans")
    suspend fun deleteAll()
}
