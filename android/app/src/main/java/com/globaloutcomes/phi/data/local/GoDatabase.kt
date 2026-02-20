package com.globaloutcomes.phi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.globaloutcomes.phi.data.local.converters.Converters
import com.globaloutcomes.phi.data.local.dao.*
import com.globaloutcomes.phi.data.local.entities.*

/**
 * Room database for PHI Platform
 * Local-first architecture with offline sync capability
 */
@Database(
    entities = [
        PatientEntity::class,
        ScanEntity::class,
        SurveyEntity::class,
        ReferralEntity::class,
        ClinicalEncounterEntity::class,
        YakapClaimEntity::class,
        BhwIncentiveEntity::class,
        ConfigThresholdEntity::class,
        BarangayEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GoDatabase : RoomDatabase() {

    // DAOs
    abstract fun patientDao(): PatientDao
    abstract fun scanDao(): ScanDao
    abstract fun surveyDao(): SurveyDao
    abstract fun referralDao(): ReferralDao
    abstract fun clinicalEncounterDao(): ClinicalEncounterDao
    abstract fun yakapClaimDao(): YakapClaimDao
    abstract fun bhwIncentiveDao(): BhwIncentiveDao
    abstract fun configThresholdDao(): ConfigThresholdDao
    abstract fun barangayDao(): BarangayDao

    companion object {
        const val DATABASE_NAME = "go_phi_database"
    }
}
