package com.globaloutcomes.phi.presentation.patient.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ReferralRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
import com.globaloutcomes.phi.domain.repository.SurveyRepository
import com.globaloutcomes.phi.domain.usecase.DeletePatientUseCase
import com.globaloutcomes.phi.domain.usecase.UpdatePatientUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PatientDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: PatientDetailViewModel
    private lateinit var mockPatientRepository: PatientRepository
    private lateinit var mockScanRepository: ScanRepository
    private lateinit var mockSurveyRepository: SurveyRepository
    private lateinit var mockReferralRepository: ReferralRepository
    private lateinit var mockUpdatePatientUseCase: UpdatePatientUseCase
    private lateinit var mockDeletePatientUseCase: DeletePatientUseCase
    private lateinit var savedStateHandle: SavedStateHandle

    private val testPatientId = "patient-123"
    private val now = System.currentTimeMillis()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockPatientRepository = mockk()
        mockScanRepository = mockk()
        mockSurveyRepository = mockk()
        mockReferralRepository = mockk()
        mockUpdatePatientUseCase = mockk()
        mockDeletePatientUseCase = mockk()

        savedStateHandle = SavedStateHandle(mapOf("patientId" to testPatientId))

        // Default mock setups
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns
            Result.success(createPatient())
        coEvery { mockScanRepository.getScansForPatient(testPatientId) } returns
            Result.success(emptyList())
        coEvery { mockSurveyRepository.getSurveysByPatientId(testPatientId) } returns
            Result.success(emptyList())
        coEvery { mockReferralRepository.getReferralsByPatientId(testPatientId) } returns
            Result.success(emptyList())
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ========== INITIAL STATE AND LOADING ==========

    @Test
    fun `initial state has default values`() = runTest {
        // Act
        viewModel = createViewModel()
        // Don't advance - check immediate state

        // Assert
        val state = viewModel.state.value
        assertNull(state.patient)
        assertTrue(state.scans.isEmpty())
        assertTrue(state.surveys.isEmpty())
        assertTrue(state.referrals.isEmpty())
        assertEquals(PatientDetailTab.OVERVIEW, state.selectedTab)
        assertFalse(state.isEditMode)
        assertNull(state.errorMessage)
        assertFalse(state.deleteConfirmationVisible)
    }

    @Test
    fun `loads patient details on init`() = runTest {
        // Arrange
        val patient = createPatient(firstName = "Juan", lastName = "Dela Cruz")
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns Result.success(patient)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertNotNull(state.patient)
        assertEquals("Juan", state.patient?.firstName)
        assertEquals("Dela Cruz", state.patient?.lastName)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loads scans surveys and referrals in parallel`() = runTest {
        // Arrange
        val patient = createPatient()
        val scans = listOf(createScan("scan-1"), createScan("scan-2"))
        val surveys = listOf(createSurvey("survey-1"))
        val referrals = listOf(createReferral("ref-1"))

        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns Result.success(patient)
        coEvery { mockScanRepository.getScansForPatient(testPatientId) } returns Result.success(scans)
        coEvery { mockSurveyRepository.getSurveysByPatientId(testPatientId) } returns Result.success(surveys)
        coEvery { mockReferralRepository.getReferralsByPatientId(testPatientId) } returns Result.success(referrals)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertEquals(2, state.scans.size)
        assertEquals(1, state.surveys.size)
        assertEquals(1, state.referrals.size)
    }

    @Test
    fun `patient not found sets error message`() = runTest {
        // Arrange
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns Result.success(null)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertNull(state.patient)
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage?.contains("Patient not found") == true)
    }

    @Test
    fun `patient repository failure sets error message`() = runTest {
        // Arrange
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns
            Result.failure(Exception("Database error"))

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertNull(state.patient)
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage?.contains("Failed to load patient") == true)
    }

    @Test
    fun `scan repository failure does not prevent patient load`() = runTest {
        // Arrange
        val patient = createPatient()
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns Result.success(patient)
        coEvery { mockScanRepository.getScansForPatient(testPatientId) } returns
            Result.failure(Exception("Scan load failed"))

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertNotNull(state.patient) // Patient still loaded
        assertTrue(state.scans.isEmpty()) // Scans defaulted to empty
        assertNull(state.errorMessage) // No error message
    }

    // ========== SCANS AND REFERRALS LOADING ==========

    @Test
    fun `loads multiple scans correctly`() = runTest {
        // Arrange
        val oldScan = createScan("scan-1", scannedAt = now - 10000)
        val newestScan = createScan("scan-2", scannedAt = now)
        val middleScan = createScan("scan-3", scannedAt = now - 5000)

        coEvery { mockScanRepository.getScansForPatient(testPatientId) } returns
            Result.success(listOf(oldScan, newestScan, middleScan))

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(3, viewModel.state.value.scans.size)
    }

    @Test
    fun `loads scans with different risk levels`() = runTest {
        // Arrange
        val scans = listOf(
            createScan("s1", riskLevel = RiskLevel.NORMAL),
            createScan("s2", riskLevel = RiskLevel.ELEVATED),
            createScan("s3", riskLevel = RiskLevel.HIGH),
            createScan("s4", riskLevel = RiskLevel.NORMAL),
            createScan("s5", riskLevel = RiskLevel.HIGH)
        )
        coEvery { mockScanRepository.getScansForPatient(testPatientId) } returns Result.success(scans)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(5, viewModel.state.value.scans.size)
        val highRiskScans = viewModel.state.value.scans.filter { it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.ELEVATED }
        assertEquals(3, highRiskScans.size)
    }

    @Test
    fun `loads referrals with different statuses`() = runTest {
        // Arrange
        val referrals = listOf(
            createReferral("r1", status = ReferralStatus.PENDING),
            createReferral("r2", status = ReferralStatus.CONFIRMED),
            createReferral("r3", status = ReferralStatus.RESOLVED),
            createReferral("r4", status = ReferralStatus.PENDING),
            createReferral("r5", status = ReferralStatus.OVERDUE)
        )
        coEvery { mockReferralRepository.getReferralsByPatientId(testPatientId) } returns Result.success(referrals)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(5, viewModel.state.value.referrals.size)
        val activeReferrals = viewModel.state.value.referrals.filter {
            it.status == ReferralStatus.PENDING || it.status == ReferralStatus.CONFIRMED
        }
        assertEquals(3, activeReferrals.size)
    }

    // ========== TAB SELECTION ==========

    @Test
    fun `SelectTab event changes selected tab`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.SelectTab(PatientDetailTab.SCANS))

        // Assert
        assertEquals(PatientDetailTab.SCANS, viewModel.state.value.selectedTab)

        // Act
        viewModel.onEvent(PatientDetailEvent.SelectTab(PatientDetailTab.SURVEYS))

        // Assert
        assertEquals(PatientDetailTab.SURVEYS, viewModel.state.value.selectedTab)
    }

    // ========== EDIT MODE ==========

    @Test
    fun `ToggleEditMode enters edit mode and saves original`() = runTest {
        // Arrange
        val patient = createPatient(firstName = "Juan")
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns Result.success(patient)
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.ToggleEditMode)

        // Assert
        assertTrue(viewModel.state.value.isEditMode)
    }

    @Test
    fun `ToggleEditMode exits edit mode and restores original`() = runTest {
        // Arrange
        val patient = createPatient(firstName = "Juan")
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns Result.success(patient)
        viewModel = createViewModel()
        advanceUntilIdle()

        // Enter edit mode
        viewModel.onEvent(PatientDetailEvent.ToggleEditMode)
        // Make changes
        viewModel.onEvent(PatientDetailEvent.UpdateFirstName("Pedro"))
        assertEquals("Pedro", viewModel.state.value.patient?.firstName)

        // Act - Exit edit mode
        viewModel.onEvent(PatientDetailEvent.ToggleEditMode)

        // Assert - Changes reverted
        assertFalse(viewModel.state.value.isEditMode)
        assertEquals("Juan", viewModel.state.value.patient?.firstName)
    }

    @Test
    fun `CancelEdit reverts changes and exits edit mode`() = runTest {
        // Arrange
        val patient = createPatient(firstName = "Juan", lastName = "Dela Cruz")
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns Result.success(patient)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(PatientDetailEvent.ToggleEditMode)
        viewModel.onEvent(PatientDetailEvent.UpdateFirstName("Maria"))
        viewModel.onEvent(PatientDetailEvent.UpdateLastName("Santos"))

        // Act
        viewModel.onEvent(PatientDetailEvent.CancelEdit)

        // Assert
        assertFalse(viewModel.state.value.isEditMode)
        assertEquals("Juan", viewModel.state.value.patient?.firstName)
        assertEquals("Dela Cruz", viewModel.state.value.patient?.lastName)
    }

    // ========== FIELD UPDATES ==========

    @Test
    fun `UpdateFirstName changes first name in state`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.UpdateFirstName("Maria"))

        // Assert
        assertEquals("Maria", viewModel.state.value.patient?.firstName)
    }

    @Test
    fun `UpdateLastName changes last name in state`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.UpdateLastName("Santos"))

        // Assert
        assertEquals("Santos", viewModel.state.value.patient?.lastName)
    }

    @Test
    fun `UpdatePhoneNumber changes phone number`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.UpdatePhoneNumber("09181234567"))

        // Assert
        assertEquals("09181234567", viewModel.state.value.patient?.phoneNumber)
    }

    @Test
    fun `UpdatePregnancy to true sets isPregnant`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.UpdatePregnancy(true))

        // Assert
        assertTrue(viewModel.state.value.patient?.isPregnant == true)
    }

    @Test
    fun `UpdatePregnancy to false clears gestational age`() = runTest {
        // Arrange
        val patient = createPatient(isPregnant = true, gestationalAgeWeeks = 20)
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns Result.success(patient)
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.UpdatePregnancy(false))

        // Assert
        assertFalse(viewModel.state.value.patient?.isPregnant == true)
        assertNull(viewModel.state.value.patient?.gestationalAgeWeeks)
    }

    @Test
    fun `UpdateGestationalAge sets weeks`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.UpdateGestationalAge(25))

        // Assert
        assertEquals(25, viewModel.state.value.patient?.gestationalAgeWeeks)
    }

    @Test
    fun `UpdateMessengerOptIn changes opt-in status`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.UpdateMessengerOptIn(true))

        // Assert
        assertTrue(viewModel.state.value.patient?.messengerOptIn == true)
    }

    // ========== SAVE CHANGES ==========

    @Test
    fun `SaveChanges calls updatePatientUseCase with modified patient`() = runTest {
        // Arrange
        val originalPatient = createPatient(firstName = "Juan")
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns Result.success(originalPatient)
        coEvery { mockUpdatePatientUseCase(any()) } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(PatientDetailEvent.ToggleEditMode)
        viewModel.onEvent(PatientDetailEvent.UpdateFirstName("Pedro"))

        // Act
        viewModel.onEvent(PatientDetailEvent.SaveChanges)
        advanceUntilIdle()

        // Assert
        coVerify { mockUpdatePatientUseCase(match { it.firstName == "Pedro" }) }
        assertFalse(viewModel.state.value.isEditMode)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `SaveChanges failure sets error message`() = runTest {
        // Arrange
        coEvery { mockUpdatePatientUseCase(any()) } returns Result.failure(Exception("Update failed"))
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.SaveChanges)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.state.value.errorMessage?.contains("Failed to save changes") == true)
        assertFalse(viewModel.state.value.isSaving)
    }

    // ========== DELETE PATIENT ==========

    @Test
    fun `ShowDeleteConfirmation shows confirmation dialog`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.ShowDeleteConfirmation)

        // Assert
        assertTrue(viewModel.state.value.deleteConfirmationVisible)
    }

    @Test
    fun `HideDeleteConfirmation hides confirmation dialog`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(PatientDetailEvent.ShowDeleteConfirmation)

        // Act
        viewModel.onEvent(PatientDetailEvent.HideDeleteConfirmation)

        // Assert
        assertFalse(viewModel.state.value.deleteConfirmationVisible)
    }

    @Test
    fun `DeletePatient calls deletePatientUseCase`() = runTest {
        // Arrange
        coEvery { mockDeletePatientUseCase(testPatientId) } returns Result.success(Unit)
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.DeletePatient)
        advanceUntilIdle()

        // Assert
        coVerify { mockDeletePatientUseCase(testPatientId) }
        assertFalse(viewModel.state.value.deleteConfirmationVisible)
        assertFalse(viewModel.state.value.isDeleting)
        assertNull(viewModel.state.value.patient) // Signals successful deletion
    }

    @Test
    fun `DeletePatient failure sets error message`() = runTest {
        // Arrange
        coEvery { mockDeletePatientUseCase(testPatientId) } returns
            Result.failure(Exception("Delete failed"))
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PatientDetailEvent.DeletePatient)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.state.value.errorMessage?.contains("Failed to delete patient") == true)
        assertFalse(viewModel.state.value.isDeleting)
    }

    // ========== ERROR HANDLING ==========

    @Test
    fun `ClearError clears error message`() = runTest {
        // Arrange
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns
            Result.failure(Exception("Test error"))
        viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.errorMessage != null)

        // Act
        viewModel.onEvent(PatientDetailEvent.ClearError)

        // Assert
        assertNull(viewModel.state.value.errorMessage)
    }

    // ========== HELPER FUNCTIONS ==========

    private fun createViewModel() = PatientDetailViewModel(
        patientRepository = mockPatientRepository,
        scanRepository = mockScanRepository,
        surveyRepository = mockSurveyRepository,
        referralRepository = mockReferralRepository,
        updatePatientUseCase = mockUpdatePatientUseCase,
        deletePatientUseCase = mockDeletePatientUseCase,
        savedStateHandle = savedStateHandle
    )

    private fun createPatient(
        id: String = testPatientId,
        phoneNumber: String = "09171234567",
        firstName: String = "Juan",
        lastName: String = "Dela Cruz",
        birthdate: Long = now - (30L * 365 * 24 * 60 * 60 * 1000),
        sex: Sex = Sex.MALE,
        barangayId: String = "brgy-001",
        highRiskFlag: Boolean = false,
        isPregnant: Boolean = false,
        gestationalAgeWeeks: Int? = null,
        messengerOptIn: Boolean = false
    ) = Patient(
        id = id,
        phoneNumber = phoneNumber,
        firstName = firstName,
        lastName = lastName,
        birthdate = birthdate,
        sex = sex,
        barangayId = barangayId,
        highRiskFlag = highRiskFlag,
        isPregnant = isPregnant,
        gestationalAgeWeeks = gestationalAgeWeeks,
        messengerOptIn = messengerOptIn
    )

    private fun createScan(
        id: String,
        scannedAt: Long = now,
        riskLevel: RiskLevel = RiskLevel.NORMAL
    ) = Scan(
        id = id,
        patientId = testPatientId,
        scannedAt = scannedAt,
        scanDurationSeconds = 50,
        systolicBp = 120f,
        diastolicBp = 80f,
        heartRate = 72f,
        riskLevel = riskLevel,
        riskScore = 0.25f,
        signalQuality = SignalQuality.GOOD,
        qualityScore = 0.85f,
        biomarkersFull = emptyMap()
    )

    private fun createSurvey(id: String) = Survey(
        id = id,
        patientId = testPatientId,
        scanId = null,
        surveyType = SurveyType.NCD,
        completedAt = now,
        responses = emptyMap()
    )

    private fun createReferral(
        id: String,
        status: ReferralStatus = ReferralStatus.PENDING
    ) = Referral(
        id = id,
        patientId = testPatientId,
        scanId = "scan-1",
        tier = ReferralTier.BHW_TO_BHS,
        status = status,
        referredAt = now,
        dueBy = now + (48 * 60 * 60 * 1000),
        riskLevel = RiskLevel.HIGH,
        riskFlags = listOf("Hypertension"),
        resolvedAt = null,
        resolvedBy = null,
        resolutionNotes = null,
        createdAt = now,
        updatedAt = now,
        syncStatus = SyncStatus.PENDING,
        syncedAt = null
    )
}
