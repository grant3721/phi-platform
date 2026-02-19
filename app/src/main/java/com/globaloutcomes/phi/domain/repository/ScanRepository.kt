package com.globaloutcomes.phi.domain.repository

import com.globaloutcomes.phi.domain.model.Scan
import kotlinx.coroutines.flow.Flow

/**
 * Scan Repository Interface - Clean Architecture
 * Defines contract for scan data operations
 */
interface ScanRepository {

    suspend fun insert(scan: Scan): Result<Long>

    suspend fun update(scan: Scan): Result<Unit>

    suspend fun getById(scanId: String): Result<Scan?>

    fun getScansForPatient(patientId: String): Flow<List<Scan>>

    suspend fun getLatestScan(patientId: String): Result<Scan?>

    fun getTodayValidatedCount(bhwId: String): Flow<Int>

    suspend fun getTodayValidatedCountSync(bhwId: String): Result<Int>

    fun getTotalValidatedScans(): Flow<Int>

    suspend fun updateSyncStatus(scanId: String, syncStatus: String): Result<Unit>
}
