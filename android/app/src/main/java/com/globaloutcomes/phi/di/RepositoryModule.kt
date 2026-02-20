package com.globaloutcomes.phi.di

import com.globaloutcomes.phi.data.repository.PatientRepositoryImpl
import com.globaloutcomes.phi.data.repository.ReferralRepositoryImpl
import com.globaloutcomes.phi.data.repository.ScanRepositoryImpl
import com.globaloutcomes.phi.data.repository.SurveyRepositoryImpl
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import com.globaloutcomes.phi.domain.repository.SurveyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    abstract fun bindSurveyRepository(
        surveyRepositoryImpl: SurveyRepositoryImpl
    ): SurveyRepository

    @Binds
    @Singleton
    abstract fun bindReferralRepository(
        referralRepositoryImpl: ReferralRepositoryImpl
    ): ReferralRepository
}
