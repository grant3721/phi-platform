package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateReferralUseCaseTest {

    private lateinit var useCase: UpdateReferralUseCase
    private lateinit var mockReferralRepository: ReferralRepository

    private val testReferralId = "referral-123"
    private val now = System.currentTimeMillis()

    @Before
    fun setup() {
        mockReferralRepository = mockk()
        useCase = UpdateReferralUseCase(mockReferralRepository)
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    // ========== UPDATE STATUS ==========

    @Test
    fun `updateStatus changes status to CONFIRMED`() = runTest {
        // Arrange
        val existingReferral = createReferral(status = ReferralStatus.PENDING)
        val updatedSlot = slot<Referral>()

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(capture(updatedSlot)) } returns Result.success(Unit)

        // Act
        val result = useCase.updateStatus(testReferralId, ReferralStatus.CONFIRMED, notes = "Patient contacted")

        // Assert
        assertTrue(result.isSuccess)
        val updated = updatedSlot.captured
        assertEquals(ReferralStatus.CONFIRMED, updated.status)
        assertEquals("Patient contacted", updated.notes)
    }

    @Test
    fun `updateStatus to RESOLVED sets resolvedAt timestamp`() = runTest {
        // Arrange
        val existingReferral = createReferral(status = ReferralStatus.PENDING)
        val updatedSlot = slot<Referral>()
        val beforeTime = System.currentTimeMillis()

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(capture(updatedSlot)) } returns Result.success(Unit)

        // Act
        useCase.updateStatus(testReferralId, ReferralStatus.RESOLVED, notes = "Resolved at BHS")

        val afterTime = System.currentTimeMillis()

        // Assert
        val updated = updatedSlot.captured
        assertEquals(ReferralStatus.RESOLVED, updated.status)
        assertNotNull(updated.resolvedAt)
        assertTrue(updated.resolvedAt!! in beforeTime..afterTime)
    }

    @Test
    fun `updateStatus preserves existing notes when not provided`() = runTest {
        // Arrange
        val existingReferral = createReferral(
            status = ReferralStatus.PENDING,
            notes = "Original notes"
        )
        val updatedSlot = slot<Referral>()

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(capture(updatedSlot)) } returns Result.success(Unit)

        // Act
        useCase.updateStatus(testReferralId, ReferralStatus.CONFIRMED, notes = null)

        // Assert
        val updated = updatedSlot.captured
        assertEquals("Original notes", updated.notes)
    }

    @Test
    fun `updateStatus updates updatedAt timestamp`() = runTest {
        // Arrange
        val existingReferral = createReferral()
        val updatedSlot = slot<Referral>()
        val beforeTime = System.currentTimeMillis()

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(capture(updatedSlot)) } returns Result.success(Unit)

        // Act
        useCase.updateStatus(testReferralId, ReferralStatus.CONFIRMED)

        val afterTime = System.currentTimeMillis()

        // Assert
        val updated = updatedSlot.captured
        assertTrue(updated.updatedAt in beforeTime..afterTime)
        assertTrue(updated.updatedAt > existingReferral.updatedAt)
    }

    // ========== ESCALATE TO RHU ==========

    @Test
    fun `escalateToRhu changes tier from BHW_TO_BHS to BHS_TO_RHU`() = runTest {
        // Arrange
        val existingReferral = createReferral(tier = ReferralTier.BHW_TO_BHS)
        val updatedSlot = slot<Referral>()

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(capture(updatedSlot)) } returns Result.success(Unit)

        // Act
        val result = useCase.escalateToRhu(testReferralId, notes = "Escalating to RHU")

        // Assert
        assertTrue(result.isSuccess)
        val updated = updatedSlot.captured
        assertEquals(ReferralTier.BHS_TO_RHU, updated.tier)
        assertEquals(ReferralStatus.CONFIRMED, updated.status)
        assertEquals("Escalating to RHU", updated.notes)
    }

    @Test
    fun `escalateToRhu sets new dueBy 48 hours from now`() = runTest {
        // Arrange
        val existingReferral = createReferral(
            tier = ReferralTier.BHW_TO_BHS,
            dueBy = now + (24 * 60 * 60 * 1000) // Original due 24 hours from now
        )
        val updatedSlot = slot<Referral>()
        val beforeTime = System.currentTimeMillis()

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(capture(updatedSlot)) } returns Result.success(Unit)

        // Act
        useCase.escalateToRhu(testReferralId, notes = "Escalating")

        val afterTime = System.currentTimeMillis()

        // Assert
        val updated = updatedSlot.captured
        val expectedMinDueBy = beforeTime + (48 * 60 * 60 * 1000)
        val expectedMaxDueBy = afterTime + (48 * 60 * 60 * 1000)
        assertTrue(updated.dueBy in expectedMinDueBy..expectedMaxDueBy)
    }

    @Test
    fun `escalateToRhu rejects non-BHW_TO_BHS referrals`() = runTest {
        // Arrange
        val existingReferral = createReferral(tier = ReferralTier.BHS_TO_RHU)

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)

        // Act
        val result = useCase.escalateToRhu(testReferralId, notes = "Trying to escalate")

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Can only escalate BHW_TO_BHS referrals") == true)

        // Verify update was not called
        coVerify(exactly = 0) { mockReferralRepository.updateReferral(any()) }
    }

    @Test
    fun `escalateToRhu rejects RHU_TO_HOSPITAL referrals`() = runTest {
        // Arrange
        val existingReferral = createReferral(tier = ReferralTier.RHU_TO_HOSPITAL)

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)

        // Act
        val result = useCase.escalateToRhu(testReferralId, notes = "Trying to escalate")

        // Assert
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { mockReferralRepository.updateReferral(any()) }
    }

    // ========== RESOLVE REFERRAL ==========

    @Test
    fun `resolveReferral sets status to RESOLVED and records details`() = runTest {
        // Arrange
        val existingReferral = createReferral(status = ReferralStatus.CONFIRMED)
        val updatedSlot = slot<Referral>()

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(capture(updatedSlot)) } returns Result.success(Unit)

        // Act
        val result = useCase.resolveReferral(
            testReferralId,
            resolutionNotes = "Patient treated successfully",
            resolvedBy = "Dr. Juan Dela Cruz"
        )

        // Assert
        assertTrue(result.isSuccess)
        val updated = updatedSlot.captured
        assertEquals(ReferralStatus.RESOLVED, updated.status)
        assertEquals("Patient treated successfully", updated.resolutionNotes)
        assertEquals("Dr. Juan Dela Cruz", updated.resolvedBy)
    }

    @Test
    fun `resolveReferral sets resolvedAt timestamp to current time`() = runTest {
        // Arrange
        val existingReferral = createReferral(
            status = ReferralStatus.CONFIRMED,
            resolvedAt = null
        )
        val updatedSlot = slot<Referral>()
        val beforeTime = System.currentTimeMillis()

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(capture(updatedSlot)) } returns Result.success(Unit)

        // Act
        useCase.resolveReferral(
            testReferralId,
            resolutionNotes = "Resolved",
            resolvedBy = "Dr. Test"
        )

        val afterTime = System.currentTimeMillis()

        // Assert
        val updated = updatedSlot.captured
        assertNotNull(updated.resolvedAt)
        assertTrue(updated.resolvedAt!! in beforeTime..afterTime)
    }

    @Test
    fun `resolveReferral updates updatedAt timestamp`() = runTest {
        // Arrange
        val existingReferral = createReferral(updatedAt = now - 1000)
        val updatedSlot = slot<Referral>()
        val beforeTime = System.currentTimeMillis()

        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(capture(updatedSlot)) } returns Result.success(Unit)

        // Act
        useCase.resolveReferral(testReferralId, "Resolved", "Dr. Test")

        val afterTime = System.currentTimeMillis()

        // Assert
        val updated = updatedSlot.captured
        assertTrue(updated.updatedAt in beforeTime..afterTime)
        assertTrue(updated.updatedAt > existingReferral.updatedAt)
    }

    // ========== REFERRAL NOT FOUND ==========

    @Test
    fun `updateStatus returns failure when referral not found`() = runTest {
        // Arrange
        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns
            Result.success(null)

        // Act
        val result = useCase.updateStatus(testReferralId, ReferralStatus.CONFIRMED)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Referral not found") == true)
    }

    @Test
    fun `escalateToRhu returns failure when referral not found`() = runTest {
        // Arrange
        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns
            Result.success(null)

        // Act
        val result = useCase.escalateToRhu(testReferralId, "Notes")

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Referral not found") == true)
    }

    @Test
    fun `resolveReferral returns failure when referral not found`() = runTest {
        // Arrange
        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns
            Result.success(null)

        // Act
        val result = useCase.resolveReferral(testReferralId, "Notes", "Dr. Test")

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Referral not found") == true)
    }

    // ========== ERROR HANDLING ==========

    @Test
    fun `updateStatus returns failure on repository error`() = runTest {
        // Arrange
        val existingReferral = createReferral()
        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(any()) } returns
            Result.failure(Exception("Database error"))

        // Act
        val result = useCase.updateStatus(testReferralId, ReferralStatus.CONFIRMED)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `escalateToRhu returns failure on repository error`() = runTest {
        // Arrange
        val existingReferral = createReferral(tier = ReferralTier.BHW_TO_BHS)
        coEvery { mockReferralRepository.getReferralById(testReferralId) } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(any()) } returns
            Result.failure(Exception("Update failed"))

        // Act
        val result = useCase.escalateToRhu(testReferralId, "Notes")

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Update failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `resolveReferral returns failure on exception`() = runTest {
        // Arrange
        coEvery { mockReferralRepository.getReferralById(testReferralId) } throws
            Exception("Unexpected error")

        // Act
        val result = useCase.resolveReferral(testReferralId, "Notes", "Dr. Test")

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Unexpected error", result.exceptionOrNull()?.message)
    }

    // ========== HELPER FUNCTIONS ==========

    private fun createReferral(
        id: String = testReferralId,
        patientId: String = "patient-123",
        scanId: String = "scan-456",
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
        createdAt: Long = now,
        updatedAt: Long = now
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
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.PENDING,
        syncedAt = null,
        serverReferralId = null
    )
}
