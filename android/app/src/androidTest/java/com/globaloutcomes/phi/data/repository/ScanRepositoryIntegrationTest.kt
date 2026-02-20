package com.globaloutcomes.phi.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.globaloutcomes.phi.data.local.GoDatabase
import com.globaloutcomes.phi.data.local.dao.ScanDao
import com.globaloutcomes.phi.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for ScanRepository with real Room database
 * Tests the complete data flow: Repository -> DAO -> Database
 */
@RunWith(AndroidJUnit4::class)
class ScanRepositoryIntegrationTest {

    private lateinit var database: GoDatabase
    private lateinit var scanDao: ScanDao
    private lateinit var repository: ScanRepositoryImpl

    private val now = System.currentTimeMillis()
    private val testPatientId = "patient-123"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            GoDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        scanDao = database.scanDao()
        repository = ScanRepositoryImpl(scanDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    // ========== INSERT SCAN ==========

    @Test
    fun insertScan_savesToDatabaseAndReturnsId() = runTest {
        // Arrange
        val scan = createScan(
            id = "scan-001",
            patientId = testPatientId,
            systolicBp = 120f,
            diastolicBp = 80f
        )

        // Act
        val result = repository.insertScan(scan)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("scan-001", result.getOrNull())

        // Verify saved
        val saved = repository.getScanById("scan-001").getOrNull()
        assertNotNull(saved)
        assertEquals(120f, saved?.systolicBp)
        assertEquals(80f, saved?.diastolicBp)
    }

    @Test
    fun insertScan_withAllBiomarkers_preservesAllData() = runTest {
        // Arrange
        val scan = createScan(
            id = "scan-002",
            systolicBp = 130f,
            diastolicBp = 85f,
            heartRate = 78f,
            heartRateVariability = 42f,
            cardiacOutput = 5.5f,
            strokeVolume = 70f,
            respiratoryRate = 16f,
            spo2 = 97f,
            perfusionIndex = 3.5f,
            bloodGlucose = 95f,
            hba1c = 5.6f,
            cholesterol = 195f,
            triglycerides = 140f,
            hemoglobin = 14.5f,
            hematocrit = 42f,
            arterialStiffness = 8.2f,
            vascularAge = 38f,
            bmi = 24.5f,
            bodyFatPercentage = 18f
        )

        // Act
        repository.insertScan(scan)

        // Assert
        val saved = repository.getScanById("scan-002").getOrNull()!!
        assertEquals(130f, saved.systolicBp)
        assertEquals(85f, saved.diastolicBp)
        assertEquals(78f, saved.heartRate)
        assertEquals(42f, saved.heartRateVariability)
        assertEquals(5.5f, saved.cardiacOutput)
        assertEquals(70f, saved.strokeVolume)
        assertEquals(16f, saved.respiratoryRate)
        assertEquals(97f, saved.spo2)
        assertEquals(95f, saved.bloodGlucose)
        assertEquals(5.6f, saved.hba1c)
        assertEquals(195f, saved.cholesterol)
    }

    @Test
    fun insertScan_withRiskAssessment_preservesRiskData() = runTest {
        // Arrange
        val scan = createScan(
            id = "scan-003",
            riskLevel = RiskLevel.HIGH,
            riskScore = 0.85f,
            highRiskFlags = listOf("Hypertension", "Tachycardia", "Low Oxygen Saturation")
        )

        // Act
        repository.insertScan(scan)

        // Assert
        val saved = repository.getScanById("scan-003").getOrNull()!!
        assertEquals(RiskLevel.HIGH, saved.riskLevel)
        assertEquals(0.85f, saved.riskScore)
        assertEquals(3, saved.highRiskFlags.size)
        assertTrue(saved.highRiskFlags.contains("Hypertension"))
    }

    // ========== GET SCAN BY ID ==========

    @Test
    fun getScanById_returnsScan_whenExists() = runTest {
        // Arrange
        val scan = createScan(id = "scan-004", heartRate = 72f)
        repository.insertScan(scan)

        // Act
        val result = repository.getScanById("scan-004")

        // Assert
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals(72f, result.getOrNull()?.heartRate)
    }

    @Test
    fun getScanById_returnsNull_whenNotExists() = runTest {
        // Act
        val result = repository.getScanById("nonexistent-scan")

        // Assert
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ========== GET SCANS FOR PATIENT ==========

    @Test
    fun getScansForPatient_returnsAllScansForPatient() = runTest {
        // Arrange
        repository.insertScan(createScan(id = "s1", patientId = "patient-A"))
        repository.insertScan(createScan(id = "s2", patientId = "patient-A"))
        repository.insertScan(createScan(id = "s3", patientId = "patient-B"))
        repository.insertScan(createScan(id = "s4", patientId = "patient-A"))

        // Act
        val scans = repository.getScansForPatient("patient-A").getOrNull()!!

        // Assert
        assertEquals(3, scans.size)
        assertTrue(scans.all { it.patientId == "patient-A" })
    }

    @Test
    fun getScansForPatient_returnsEmptyList_whenNoScans() = runTest {
        // Act
        val scans = repository.getScansForPatient("patient-no-scans").getOrNull()!!

        // Assert
        assertTrue(scans.isEmpty())
    }

    // ========== GET LATEST SCAN ==========

    @Test
    fun getLatestScanForPatient_returnsMostRecentScan() = runTest {
        // Arrange
        val oldScan = createScan(id = "old", patientId = testPatientId, scannedAt = now - 10000)
        val latestScan = createScan(id = "latest", patientId = testPatientId, scannedAt = now)
        val middleScan = createScan(id = "middle", patientId = testPatientId, scannedAt = now - 5000)

        repository.insertScan(oldScan)
        repository.insertScan(latestScan)
        repository.insertScan(middleScan)

        // Act
        val latest = repository.getLatestScanForPatient(testPatientId).getOrNull()

        // Assert
        assertNotNull(latest)
        assertEquals("latest", latest?.id)
        assertEquals(now, latest?.scannedAt)
    }

    @Test
    fun getLatestScanForPatient_returnsNull_whenNoScans() = runTest {
        // Act
        val latest = repository.getLatestScanForPatient("patient-no-scans").getOrNull()

        // Assert
        assertNull(latest)
    }

    // ========== GET SCANS SINCE TIMESTAMP ==========

    @Test
    fun getScansSince_returnsOnlyScansAfterTimestamp() = runTest {
        // Arrange
        val cutoffTime = now - (7 * 24 * 60 * 60 * 1000) // 7 days ago
        repository.insertScan(createScan(id = "s1", scannedAt = now - (10 * 24 * 60 * 60 * 1000))) // 10 days ago
        repository.insertScan(createScan(id = "s2", scannedAt = now - (5 * 24 * 60 * 60 * 1000)))  // 5 days ago
        repository.insertScan(createScan(id = "s3", scannedAt = now - (2 * 24 * 60 * 60 * 1000)))  // 2 days ago
        repository.insertScan(createScan(id = "s4", scannedAt = now))                             // today

        // Act
        val recentScans = repository.getScansSince(testPatientId, cutoffTime).getOrNull()!!

        // Assert
        assertEquals(3, recentScans.size) // s2, s3, s4 (after 7 days ago)
        assertTrue(recentScans.all { it.scannedAt >= cutoffTime })
    }

    // ========== GET SCANS FOR DAY ==========

    @Test
    fun getScansForDay_returnsOnlyScansInRange() = runTest {
        // Arrange
        val startOfDay = getStartOfDay(now)
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1

        // Scans at different times
        repository.insertScan(createScan(id = "s1", scannedAt = startOfDay - 1000)) // Before today
        repository.insertScan(createScan(id = "s2", scannedAt = startOfDay + 1000)) // During today
        repository.insertScan(createScan(id = "s3", scannedAt = startOfDay + 12 * 60 * 60 * 1000)) // Noon today
        repository.insertScan(createScan(id = "s4", scannedAt = endOfDay + 1000)) // After today

        // Act
        val todayScans = repository.getScansForDay(startOfDay, endOfDay).getOrNull()!!

        // Assert
        assertEquals(2, todayScans.size) // Only s2 and s3
        assertTrue(todayScans.all { it.scannedAt in startOfDay..endOfDay })
    }

    @Test
    fun getScanCountForDay_countsScansInRange() = runTest {
        // Arrange
        val startOfDay = getStartOfDay(now)
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1

        repository.insertScan(createScan(id = "s1", scannedAt = startOfDay + 1000))
        repository.insertScan(createScan(id = "s2", scannedAt = startOfDay + 2000))
        repository.insertScan(createScan(id = "s3", scannedAt = startOfDay + 3000))
        repository.insertScan(createScan(id = "s4", scannedAt = startOfDay - 1000)) // Before range

        // Act
        val count = repository.getScanCountForDay(startOfDay, endOfDay).getOrNull()

        // Assert
        assertEquals(3, count)
    }

    // ========== HIGH RISK SCANS ==========

    @Test
    fun getHighRiskScansFlow_returnsOnlyHighAndElevatedRisk() = runTest {
        // Arrange
        repository.insertScan(createScan(id = "s1", riskLevel = RiskLevel.NORMAL))
        repository.insertScan(createScan(id = "s2", riskLevel = RiskLevel.ELEVATED))
        repository.insertScan(createScan(id = "s3", riskLevel = RiskLevel.HIGH))
        repository.insertScan(createScan(id = "s4", riskLevel = RiskLevel.NORMAL))
        repository.insertScan(createScan(id = "s5", riskLevel = RiskLevel.HIGH))

        // Act
        val highRiskScans = repository.getHighRiskScansFlow().first()

        // Assert
        assertEquals(3, highRiskScans.size) // ELEVATED and HIGH only
        assertTrue(highRiskScans.all { it.riskLevel != RiskLevel.NORMAL })
    }

    // ========== SYNC STATUS ==========

    @Test
    fun getScansBySyncStatus_filtersByStatus() = runTest {
        // Arrange
        repository.insertScan(createScan(id = "s1", syncStatus = SyncStatus.PENDING))
        repository.insertScan(createScan(id = "s2", syncStatus = SyncStatus.SYNCED))
        repository.insertScan(createScan(id = "s3", syncStatus = SyncStatus.PENDING))
        repository.insertScan(createScan(id = "s4", syncStatus = SyncStatus.FAILED))

        // Act
        val pendingScans = repository.getScansBySyncStatus(SyncStatus.PENDING).getOrNull()!!

        // Assert
        assertEquals(2, pendingScans.size)
        assertTrue(pendingScans.all { it.syncStatus == SyncStatus.PENDING })
    }

    @Test
    fun updateSyncStatus_changesSyncStatus() = runTest {
        // Arrange
        val scan = createScan(id = "scan-005", syncStatus = SyncStatus.PENDING)
        repository.insertScan(scan)

        // Act
        val syncTime = now
        repository.updateSyncStatus("scan-005", SyncStatus.SYNCED, syncTime)

        // Assert
        val updated = repository.getScanById("scan-005").getOrNull()!!
        assertEquals(SyncStatus.SYNCED, updated.syncStatus)
        assertEquals(syncTime, updated.syncedAt)
    }

    // ========== DELETE SCAN ==========

    @Test
    fun deleteScan_removesFromDatabase() = runTest {
        // Arrange
        val scan = createScan(id = "scan-006")
        repository.insertScan(scan)
        assertNotNull(repository.getScanById("scan-006").getOrNull())

        // Act
        repository.deleteScan("scan-006")

        // Assert
        assertNull(repository.getScanById("scan-006").getOrNull())
    }

    @Test
    fun deleteScan_nonexistent_doesNotThrowError() = runTest {
        // Act & Assert
        val result = repository.deleteScan("nonexistent-scan")
        assertTrue(result.isSuccess)
    }

    // ========== FLOW OPERATIONS ==========

    @Test
    fun getScanByIdFlow_emitsUpdates() = runTest {
        // Arrange
        val scan = createScan(id = "scan-007", heartRate = 72f)
        repository.insertScan(scan)

        // Act
        val flow = repository.getScanByIdFlow("scan-007")
        val initial = flow.first()

        // Assert
        assertEquals(72f, initial?.heartRate)
    }

    @Test
    fun getScansByPatientFlow_emitsUpdates() = runTest {
        // Arrange
        repository.insertScan(createScan(id = "s1", patientId = testPatientId))
        repository.insertScan(createScan(id = "s2", patientId = testPatientId))

        // Act
        val flow = repository.getScansByPatientFlow(testPatientId)
        val scans = flow.first()

        // Assert
        assertEquals(2, scans.size)
    }

    // ========== ORDERING ==========

    @Test
    fun getScansForPatient_orderedByTimestamp() = runTest {
        // Arrange
        val scan3 = createScan(id = "s3", scannedAt = now)
        val scan1 = createScan(id = "s1", scannedAt = now - 2000)
        val scan2 = createScan(id = "s2", scannedAt = now - 1000)

        // Insert in random order
        repository.insertScan(scan3)
        repository.insertScan(scan1)
        repository.insertScan(scan2)

        // Act
        val scans = repository.getScansForPatient(testPatientId).getOrNull()!!

        // Assert - Should be ordered by timestamp descending (newest first)
        assertEquals(3, scans.size)
        assertEquals("s3", scans[0].id) // Newest
        assertEquals("s2", scans[1].id) // Middle
        assertEquals("s1", scans[2].id) // Oldest
    }

    // ========== SIGNAL QUALITY ==========

    @Test
    fun insertScan_preservesSignalQuality() = runTest {
        // Arrange
        val scan = createScan(
            id = "scan-008",
            signalQuality = SignalQuality.EXCELLENT,
            qualityScore = 0.95f,
            rejectionReason = null
        )

        // Act
        repository.insertScan(scan)

        // Assert
        val saved = repository.getScanById("scan-008").getOrNull()!!
        assertEquals(SignalQuality.EXCELLENT, saved.signalQuality)
        assertEquals(0.95f, saved.qualityScore)
        assertNull(saved.rejectionReason)
    }

    @Test
    fun insertScan_withRejection_preservesReason() = runTest {
        // Arrange
        val scan = createScan(
            id = "scan-009",
            signalQuality = SignalQuality.POOR,
            qualityScore = 0.45f,
            rejectionReason = "Poor lighting conditions"
        )

        // Act
        repository.insertScan(scan)

        // Assert
        val saved = repository.getScanById("scan-009").getOrNull()!!
        assertEquals(SignalQuality.POOR, saved.signalQuality)
        assertEquals("Poor lighting conditions", saved.rejectionReason)
    }

    // ========== HELPER FUNCTIONS ==========

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun createScan(
        id: String,
        patientId: String = testPatientId,
        scannedAt: Long = now,
        scanDurationSeconds: Int = 50,
        systolicBp: Float? = 120f,
        diastolicBp: Float? = 80f,
        heartRate: Float? = 72f,
        heartRateVariability: Float? = null,
        cardiacOutput: Float? = null,
        strokeVolume: Float? = null,
        respiratoryRate: Float? = null,
        spo2: Float? = null,
        perfusionIndex: Float? = null,
        bloodGlucose: Float? = null,
        hba1c: Float? = null,
        cholesterol: Float? = null,
        triglycerides: Float? = null,
        hemoglobin: Float? = null,
        hematocrit: Float? = null,
        arterialStiffness: Float? = null,
        vascularAge: Float? = null,
        bmi: Float? = null,
        bodyFatPercentage: Float? = null,
        riskLevel: RiskLevel = RiskLevel.NORMAL,
        riskScore: Float = 0.25f,
        highRiskFlags: List<String> = emptyList(),
        signalQuality: SignalQuality = SignalQuality.GOOD,
        qualityScore: Float = 0.85f,
        rejectionReason: String? = null,
        syncStatus: SyncStatus = SyncStatus.PENDING,
        syncedAt: Long? = null
    ) = Scan(
        id = id,
        patientId = patientId,
        scannedAt = scannedAt,
        scanDurationSeconds = scanDurationSeconds,
        systolicBp = systolicBp,
        diastolicBp = diastolicBp,
        heartRate = heartRate,
        heartRateVariability = heartRateVariability,
        cardiacOutput = cardiacOutput,
        strokeVolume = strokeVolume,
        respiratoryRate = respiratoryRate,
        spo2 = spo2,
        perfusionIndex = perfusionIndex,
        bloodGlucose = bloodGlucose,
        hba1c = hba1c,
        cholesterol = cholesterol,
        triglycerides = triglycerides,
        hemoglobin = hemoglobin,
        hematocrit = hematocrit,
        arterialStiffness = arterialStiffness,
        vascularAge = vascularAge,
        bmi = bmi,
        bodyFatPercentage = bodyFatPercentage,
        riskLevel = riskLevel,
        riskScore = riskScore,
        highRiskFlags = highRiskFlags,
        signalQuality = signalQuality,
        qualityScore = qualityScore,
        rejectionReason = rejectionReason,
        createdAt = now,
        updatedAt = now,
        syncStatus = syncStatus,
        syncedAt = syncedAt,
        serverScanId = null
    )
}
