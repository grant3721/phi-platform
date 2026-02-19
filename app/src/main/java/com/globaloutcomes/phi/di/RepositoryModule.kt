package com.globaloutcomes.phi.di

import com.globaloutcomes.phi.data.repository.BarangayRepositoryImpl
import com.globaloutcomes.phi.data.repository.PatientRepositoryImpl
import com.globaloutcomes.phi.data.repository.ScanRepositoryImpl
import com.globaloutcomes.phi.domain.repository.BarangayRepository
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Repository Module - Binds repository implementations to interfaces
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPatientRepository(
        patientRepositoryImpl: PatientRepositoryImpl
    ): PatientRepository

    @Binds
    @Singleton
    abstract fun bindScanRepository(
        scanRepositoryImpl: ScanRepositoryImpl
    ): ScanRepository

    @Binds
    @Singleton
    abstract fun bindBarangayRepository(
        barangayRepositoryImpl: BarangayRepositoryImpl
    ): BarangayRepository
}
