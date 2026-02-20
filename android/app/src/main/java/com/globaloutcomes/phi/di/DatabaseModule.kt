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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): GoDatabase {
        return Room.databaseBuilder(
            context,
            GoDatabase::class.java,
            GoDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development only
            .build()
    }

    @Provides
    fun providePatientDao(database: GoDatabase): PatientDao {
        return database.patientDao()
    }

    @Provides
    fun provideScanDao(database: GoDatabase): ScanDao {
        return database.scanDao()
    }

    @Provides
    fun provideSurveyDao(database: GoDatabase): SurveyDao {
        return database.surveyDao()
    }

    @Provides
    fun provideReferralDao(database: GoDatabase): ReferralDao {
        return database.referralDao()
    }

    @Provides
    fun provideClinicalEncounterDao(database: GoDatabase): ClinicalEncounterDao {
        return database.clinicalEncounterDao()
    }

    @Provides
    fun provideYakapClaimDao(database: GoDatabase): YakapClaimDao {
        return database.yakapClaimDao()
    }

    @Provides
    fun provideBhwIncentiveDao(database: GoDatabase): BhwIncentiveDao {
        return database.bhwIncentiveDao()
    }

    @Provides
    fun provideConfigThresholdDao(database: GoDatabase): ConfigThresholdDao {
        return database.configThresholdDao()
    }

    @Provides
    fun provideBarangayDao(database: GoDatabase): BarangayDao {
        return database.barangayDao()
    }
}
