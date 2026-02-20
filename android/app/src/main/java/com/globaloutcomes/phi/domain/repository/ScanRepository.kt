package com.globaloutcomes.phi.domain.repository

import com.globaloutcomes.phi.domain.model.Scan
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Scan operations
 */
interface ScanRepository {

    suspend fun insertScan(scan: Scan): Result<String>

    suspend fun getScanById(scanId: String): Result<Scan?>

    fun getScanByIdFlow(scanId: String): Flow<Scan?>

    fun getScansByPatientFlow(patientId: String): Flow<List<Scan>>

    suspend fun getScansForPatient(patientId: String): Result<List<Scan>>

    suspend fun getLatestScanForPatient(patientId: String): Result<Scan?>

    suspend fun getScansSince(patientId: String, since: Long): Result<List<Scan>>

    suspend fun getScansForDay(startOfDay: Long, endOfDay: Long): Result<List<Scan>>

    suspend fun getScanCountForDay(startOfDay: Long, endOfDay: Long): Result<Int>

    fun getHighRiskScansFlow(): Flow<List<Scan>>

    suspend fun getScansBySyncStatus(status: com.globaloutcomes.phi.domain.model.SyncStatus): Result<List<Scan>>

    suspend fun updateSyncStatus(scanId: String, status: com.globaloutcomes.phi.domain.model.SyncStatus, syncedAt: Long): Result<Unit>

    suspend fun deleteScan(scanId: String): Result<Unit>
}
