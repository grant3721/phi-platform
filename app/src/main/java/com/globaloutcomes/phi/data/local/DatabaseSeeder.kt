package com.globaloutcomes.phi.data.local

import android.content.Context
import com.globaloutcomes.phi.data.local.dao.BarangayDao
import com.globaloutcomes.phi.data.local.dao.ConfigDao
import com.globaloutcomes.phi.data.local.entities.BarangayEntity
import com.globaloutcomes.phi.data.local.entities.ConfigThresholdEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Database Seeder - Initial Data Population
 * Seeds ConfigThresholds and Barangays on first app launch
 */
class DatabaseSeeder(
    private val context: Context,
    private val configDao: ConfigDao,
    private val barangayDao: BarangayDao
) {

    /**
     * Seed default risk thresholds from specification
     */
    suspend fun seedConfigThresholds() = withContext(Dispatchers.IO) {
        val count = configDao.getCount()
        if (count > 0) return@withContext // Already seeded

        val thresholds = listOf(
            ConfigThresholdEntity(
                key = "bp_systolic_crisis",
                value = 160.0,
                description = "Blood pressure systolic crisis threshold (mmHg)"
            ),
            ConfigThresholdEntity(
                key = "bp_diastolic_crisis",
                value = 100.0,
                description = "Blood pressure diastolic crisis threshold (mmHg)"
            ),
            ConfigThresholdEntity(
                key = "spo2_low",
                value = 94.0,
                description = "Low oxygen saturation threshold (%)"
            ),
            ConfigThresholdEntity(
                key = "hr_high",
                value = 120.0,
                description = "High heart rate threshold (bpm)"
            ),
            ConfigThresholdEntity(
                key = "hr_low",
                value = 50.0,
                description = "Low heart rate threshold (bpm)"
            ),
            ConfigThresholdEntity(
                key = "rr_high",
                value = 25.0,
                description = "High respiration rate threshold (breaths/min)"
            ),
            ConfigThresholdEntity(
                key = "rr_low",
                value = 10.0,
                description = "Low respiration rate threshold (breaths/min)"
            ),
            ConfigThresholdEntity(
                key = "stress_high",
                value = 4.0,
                description = "High stress index threshold"
            ),
            ConfigThresholdEntity(
                key = "high_bp_risk_threshold",
                value = 0.7,
                description = "High blood pressure risk score threshold"
            ),
            ConfigThresholdEntity(
                key = "high_glucose_risk_threshold",
                value = 0.7,
                description = "High fasting glucose risk score threshold"
            ),
            ConfigThresholdEntity(
                key = "high_a1c_risk_threshold",
                value = 0.7,
                description = "High hemoglobin A1c risk score threshold"
            ),
            ConfigThresholdEntity(
                key = "high_cholesterol_risk_threshold",
                value = 0.7,
                description = "High total cholesterol risk score threshold"
            ),
            ConfigThresholdEntity(
                key = "low_hemoglobin_risk_threshold",
                value = 0.7,
                description = "Low hemoglobin risk score threshold"
            )
        )

        configDao.insertAll(thresholds)
    }

    /**
     * Seed 50 sample barangays for development
     * Full 1,209 barangays loaded in production from CSV
     */
    suspend fun seedBarangays() = withContext(Dispatchers.IO) {
        val count = barangayDao.getCount()
        if (count > 0) return@withContext // Already seeded

        val barangays = listOf(
            // Lucena City (10 barangays)
            BarangayEntity("045801001", "Barangay 1 (Poblacion)", "045801", "Lucena City", 4523),
            BarangayEntity("045801002", "Barangay 2 (Poblacion)", "045801", "Lucena City", 3892),
            BarangayEntity("045801003", "Barangay 3 (Poblacion)", "045801", "Lucena City", 4156),
            BarangayEntity("045801004", "Barangay 4 (Poblacion)", "045801", "Lucena City", 3721),
            BarangayEntity("045801005", "Barangay 5 (Poblacion)", "045801", "Lucena City", 4289),
            BarangayEntity("045801010", "Cotta", "045801", "Lucena City", 5634),
            BarangayEntity("045801015", "Gulang-Gulang", "045801", "Lucena City", 6123),
            BarangayEntity("045801020", "Ibabang Dupay", "045801", "Lucena City", 4897),
            BarangayEntity("045801025", "Ilayang Dupay", "045801", "Lucena City", 5234),
            BarangayEntity("045801030", "Market View", "045801", "Lucena City", 7456),

            // Tayabas City (10 barangays)
            BarangayEntity("045802001", "Angeles Zone I (Poblacion)", "045802", "Tayabas City", 2134),
            BarangayEntity("045802005", "Ayaas", "045802", "Tayabas City", 3456),
            BarangayEntity("045802010", "Baguio", "045802", "Tayabas City", 2789),
            BarangayEntity("045802015", "Banilad", "045802", "Tayabas City", 1923),
            BarangayEntity("045802020", "Bukal", "045802", "Tayabas City", 2567),
            BarangayEntity("045802025", "Calumpang", "045802", "Tayabas City", 3234),
            BarangayEntity("045802030", "Domoit", "045802", "Tayabas City", 2891),
            BarangayEntity("045802035", "Gibanga", "045802", "Tayabas City", 1678),
            BarangayEntity("045802040", "Lita", "045802", "Tayabas City", 2345),
            BarangayEntity("045802045", "Mateuna", "045802", "Tayabas City", 3456),

            // Sariaya (10 barangays)
            BarangayEntity("045803001", "Barangay 1 (Poblacion)", "045803", "Sariaya", 1823),
            BarangayEntity("045803002", "Barangay 2 (Poblacion)", "045803", "Sariaya", 1567),
            BarangayEntity("045803005", "Antipolo", "045803", "Sariaya", 2134),
            BarangayEntity("045803010", "Bagong Pook", "045803", "Sariaya", 1789),
            BarangayEntity("045803015", "Bignay 1", "045803", "Sariaya", 2345),
            BarangayEntity("045803020", "Castañas", "045803", "Sariaya", 1456),
            BarangayEntity("045803025", "Concepcion Banahaw", "045803", "Sariaya", 3234),
            BarangayEntity("045803030", "Gibanga", "045803", "Sariaya", 2678),
            BarangayEntity("045803035", "Guisguis-San Roque", "045803", "Sariaya", 1923),
            BarangayEntity("045803040", "Guisguis-Talon", "045803", "Sariaya", 2456),

            // Candelaria (10 barangays)
            BarangayEntity("045804001", "Poblacion 1", "045804", "Candelaria", 2134),
            BarangayEntity("045804002", "Poblacion 2", "045804", "Candelaria", 1897),
            BarangayEntity("045804005", "Buenavista East", "045804", "Candelaria", 3456),
            BarangayEntity("045804010", "Buenavista West", "045804", "Candelaria", 2789),
            BarangayEntity("045804015", "Bukal Norte", "045804", "Candelaria", 2345),
            BarangayEntity("045804020", "Kinatihan I", "045804", "Candelaria", 4123),
            BarangayEntity("045804025", "Kinatihan II", "045804", "Candelaria", 3678),
            BarangayEntity("045804030", "Malabanban Norte", "045804", "Candelaria", 2456),
            BarangayEntity("045804035", "Malabanban Sur", "045804", "Candelaria", 2234),
            BarangayEntity("045804040", "Mangilag Norte", "045804", "Candelaria", 1789),

            // Tiaong (10 barangays)
            BarangayEntity("045805001", "Poblacion Barangay 1", "045805", "Tiaong", 1456),
            BarangayEntity("045805002", "Poblacion Barangay 2", "045805", "Tiaong", 1234),
            BarangayEntity("045805005", "Anastacia", "045805", "Tiaong", 2567),
            BarangayEntity("045805010", "Ayusan I", "045805", "Tiaong", 3456),
            BarangayEntity("045805015", "Behia", "045805", "Tiaong", 2134),
            BarangayEntity("045805020", "Bulakin", "045805", "Tiaong", 2789),
            BarangayEntity("045805025", "Buo", "045805", "Tiaong", 3234),
            BarangayEntity("045805030", "Cabatang", "045805", "Tiaong", 1923),
            BarangayEntity("045805035", "Cabay", "045805", "Tiaong", 2456),
            BarangayEntity("045805040", "Lagalag", "045805", "Tiaong", 2678)
        )

        barangayDao.insertAll(barangays)
    }

    private suspend fun ConfigDao.getCount(): Int {
        // Workaround: count all thresholds
        return try {
            getAllThresholds().let { 0 } // TODO: Implement count query
        } catch (e: Exception) {
            0
        }
    }
}
