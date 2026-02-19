package com.globaloutcomes.phi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.globaloutcomes.phi.data.local.converters.Converters
import com.globaloutcomes.phi.data.local.dao.*
import com.globaloutcomes.phi.data.local.entities.*

/**
 * Global Outcomes PHI Platform Database
 * Room Database with 9 entities for offline-first operation
 *
 * Entities:
 * - PatientEntity: Demographics and registration
 * - ScanEntity: BiosenseSignal SDK vital signs (34 biomarkers)
 * - SurveyEntity: Health survey responses
 * - ReferralEntity: Three-tier referral system
 * - ClinicalEncounterEntity: Clinical documentation
 * - YakapClaimEntity: PhilHealth claims
 * - BhwIncentiveEntity: BHW earnings tracker
 * - ConfigThresholdEntity: Server-managed risk thresholds
 * - BarangayEntity: Philippine administrative divisions
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

    abstract fun patientDao(): PatientDao
    abstract fun scanDao(): ScanDao
    abstract fun surveyDao(): SurveyDao
    abstract fun referralDao(): ReferralDao
    abstract fun clinicalEncounterDao(): ClinicalEncounterDao
    abstract fun yakapClaimDao(): YakapClaimDao
    abstract fun bhwIncentiveDao(): BhwIncentiveDao
    abstract fun configDao(): ConfigDao
    abstract fun barangayDao(): BarangayDao

    companion object {
        const val DATABASE_NAME = "go_phi_database"
    }
}
