package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CheckRescanEligibilityUseCaseTest {

    private lateinit var useCase: CheckRescanEligibilityUseCase
    private lateinit var mockScanRepository: ScanRepository
    private lateinit var mockPatientRepository: PatientRepository

    private val now = System.currentTimeMillis()
    private val oneDayAgo = now - (1 * 24 * 60 * 60 * 1000L)
    private val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000L)
    private val oneMonthAgo = now - (30 * 24 * 60 * 60 * 1000L)
    private val sixMonthsAgo = now - (180 * 24 * 60 * 60 * 1000L)
    private val oneYearAgo = now - (365 * 24 * 60 * 60 * 1000L)

    @Before
    fun setup() {
        mockScanRepository = mockk()
        mockPatientRepository = mockk()
        useCase = CheckRescanEligibilityUseCase(mockScanRepository)
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    // ========== BASELINE SCANS (NEW PATIENTS) ==========

    @Test
    fun `new patient with no scans is eligible for baseline scan`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId)
        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(null)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertTrue(eligibility.isEligible)
        assertEquals(EligibilityReason.BASELINE, eligibility.reason)
    }

    // ========== HIGH-RISK RESCANS ==========

    @Test
    fun `high-risk patient can rescan immediately`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId, highRiskFlag = true)
        val lastScan = createScan(scannedAt = oneDayAgo, riskLevel = RiskLevel.HIGH)

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(lastScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertTrue(eligibility.isEligible)
        assertEquals(EligibilityReason.HIGH_RISK, eligibility.reason)
    }

    @Test
    fun `elevated risk patient can rescan immediately`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId, highRiskFlag = true)
        val lastScan = createScan(scannedAt = oneDayAgo, riskLevel = RiskLevel.ELEVATED)

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(lastScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertTrue(eligibility.isEligible)
        assertEquals(EligibilityReason.HIGH_RISK, eligibility.reason)
    }

    // ========== MATERNAL HIGH-RISK RESCANS ==========

    @Test
    fun `pregnant high-risk patient can rescan immediately`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(
            patientId,
            isPregnant = true,
            maternalHighRisk = true,
            highRiskFlag = true
        )
        val lastScan = createScan(scannedAt = oneDayAgo)

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(lastScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertTrue(eligibility.isEligible)
        assertEquals(EligibilityReason.MATERNAL_HIGH_RISK, eligibility.reason)
    }

    // ========== MATERNAL RESCANS (6 MONTHS) ==========

    @Test
    fun `pregnant patient can rescan after 6 months`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId, isPregnant = true)
        val lastScan = createScan(scannedAt = sixMonthsAgo)

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(lastScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertTrue(eligibility.isEligible)
        assertEquals(EligibilityReason.MATERNAL, eligibility.reason)
    }

    @Test
    fun `pregnant patient cannot rescan before 6 months`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId, isPregnant = true, highRiskFlag = false, maternalHighRisk = false)
        val lastScan = createScan(scannedAt = oneMonthAgo) // Only 1 month ago

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(lastScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertFalse(eligibility.isEligible)
        assertEquals(EligibilityReason.MATERNAL_TOO_SOON, eligibility.reason)
        assertTrue(eligibility.daysUntilEligible!! > 0)
    }

    // ========== ANNUAL RESCANS (12 MONTHS) ==========

    @Test
    fun `normal patient can rescan after 12 months`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId, highRiskFlag = false)
        val lastScan = createScan(scannedAt = oneYearAgo, riskLevel = RiskLevel.NORMAL)

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(lastScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertTrue(eligibility.isEligible)
        assertEquals(EligibilityReason.ANNUAL, eligibility.reason)
    }

    @Test
    fun `normal patient cannot rescan before 12 months`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId, highRiskFlag = false)
        val lastScan = createScan(scannedAt = sixMonthsAgo, riskLevel = RiskLevel.NORMAL) // Only 6 months ago

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(lastScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertFalse(eligibility.isEligible)
        assertEquals(EligibilityReason.TOO_SOON, eligibility.reason)
        assertTrue(eligibility.daysUntilEligible!! > 0)
    }

    // ========== REJECTION WITH DAYS CALCULATION ==========

    @Test
    fun `rejected patient shows correct days until eligible`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId, highRiskFlag = false)
        val lastScan = createScan(scannedAt = sixMonthsAgo, riskLevel = RiskLevel.NORMAL)

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(lastScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertFalse(eligibility.isEligible)

        // Should be approximately 180 days (12 months - 6 months)
        val expectedDays = 180
        val actualDays = eligibility.daysUntilEligible!!
        assertTrue(actualDays in (expectedDays - 2)..(expectedDays + 2)) // Allow 2 day tolerance
    }

    // ========== MULTIPLE SCANS (USE MOST RECENT) ==========

    @Test
    fun `uses most recent scan for eligibility check`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId, highRiskFlag = false)
        val oldScan = createScan(scannedAt = oneYearAgo) // Old enough
        val recentScan = createScan(scannedAt = oneDayAgo) // Too recent

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(recentScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertFalse(eligibility.isEligible) // Should use recent scan
        assertEquals(EligibilityReason.TOO_SOON, eligibility.reason)
    }

    // ========== EDGE CASES ==========

    @Test
    fun `patient not found returns failure`() = runTest {
        // Arrange
        val patientId = "nonexistent-patient"
        val patient = createPatient(patientId)
        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(null)

        // Act
        val result = useCase(patient)

        // Assert - New patient with no scans is eligible for baseline
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEligible)
    }

    @Test
    fun `repository failure returns failure`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId)
        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns
            Result.failure(Exception("Database error"))

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `exactly 12 months ago is eligible`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId, highRiskFlag = false)
        val exactlyOneYearAgo = now - (365 * 24 * 60 * 60 * 1000L)
        val lastScan = createScan(scannedAt = exactlyOneYearAgo, riskLevel = RiskLevel.NORMAL)

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(lastScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertTrue(eligibility.isEligible)
    }

    @Test
    fun `exactly 6 months ago for pregnant is eligible`() = runTest {
        // Arrange
        val patientId = "patient-123"
        val patient = createPatient(patientId, isPregnant = true)
        val exactlySixMonthsAgo = now - (180 * 24 * 60 * 60 * 1000L)
        val lastScan = createScan(scannedAt = exactlySixMonthsAgo)

        coEvery { mockScanRepository.getLatestScanForPatient(patientId) } returns Result.success(lastScan)

        // Act
        val result = useCase(patient)

        // Assert
        assertTrue(result.isSuccess)
        val eligibility = result.getOrNull()!!
        assertTrue(eligibility.isEligible)
    }

    // ========== HELPER FUNCTIONS ==========

    private fun createPatient(
        id: String,
        highRiskFlag: Boolean = false,
        maternalHighRisk: Boolean = false,
        isPregnant: Boolean = false
    ) = Patient(
        id = id,
        phoneNumber = "09171234567",
        firstName = "Juan",
        lastName = "Dela Cruz",
        birthdate = now - (30L * 365 * 24 * 60 * 60 * 1000),
        sex = Sex.MALE,
        barangayId = "brgy-001",
        highRiskFlag = highRiskFlag,
        maternalHighRisk = maternalHighRisk,
        isPregnant = isPregnant
    )

    private fun createScan(
        scannedAt: Long,
        riskLevel: RiskLevel = RiskLevel.NORMAL
    ) = Scan(
        id = "scan-${System.currentTimeMillis()}",
        patientId = "patient-123",
        scannedAt = scannedAt,
        scanDurationSeconds = 50,
        systolicBp = 120f,
        diastolicBp = 80f,
        heartRate = 72f,
        riskLevel = riskLevel,
        riskScore = if (riskLevel == RiskLevel.HIGH) 85f else 25f,
        signalQuality = SignalQuality.GOOD,
        qualityScore = 85f,
        biomarkersFull = emptyMap()
    )
}
