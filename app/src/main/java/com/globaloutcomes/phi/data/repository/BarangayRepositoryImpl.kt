package com.globaloutcomes.phi.data.repository

import com.globaloutcomes.phi.data.local.dao.BarangayDao
import com.globaloutcomes.phi.data.local.entities.BarangayEntity
import com.globaloutcomes.phi.domain.model.Barangay
import com.globaloutcomes.phi.domain.repository.BarangayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Barangay Repository Implementation
 */
class BarangayRepositoryImpl @Inject constructor(
    private val barangayDao: BarangayDao
) : BarangayRepository {

    override suspend fun getByCode(code: String): Result<Barangay?> = runCatching {
        barangayDao.getByCode(code)?.toDomain()
    }

    override fun getAllBarangays(): Flow<List<Barangay>> {
        return barangayDao.getAllBarangays().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchBarangays(query: String): Flow<List<Barangay>> {
        return barangayDao.searchBarangays(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCount(): Result<Int> = runCatching {
        barangayDao.getCount()
    }

    // Mappers
    private fun BarangayEntity.toDomain(): Barangay {
        return Barangay(
            code = code,
            name = name,
            municipalityCode = municipalityCode,
            municipalityName = municipalityName,
            population = population
        )
    }
}
