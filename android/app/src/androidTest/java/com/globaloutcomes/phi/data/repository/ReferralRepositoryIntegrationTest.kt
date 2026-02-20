package com.globaloutcomes.phi.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.globaloutcomes.phi.data.local.GoDatabase
import com.globaloutcomes.phi.data.local.dao.ReferralDao
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
 * Integration tests for ReferralRepository with real Room database
 * Tests the complete data flow: Repository -> DAO -> Database
 */
@RunWith(AndroidJUnit4::class)
class ReferralRepositoryIntegrationTest {

    private lateinit var database: GoDatabase
    private lateinit var referralDao: ReferralDao
    private lateinit var repository: ReferralRepositoryImpl

    private val now = System.currentTimeMillis()
    private val testPatientId = "patient-123"
    private val testScanId = "scan-456"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            GoDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        referralDao = database.referralDao()
        repository = ReferralRepositoryImpl(referralDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    // ========== INSERT REFERRAL ==========

    @Test
    fun insertReferral_savesToDatabaseAndReturnsId() = runTest {
        // Arrange
        val referral = createReferral(
            id = "ref-001",
            tier = ReferralTier.BHW_TO_BHS,
            status = ReferralStatus.PENDING
        )

        // Act
        val result = repository.insertReferral(referral)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("ref-001", result.getOrNull())

        // Verify saved
        val saved = repository.getReferralById("ref-001").getOrNull()
        assertNotNull(saved)
        assertEquals(ReferralTier.BHW_TO_BHS, saved?.tier)
        assertEquals(ReferralStatus.PENDING, saved?.status)
    }

    @Test
    fun insertReferral_withRiskFlags_preservesFlags() = runTest {
        // Arrange
        val riskFlags = listOf("Hypertension", "Tachycardia", "Low Oxygen Saturation")
        val referral = createReferral(
            id = "ref-002",
            riskLevel = RiskLevel.HIGH,
            riskFlags = riskFlags
        )

        // Act
        repository.insertReferral(referral)

        // Assert
        val saved = repository.getReferralById("ref-002").getOrNull()!!
        assertEquals(RiskLevel.HIGH, saved.riskLevel)
        assertEquals(3, saved.riskFlags.size)
        assertTrue(saved.riskFlags.contains("Hypertension"))
        assertTrue(saved.riskFlags.contains("Tachycardia"))
    }

    @Test
    fun insertReferral_withAllFields_preservesAllData() = runTest {
        // Arrange
        val referral = createReferral(
            id = "ref-003",
            patientId = testPatientId,
            scanId = testScanId,
            tier = ReferralTier.BHW_TO_BHS,
            status = ReferralStatus.PENDING,
            referredAt = now,
            dueBy = now + (48 * 60 * 60 * 1000),
            riskLevel = RiskLevel.ELEVATED,
            riskFlags = listOf("High Blood Glucose"),
            notes = "Patient requires follow-up",
            resolvedAt = null,
            resolvedBy = null,
            resolutionNotes = null
        )

        // Act
        repository.insertReferral(referral)

        // Assert
        val saved = repository.getReferralById("ref-003").getOrNull()!!
        assertEquals(testPatientId, saved.patientId)
        assertEquals(testScanId, saved.scanId)
        assertEquals("Patient requires follow-up", saved.notes)
        assertEquals(RiskLevel.ELEVATED, saved.riskLevel)
        assertNull(saved.resolvedAt)
    }

    // ========== UPDATE REFERRAL ==========

    @Test
    fun updateReferral_modifiesExistingReferral() = runTest {
        // Arrange
        val referral = createReferral(
            id = "ref-004",
            status = ReferralStatus.PENDING,
            notes = "Initial notes"
        )
        repository.insertReferral(referral)

        // Act
        val updated = referral.copy(
            status = ReferralStatus.CONFIRMED,
            notes = "Updated notes"
        )
        val result = repository.updateReferral(updated)

        // Assert
        assertTrue(result.isSuccess)
        val saved = repository.getReferralById("ref-004").getOrNull()!!
        assertEquals(ReferralStatus.CONFIRMED, saved.status)
        assertEquals("Updated notes", saved.notes)
    }

    @Test
    fun updateReferral_escalatesToNextTier() = runTest {
        // Arrange
        val referral = createReferral(
            id = "ref-005",
            tier = ReferralTier.BHW_TO_BHS,
            status = ReferralStatus.PENDING
        )
        repository.insertReferral(referral)

        // Act - Escalate to RHU
        val escalated = referral.copy(
            tier = ReferralTier.BHS_TO_RHU,
            status = ReferralStatus.CONFIRMED,
            dueBy = now + (48 * 60 * 60 * 1000) // New 48-hour deadline
        )
        repository.updateReferral(escalated)

        // Assert
        val saved = repository.getReferralById("ref-005").getOrNull()!!
        assertEquals(ReferralTier.BHS_TO_RHU, saved.tier)
        assertEquals(ReferralStatus.CONFIRMED, saved.status)
    }

    @Test
    fun updateReferral_marksAsResolved() = runTest {
        // Arrange
        val referral = createReferral(
            id = "ref-006",
            status = ReferralStatus.CONFIRMED
        )
        repository.insertReferral(referral)

        // Act
        val resolved = referral.copy(
            status = ReferralStatus.RESOLVED,
            resolvedAt = now,
            resolvedBy = "Dr. Juan Dela Cruz",
            resolutionNotes = "Patient treated successfully"
        )
        repository.updateReferral(resolved)

        // Assert
        val saved = repository.getReferralById("ref-006").getOrNull()!!
        assertEquals(ReferralStatus.RESOLVED, saved.status)
        assertEquals(now, saved.resolvedAt)
        assertEquals("Dr. Juan Dela Cruz", saved.resolvedBy)
        assertEquals("Patient treated successfully", saved.resolutionNotes)
    }

    // ========== GET REFERRAL BY ID ==========

    @Test
    fun getReferralById_returnsReferral_whenExists() = runTest {
        // Arrange
        val referral = createReferral(id = "ref-007", riskLevel = RiskLevel.HIGH)
        repository.insertReferral(referral)

        // Act
        val result = repository.getReferralById("ref-007")

        // Assert
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals(RiskLevel.HIGH, result.getOrNull()?.riskLevel)
    }

    @Test
    fun getReferralById_returnsNull_whenNotExists() = runTest {
        // Act
        val result = repository.getReferralById("nonexistent-referral")

        // Assert
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ========== ACTIVE REFERRALS FLOW ==========

    @Test
    fun getActiveReferralsFlow_returnsOnlyPendingAndConfirmed() = runTest {
        // Arrange
        repository.insertReferral(createReferral(id = "r1", status = ReferralStatus.PENDING))
        repository.insertReferral(createReferral(id = "r2", status = ReferralStatus.CONFIRMED))
        repository.insertReferral(createReferral(id = "r3", status = ReferralStatus.RESOLVED))
        repository.insertReferral(createReferral(id = "r4", status = ReferralStatus.PENDING))
        repository.insertReferral(createReferral(id = "r5", status = ReferralStatus.OVERDUE))

        // Act
        val activeReferrals = repository.getActiveReferralsFlow().first()

        // Assert
        // Active = PENDING + CONFIRMED (not RESOLVED or OVERDUE)
        assertEquals(2, activeReferrals.size)
        assertTrue(activeReferrals.all {
            it.status == ReferralStatus.PENDING || it.status == ReferralStatus.CONFIRMED
        })
    }

    // ========== SYNC STATUS ==========

    @Test
    fun getReferralsBySyncStatus_filtersByStatus() = runTest {
        // Arrange
        repository.insertReferral(createReferral(id = "r1", syncStatus = SyncStatus.PENDING))
        repository.insertReferral(createReferral(id = "r2", syncStatus = SyncStatus.SYNCED))
        repository.insertReferral(createReferral(id = "r3", syncStatus = SyncStatus.PENDING))
        repository.insertReferral(createReferral(id = "r4", syncStatus = SyncStatus.FAILED))

        // Act
        val pendingReferrals = repository.getReferralsBySyncStatus(SyncStatus.PENDING).getOrNull()!!

        // Assert
        assertEquals(2, pendingReferrals.size)
        assertTrue(pendingReferrals.all { it.syncStatus == SyncStatus.PENDING })
    }

    @Test
    fun updateSyncStatus_changesSyncStatus() = runTest {
        // Arrange
        val referral = createReferral(id = "ref-008", syncStatus = SyncStatus.PENDING)
        repository.insertReferral(referral)

        // Act
        val syncTime = now
        repository.updateSyncStatus("ref-008", SyncStatus.SYNCED, syncTime)

        // Assert
        val updated = repository.getReferralById("ref-008").getOrNull()!!
        assertEquals(SyncStatus.SYNCED, updated.syncStatus)
        assertEquals(syncTime, updated.syncedAt)
    }

    // ========== STATUS TRANSITIONS ==========

    @Test
    fun referralStatusProgression_worksCorrectly() = runTest {
        // Arrange
        val referral = createReferral(id = "ref-009", status = ReferralStatus.PENDING)
        repository.insertReferral(referral)

        // Act & Assert - PENDING -> CONFIRMED
        repository.updateReferral(referral.copy(status = ReferralStatus.CONFIRMED))
        assertEquals(ReferralStatus.CONFIRMED, repository.getReferralById("ref-009").getOrNull()?.status)

        // Act & Assert - CONFIRMED -> RESOLVED
        repository.updateReferral(referral.copy(
            status = ReferralStatus.RESOLVED,
            resolvedAt = now,
            resolvedBy = "Dr. Test"
        ))
        val resolved = repository.getReferralById("ref-009").getOrNull()!!
        assertEquals(ReferralStatus.RESOLVED, resolved.status)
        assertNotNull(resolved.resolvedAt)
    }

    // ========== DUE DATE HANDLING ==========

    @Test
    fun referralDueDate_preservesCorrectly() = runTest {
        // Arrange
        val dueDate = now + (48 * 60 * 60 * 1000) // 48 hours from now
        val referral = createReferral(
            id = "ref-010",
            referredAt = now,
            dueBy = dueDate
        )

        // Act
        repository.insertReferral(referral)

        // Assert
        val saved = repository.getReferralById("ref-010").getOrNull()!!
        assertEquals(now, saved.referredAt)
        assertEquals(dueDate, saved.dueBy)
    }

    @Test
    fun overdueReferral_canBeMarkedAsOverdue() = runTest {
        // Arrange
        val pastDueDate = now - (24 * 60 * 60 * 1000) // 24 hours ago
        val referral = createReferral(
            id = "ref-011",
            status = ReferralStatus.PENDING,
            dueBy = pastDueDate
        )
        repository.insertReferral(referral)

        // Act - Update to OVERDUE
        repository.updateReferral(referral.copy(status = ReferralStatus.OVERDUE))

        // Assert
        val saved = repository.getReferralById("ref-011").getOrNull()!!
        assertEquals(ReferralStatus.OVERDUE, saved.status)
        assertTrue(saved.dueBy < now) // Confirm it's actually overdue
    }

    // ========== TIER ESCALATION ==========

    @Test
    fun tierEscalation_fromBhwToHospital() = runTest {
        // Arrange
        val referral = createReferral(id = "ref-012", tier = ReferralTier.BHW_TO_BHS)
        repository.insertReferral(referral)

        // Act - BHW to BHS (already set)
        assertEquals(ReferralTier.BHW_TO_BHS, repository.getReferralById("ref-012").getOrNull()?.tier)

        // Act - Escalate to RHU
        repository.updateReferral(referral.copy(tier = ReferralTier.BHS_TO_RHU))
        assertEquals(ReferralTier.BHS_TO_RHU, repository.getReferralById("ref-012").getOrNull()?.tier)

        // Act - Escalate to Hospital
        repository.updateReferral(referral.copy(tier = ReferralTier.RHU_TO_HOSPITAL))
        assertEquals(ReferralTier.RHU_TO_HOSPITAL, repository.getReferralById("ref-012").getOrNull()?.tier)
    }

    // ========== MULTIPLE REFERRALS ==========

    @Test
    fun multipleReferrals_forSamePatient_allSaved() = runTest {
        // Arrange
        val referral1 = createReferral(
            id = "r1",
            patientId = testPatientId,
            scanId = "scan-1",
            referredAt = now - 5000
        )
        val referral2 = createReferral(
            id = "r2",
            patientId = testPatientId,
            scanId = "scan-2",
            referredAt = now
        )

        // Act
        repository.insertReferral(referral1)
        repository.insertReferral(referral2)

        // Assert - Both should exist independently
        assertNotNull(repository.getReferralById("r1").getOrNull())
        assertNotNull(repository.getReferralById("r2").getOrNull())
    }

    // ========== RISK FLAGS SERIALIZATION ==========

    @Test
    fun riskFlags_emptyList_handledCorrectly() = runTest {
        // Arrange
        val referral = createReferral(id = "ref-013", riskFlags = emptyList())

        // Act
        repository.insertReferral(referral)

        // Assert
        val saved = repository.getReferralById("ref-013").getOrNull()!!
        assertTrue(saved.riskFlags.isEmpty())
    }

    @Test
    fun riskFlags_multipleFlags_preservedInOrder() = runTest {
        // Arrange
        val flags = listOf(
            "Hypertension",
            "High Blood Glucose",
            "Low Oxygen Saturation",
            "Tachycardia",
            "High Cholesterol"
        )
        val referral = createReferral(id = "ref-014", riskFlags = flags)

        // Act
        repository.insertReferral(referral)

        // Assert
        val saved = repository.getReferralById("ref-014").getOrNull()!!
        assertEquals(5, saved.riskFlags.size)
        assertEquals(flags, saved.riskFlags) // Order preserved
    }

    // ========== HELPER FUNCTIONS ==========

    private fun createReferral(
        id: String,
        patientId: String = testPatientId,
        scanId: String = testScanId,
        tier: ReferralTier = ReferralTier.BHW_TO_BHS,
        status: ReferralStatus = ReferralStatus.PENDING,
        referredAt: Long = now,
        dueBy: Long = now + (48 * 60 * 60 * 1000),
        riskLevel: RiskLevel = RiskLevel.HIGH,
        riskFlags: List<String> = listOf("Hypertension"),
        notes: String? = null,
        resolvedAt: Long? = null,
        resolvedBy: String? = null,
        resolutionNotes: String? = null,
        syncStatus: SyncStatus = SyncStatus.PENDING,
        syncedAt: Long? = null
    ) = Referral(
        id = id,
        patientId = patientId,
        scanId = scanId,
        tier = tier,
        status = status,
        referredAt = referredAt,
        dueBy = dueBy,
        riskLevel = riskLevel,
        riskFlags = riskFlags,
        notes = notes,
        resolvedAt = resolvedAt,
        resolvedBy = resolvedBy,
        resolutionNotes = resolutionNotes,
        createdAt = now,
        updatedAt = now,
        syncStatus = syncStatus,
        syncedAt = syncedAt,
        serverReferralId = null
    )
}
