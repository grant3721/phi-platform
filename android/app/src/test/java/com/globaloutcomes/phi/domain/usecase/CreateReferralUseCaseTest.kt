package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CreateReferralUseCaseTest {

    private lateinit var useCase: CreateReferralUseCase
    private lateinit var mockReferralRepository: ReferralRepository
    private lateinit var mockPatientRepository: PatientRepository

    private val testPatientId = "patient-123"
    private val testScanId = "scan-456"

    @Before
    fun setup() {
        mockReferralRepository = mockk()
        mockPatientRepository = mockk()
        useCase = CreateReferralUseCase(mockReferralRepository, mockPatientRepository)
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    // ========== HIGH RISK REFERRALS ==========

    @Test
    fun `creates referral for HIGH risk scan`() = runTest {
        // Arrange
        val riskFlags = listOf("Hypertension", "High Blood Glucose")
        val referralSlot = slot<Referral>()

        coEvery { mockReferralRepository.insertReferral(capture(referralSlot)) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        val result = useCase(testPatientId, testScanId, RiskLevel.HIGH, riskFlags)

        // Assert
        assertTrue(result.isSuccess)

        val referral = referralSlot.captured
        assertEquals(testPatientId, referral.patientId)
        assertEquals(testScanId, referral.scanId)
        assertEquals(RiskLevel.HIGH, referral.riskLevel)
        assertEquals(riskFlags, referral.riskFlags)
        assertEquals(ReferralTier.BHW_TO_BHS, referral.tier)
        assertEquals(ReferralStatus.PENDING, referral.status)
        assertEquals(SyncStatus.PENDING, referral.syncStatus)
    }

    @Test
    fun `creates referral for ELEVATED risk scan`() = runTest {
        // Arrange
        val riskFlags = listOf("Tachycardia")
        coEvery { mockReferralRepository.insertReferral(any()) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        val result = useCase(testPatientId, testScanId, RiskLevel.ELEVATED, riskFlags)

        // Assert
        assertTrue(result.isSuccess)
        coVerify { mockReferralRepository.insertReferral(match { it.riskLevel == RiskLevel.ELEVATED }) }
    }

    // ========== NORMAL RISK REJECTION ==========

    @Test
    fun `rejects NORMAL risk scan with error`() = runTest {
        // Arrange
        val riskFlags = emptyList<String>()

        // Act
        val result = useCase(testPatientId, testScanId, RiskLevel.NORMAL, riskFlags)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Normal risk scans do not require referrals") == true)

        // Verify no database calls were made
        coVerify(exactly = 0) { mockReferralRepository.insertReferral(any()) }
        coVerify(exactly = 0) { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) }
    }

    // ========== DUE DATE CALCULATION ==========

    @Test
    fun `sets dueBy to 48 hours from now`() = runTest {
        // Arrange
        val referralSlot = slot<Referral>()
        val beforeTime = System.currentTimeMillis()

        coEvery { mockReferralRepository.insertReferral(capture(referralSlot)) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        useCase(testPatientId, testScanId, RiskLevel.HIGH, listOf("Hypertension"))

        val afterTime = System.currentTimeMillis()

        // Assert
        val referral = referralSlot.captured
        val expectedDueBy = referral.referredAt + (48 * 60 * 60 * 1000)
        assertEquals(expectedDueBy, referral.dueBy)

        // Verify dueBy is approximately 48 hours from now (allow 1 second tolerance)
        val expectedMinDueBy = beforeTime + (48 * 60 * 60 * 1000)
        val expectedMaxDueBy = afterTime + (48 * 60 * 60 * 1000)
        assertTrue(referral.dueBy in expectedMinDueBy..expectedMaxDueBy)
    }

    // ========== PATIENT RISK STATUS UPDATE ==========

    @Test
    fun `updates patient risk status with high risk flag`() = runTest {
        // Arrange
        val riskFlags = listOf("Hypertension", "Low Oxygen Saturation")
        coEvery { mockReferralRepository.insertReferral(any()) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        useCase(testPatientId, testScanId, RiskLevel.HIGH, riskFlags)

        // Assert
        coVerify {
            mockPatientRepository.updateRiskStatus(
                patientId = testPatientId,
                highRiskFlag = true,
                highRiskReasons = riskFlags,
                timestamp = any()
            )
        }
    }

    @Test
    fun `patient risk update failure does not prevent referral creation`() = runTest {
        // Arrange
        coEvery { mockReferralRepository.insertReferral(any()) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns
            Result.failure(Exception("Patient update failed"))

        // Act
        val result = useCase(testPatientId, testScanId, RiskLevel.HIGH, listOf("Hypertension"))

        // Assert
        // Referral creation still succeeds even if patient update fails
        assertTrue(result.isSuccess)
    }

    // ========== MULTIPLE RISK FLAGS ==========

    @Test
    fun `handles multiple risk flags correctly`() = runTest {
        // Arrange
        val riskFlags = listOf(
            "Hypertension",
            "High Blood Glucose",
            "Low Oxygen Saturation",
            "Tachycardia",
            "High Cholesterol"
        )
        val referralSlot = slot<Referral>()

        coEvery { mockReferralRepository.insertReferral(capture(referralSlot)) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        useCase(testPatientId, testScanId, RiskLevel.HIGH, riskFlags)

        // Assert
        val referral = referralSlot.captured
        assertEquals(5, referral.riskFlags.size)
        assertTrue(referral.riskFlags.contains("Hypertension"))
        assertTrue(referral.riskFlags.contains("High Blood Glucose"))
        assertTrue(referral.riskFlags.contains("Low Oxygen Saturation"))
    }

    @Test
    fun `handles empty risk flags list`() = runTest {
        // Arrange
        val riskFlags = emptyList<String>()
        val referralSlot = slot<Referral>()

        coEvery { mockReferralRepository.insertReferral(capture(referralSlot)) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        useCase(testPatientId, testScanId, RiskLevel.ELEVATED, riskFlags)

        // Assert
        val referral = referralSlot.captured
        assertTrue(referral.riskFlags.isEmpty())
    }

    // ========== INITIAL VALUES VERIFICATION ==========

    @Test
    fun `referral starts with BHW_TO_BHS tier`() = runTest {
        // Arrange
        val referralSlot = slot<Referral>()
        coEvery { mockReferralRepository.insertReferral(capture(referralSlot)) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        useCase(testPatientId, testScanId, RiskLevel.HIGH, listOf("Hypertension"))

        // Assert
        assertEquals(ReferralTier.BHW_TO_BHS, referralSlot.captured.tier)
    }

    @Test
    fun `referral starts with PENDING status`() = runTest {
        // Arrange
        val referralSlot = slot<Referral>()
        coEvery { mockReferralRepository.insertReferral(capture(referralSlot)) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        useCase(testPatientId, testScanId, RiskLevel.HIGH, listOf("Hypertension"))

        // Assert
        assertEquals(ReferralStatus.PENDING, referralSlot.captured.status)
    }

    @Test
    fun `referral has null resolution fields initially`() = runTest {
        // Arrange
        val referralSlot = slot<Referral>()
        coEvery { mockReferralRepository.insertReferral(capture(referralSlot)) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        useCase(testPatientId, testScanId, RiskLevel.HIGH, listOf("Hypertension"))

        // Assert
        val referral = referralSlot.captured
        assertEquals(null, referral.resolvedAt)
        assertEquals(null, referral.resolvedBy)
        assertEquals(null, referral.resolutionNotes)
    }

    // ========== ERROR HANDLING ==========

    @Test
    fun `repository insert failure returns failure`() = runTest {
        // Arrange
        coEvery { mockReferralRepository.insertReferral(any()) } returns
            Result.failure(Exception("Database error"))

        // Act
        val result = useCase(testPatientId, testScanId, RiskLevel.HIGH, listOf("Hypertension"))

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `exception during creation returns failure`() = runTest {
        // Arrange
        coEvery { mockReferralRepository.insertReferral(any()) } throws Exception("Unexpected error")

        // Act
        val result = useCase(testPatientId, testScanId, RiskLevel.HIGH, listOf("Hypertension"))

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Unexpected error", result.exceptionOrNull()?.message)
    }

    // ========== TIMESTAMP VERIFICATION ==========

    @Test
    fun `createdAt and updatedAt timestamps are set to current time`() = runTest {
        // Arrange
        val referralSlot = slot<Referral>()
        val beforeTime = System.currentTimeMillis()

        coEvery { mockReferralRepository.insertReferral(capture(referralSlot)) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        useCase(testPatientId, testScanId, RiskLevel.HIGH, listOf("Hypertension"))

        val afterTime = System.currentTimeMillis()

        // Assert
        val referral = referralSlot.captured
        assertTrue(referral.createdAt in beforeTime..afterTime)
        assertTrue(referral.updatedAt in beforeTime..afterTime)
        assertTrue(referral.referredAt in beforeTime..afterTime)
        assertEquals(referral.createdAt, referral.updatedAt)
    }

    // ========== SYNC STATUS ==========

    @Test
    fun `referral sync status is PENDING initially`() = runTest {
        // Arrange
        val referralSlot = slot<Referral>()
        coEvery { mockReferralRepository.insertReferral(capture(referralSlot)) } returns Result.success("referral-id")
        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        useCase(testPatientId, testScanId, RiskLevel.HIGH, listOf("Hypertension"))

        // Assert
        val referral = referralSlot.captured
        assertEquals(SyncStatus.PENDING, referral.syncStatus)
        assertEquals(null, referral.syncedAt)
    }
}
