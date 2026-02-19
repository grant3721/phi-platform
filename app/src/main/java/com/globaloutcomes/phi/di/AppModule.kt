package com.globaloutcomes.phi.di

import android.content.Context
import androidx.room.Room
import com.globaloutcomes.phi.data.local.GoDatabase
import com.globaloutcomes.phi.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt App Module - Provides core dependencies
 * Database, DAOs, and other app-level singletons
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGoDatabase(
        @ApplicationContext context: Context
    ): GoDatabase {
        return Room.databaseBuilder(
            context,
            GoDatabase::class.java,
            GoDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development only - remove in production
            .build()
    }

    @Provides
    @Singleton
    fun providePatientDao(database: GoDatabase): PatientDao {
        return database.patientDao()
    }

    @Provides
    @Singleton
    fun provideScanDao(database: GoDatabase): ScanDao {
        return database.scanDao()
    }

    @Provides
    @Singleton
    fun provideSurveyDao(database: GoDatabase): SurveyDao {
        return database.surveyDao()
    }

    @Provides
    @Singleton
    fun provideReferralDao(database: GoDatabase): ReferralDao {
        return database.referralDao()
    }

    @Provides
    @Singleton
    fun provideClinicalEncounterDao(database: GoDatabase): ClinicalEncounterDao {
        return database.clinicalEncounterDao()
    }

    @Provides
    @Singleton
    fun provideYakapClaimDao(database: GoDatabase): YakapClaimDao {
        return database.yakapClaimDao()
    }

    @Provides
    @Singleton
    fun provideBhwIncentiveDao(database: GoDatabase): BhwIncentiveDao {
        return database.bhwIncentiveDao()
    }

    @Provides
    @Singleton
    fun provideConfigDao(database: GoDatabase): ConfigDao {
        return database.configDao()
    }

    @Provides
    @Singleton
    fun provideBarangayDao(database: GoDatabase): BarangayDao {
        return database.barangayDao()
    }
}
