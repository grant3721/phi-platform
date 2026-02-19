package com.globaloutcomes.phi.domain.repository

import com.globaloutcomes.phi.domain.model.Barangay
import kotlinx.coroutines.flow.Flow

/**
 * Barangay Repository Interface
 */
interface BarangayRepository {

    suspend fun getByCode(code: String): Result<Barangay?>

    fun getAllBarangays(): Flow<List<Barangay>>

    fun searchBarangays(query: String): Flow<List<Barangay>>

    suspend fun getCount(): Result<Int>
}
