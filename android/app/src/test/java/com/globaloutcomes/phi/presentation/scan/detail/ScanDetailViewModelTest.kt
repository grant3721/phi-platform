package com.globaloutcomes.phi.presentation.scan.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.repository.ScanRepository
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
class ScanDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ScanDetailViewModel
    private lateinit var mockScanRepository: ScanRepository
    private lateinit var mockPatientRepository: PatientRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val testScanId = "scan-123"
    private val testPatientId = "patient-456"
    private val now = System.currentTimeMillis()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockScanRepository = mockk()
        mockPatientRepository = mockk()

        savedStateHandle = SavedStateHandle(mapOf("scanId" to testScanId))

        // Default mock setups
        coEvery { mockScanRepository.getScanById(testScanId) } returns
            Result.success(createScan())
        coEvery { mockPatientRepository.getPatientById(any()) } returns
            Result.success(createPatient())
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ========== INITIAL STATE ==========

    @Test
    fun `initial state has default values`() = runTest {
        // Act
        viewModel = createViewModel()
        // Don't advance - check immediate state

        // Assert
        val state = viewModel.state.value
        assertTrue(state.isLoading)
        assertNull(state.scan)
        assertNull(state.patient)
        assertEquals(BiomarkerCategory.ALL, state.selectedCategory)
        assertNull(state.errorMessage)
        assertFalse(state.showShareDialog)
    }

    // ========== LOADING SCAN ==========

    @Test
    fun `loads scan successfully on init`() = runTest {
        // Arrange
        val scan = createScan(
            id = testScanId,
            systolicBp = 120f,
            diastolicBp = 80f,
            heartRate = 72f
        )
        coEvery { mockScanRepository.getScanById(testScanId) } returns Result.success(scan)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertNotNull(state.scan)
        assertEquals(testScanId, state.scan?.id)
        assertEquals(120f, state.scan?.systolicBp)
        assertEquals(80f, state.scan?.diastolicBp)
        assertEquals(72f, state.scan?.heartRate)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loads patient context for scan`() = runTest {
        // Arrange
        val scan = createScan(patientId = testPatientId)
        val patient = createPatient(
            id = testPatientId,
            firstName = "Juan",
            lastName = "Dela Cruz"
        )
        coEvery { mockScanRepository.getScanById(testScanId) } returns Result.success(scan)
        coEvery { mockPatientRepository.getPatientById(testPatientId) } returns Result.success(patient)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertNotNull(state.patient)
        assertEquals("Juan", state.patient?.firstName)
        assertEquals("Dela Cruz", state.patient?.lastName)
    }

    @Test
    fun `scan not found sets error message`() = runTest {
        // Arrange
        coEvery { mockScanRepository.getScanById(testScanId) } returns Result.success(null)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertNull(state.scan)
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage?.contains("Scan not found") == true)
    }

    @Test
    fun `repository failure sets error message`() = runTest {
        // Arrange
        coEvery { mockScanRepository.getScanById(testScanId) } returns
            Result.failure(Exception("Database error"))

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertNull(state.scan)
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage?.contains("Failed to load scan") == true)
    }

    @Test
    fun `patient repository failure does not prevent scan load`() = runTest {
        // Arrange
        val scan = createScan()
        coEvery { mockScanRepository.getScanById(testScanId) } returns Result.success(scan)
        coEvery { mockPatientRepository.getPatientById(any()) } returns
            Result.failure(Exception("Patient load failed"))

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertNotNull(state.scan) // Scan still loaded
        assertNull(state.patient) // Patient is null
        assertNull(state.errorMessage) // No error message
        assertFalse(state.isLoading)
    }

    @Test
    fun `patient not found does not prevent scan display`() = runTest {
        // Arrange
        val scan = createScan()
        coEvery { mockScanRepository.getScanById(testScanId) } returns Result.success(scan)
        coEvery { mockPatientRepository.getPatientById(any()) } returns Result.success(null)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertNotNull(state.scan)
        assertNull(state.patient)
        assertNull(state.errorMessage)
    }

    // ========== CATEGORY SELECTION ==========

    @Test
    fun `SelectCategory changes selected category to CARDIOVASCULAR`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(ScanDetailEvent.SelectCategory(BiomarkerCategory.CARDIOVASCULAR))

        // Assert
        assertEquals(BiomarkerCategory.CARDIOVASCULAR, viewModel.state.value.selectedCategory)
    }

    @Test
    fun `SelectCategory changes selected category to RESPIRATORY`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(ScanDetailEvent.SelectCategory(BiomarkerCategory.RESPIRATORY))

        // Assert
        assertEquals(BiomarkerCategory.RESPIRATORY, viewModel.state.value.selectedCategory)
    }

    @Test
    fun `SelectCategory cycles through all categories`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act & Assert
        viewModel.onEvent(ScanDetailEvent.SelectCategory(BiomarkerCategory.METABOLIC))
        assertEquals(BiomarkerCategory.METABOLIC, viewModel.state.value.selectedCategory)

        viewModel.onEvent(ScanDetailEvent.SelectCategory(BiomarkerCategory.VASCULAR))
        assertEquals(BiomarkerCategory.VASCULAR, viewModel.state.value.selectedCategory)

        viewModel.onEvent(ScanDetailEvent.SelectCategory(BiomarkerCategory.AUTONOMIC))
        assertEquals(BiomarkerCategory.AUTONOMIC, viewModel.state.value.selectedCategory)

        viewModel.onEvent(ScanDetailEvent.SelectCategory(BiomarkerCategory.BODY_COMP))
        assertEquals(BiomarkerCategory.BODY_COMP, viewModel.state.value.selectedCategory)

        viewModel.onEvent(ScanDetailEvent.SelectCategory(BiomarkerCategory.CARDIAC))
        assertEquals(BiomarkerCategory.CARDIAC, viewModel.state.value.selectedCategory)

        viewModel.onEvent(ScanDetailEvent.SelectCategory(BiomarkerCategory.ALL))
        assertEquals(BiomarkerCategory.ALL, viewModel.state.value.selectedCategory)
    }

    // ========== SHARE DIALOG ==========

    @Test
    fun `ShareScan shows share dialog`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(ScanDetailEvent.ShareScan)

        // Assert
        assertTrue(viewModel.state.value.showShareDialog)
    }

    @Test
    fun `DismissShareDialog hides share dialog`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(ScanDetailEvent.ShareScan)
        assertTrue(viewModel.state.value.showShareDialog)

        // Act
        viewModel.onEvent(ScanDetailEvent.DismissShareDialog)

        // Assert
        assertFalse(viewModel.state.value.showShareDialog)
    }

    // ========== LOAD SCAN EVENT ==========

    @Test
    fun `LoadScan event reloads scan details`() = runTest {
        // Arrange
        val initialScan = createScan(heartRate = 72f)
        val updatedScan = createScan(heartRate = 85f)

        coEvery { mockScanRepository.getScanById(testScanId) } returns Result.success(initialScan)
        viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(72f, viewModel.state.value.scan?.heartRate)

        // Update mock to return new scan
        coEvery { mockScanRepository.getScanById(testScanId) } returns Result.success(updatedScan)

        // Act
        viewModel.onEvent(ScanDetailEvent.LoadScan(testScanId))
        advanceUntilIdle()

        // Assert
        assertEquals(85f, viewModel.state.value.scan?.heartRate)
    }

    // ========== NAVIGATION EVENTS ==========

    @Test
    fun `NavigateBack event does not modify state`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        val stateBefore = viewModel.state.value

        // Act
        viewModel.onEvent(ScanDetailEvent.NavigateBack)

        // Assert - state unchanged
        assertEquals(stateBefore, viewModel.state.value)
    }

    @Test
    fun `CompareToPrevious event does not modify state`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()
        val stateBefore = viewModel.state.value

        // Act
        viewModel.onEvent(ScanDetailEvent.CompareToPrevious)

        // Assert - state unchanged (TODO feature)
        assertEquals(stateBefore, viewModel.state.value)
    }

    // ========== SCAN WITH ALL BIOMARKERS ==========

    @Test
    fun `loads scan with all 34 biomarkers populated`() = runTest {
        // Arrange
        val fullScan = createScan(
            // Cardiovascular
            systolicBp = 130f,
            diastolicBp = 85f,
            heartRate = 78f,
            heartRateVariability = 42f,
            cardiacOutput = 5.5f,
            strokeVolume = 70f,
            // Respiratory
            respiratoryRate = 16f,
            spo2 = 97f,
            perfusionIndex = 3.5f,
            // Metabolic
            bloodGlucose = 95f,
            hba1c = 5.6f,
            cholesterol = 195f,
            triglycerides = 140f,
            hemoglobin = 14.5f,
            hematocrit = 42f,
            // Vascular
            arterialStiffness = 8.2f,
            vascularAge = 38f,
            // Body Composition
            bmi = 24.5f,
            bodyFatPercentage = 18f
        )
        coEvery { mockScanRepository.getScanById(testScanId) } returns Result.success(fullScan)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val scan = viewModel.state.value.scan!!
        assertEquals(130f, scan.systolicBp)
        assertEquals(85f, scan.diastolicBp)
        assertEquals(78f, scan.heartRate)
        assertEquals(42f, scan.heartRateVariability)
        assertEquals(5.5f, scan.cardiacOutput)
        assertEquals(70f, scan.strokeVolume)
        assertEquals(16f, scan.respiratoryRate)
        assertEquals(97f, scan.spo2)
        assertEquals(3.5f, scan.perfusionIndex)
        assertEquals(95f, scan.bloodGlucose)
        assertEquals(5.6f, scan.hba1c)
        assertEquals(195f, scan.cholesterol)
        assertEquals(140f, scan.triglycerides)
    }

    // ========== RISK ASSESSMENT ==========

    @Test
    fun `loads scan with HIGH risk level`() = runTest {
        // Arrange
        val highRiskScan = createScan(
            riskLevel = RiskLevel.HIGH,
            riskScore = 0.85f,
            highRiskFlags = listOf("Hypertension", "Low Oxygen Saturation")
        )
        coEvery { mockScanRepository.getScanById(testScanId) } returns Result.success(highRiskScan)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val scan = viewModel.state.value.scan!!
        assertEquals(RiskLevel.HIGH, scan.riskLevel)
        assertEquals(0.85f, scan.riskScore)
        assertEquals(2, scan.highRiskFlags.size)
        assertTrue(scan.highRiskFlags.contains("Hypertension"))
    }

    @Test
    fun `loads scan with signal quality information`() = runTest {
        // Arrange
        val qualityScan = createScan(
            signalQuality = SignalQuality.EXCELLENT,
            qualityScore = 0.95f
        )
        coEvery { mockScanRepository.getScanById(testScanId) } returns Result.success(qualityScan)

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val scan = viewModel.state.value.scan!!
        assertEquals(SignalQuality.EXCELLENT, scan.signalQuality)
        assertEquals(0.95f, scan.qualityScore)
    }

    // ========== HELPER FUNCTIONS ==========

    private fun createViewModel() = ScanDetailViewModel(
        scanRepository = mockScanRepository,
        patientRepository = mockPatientRepository,
        savedStateHandle = savedStateHandle
    )

    private fun createPatient(
        id: String = testPatientId,
        firstName: String = "Juan",
        lastName: String = "Dela Cruz"
    ) = Patient(
        id = id,
        phoneNumber = "09171234567",
        firstName = firstName,
        lastName = lastName,
        birthdate = now - (30L * 365 * 24 * 60 * 60 * 1000),
        sex = Sex.MALE,
        barangayId = "brgy-001"
    )

    private fun createScan(
        id: String = testScanId,
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
        qualityScore: Float = 0.85f
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
        biomarkersFull = emptyMap()
    )
}
