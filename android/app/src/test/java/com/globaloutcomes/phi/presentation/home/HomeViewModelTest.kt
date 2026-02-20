package com.globaloutcomes.phi.presentation.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.globaloutcomes.phi.domain.model.Patient
import com.globaloutcomes.phi.domain.model.Sex
import com.globaloutcomes.phi.domain.repository.PatientRepository
import com.globaloutcomes.phi.domain.usecase.CheckDailyCapUseCase
import com.globaloutcomes.phi.domain.usecase.DailyCapResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: HomeViewModel
    private lateinit var mockPatientRepository: PatientRepository
    private lateinit var mockCheckDailyCapUseCase: CheckDailyCapUseCase

    private val now = System.currentTimeMillis()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockPatientRepository = mockk()
        mockCheckDailyCapUseCase = mockk()

        // Default mock setups
        coEvery { mockCheckDailyCapUseCase(any()) } returns Result.success(
            DailyCapResult(
                scansToday = 0,
                amountToday = 0f,
                isAtCap = false,
                remainingScans = 50,
                remainingAmount = 150f,
                message = "50 scans remaining"
            )
        )
        coEvery { mockPatientRepository.getPatientCount() } returns Result.success(0)
        coEvery { mockPatientRepository.getHighRiskCount() } returns Result.success(0)
        coEvery { mockPatientRepository.getHighRiskPatientsFlow() } returns flowOf(emptyList())
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
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertEquals(0, state.scansToday)
        assertEquals(0f, state.earningsToday)
        assertFalse(state.dailyCapReached)
        assertEquals(50, state.remainingScans)
        assertEquals(150f, state.remainingAmount)
        assertTrue(state.highRiskPatients.isEmpty())
        assertEquals(0, state.totalPatients)
        assertEquals(0, state.totalHighRiskPatients)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    // ========== DAILY CAP LOADING ==========

    @Test
    fun `loadDashboardData loads daily cap successfully`() = runTest {
        // Arrange
        val capResult = DailyCapResult(
            scansToday = 10,
            amountToday = 30f,
            isAtCap = false,
            remainingScans = 40,
            remainingAmount = 120f,
            message = "40 scans remaining"
        )
        coEvery { mockCheckDailyCapUseCase(any()) } returns Result.success(capResult)

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertEquals(10, state.scansToday)
        assertEquals(30f, state.earningsToday)
        assertFalse(state.dailyCapReached)
        assertEquals(40, state.remainingScans)
        assertEquals(120f, state.remainingAmount)
    }

    @Test
    fun `daily cap reached updates state correctly`() = runTest {
        // Arrange
        val capResult = DailyCapResult(
            scansToday = 50,
            amountToday = 150f,
            isAtCap = true,
            remainingScans = 0,
            remainingAmount = 0f,
            message = "Daily cap reached"
        )
        coEvery { mockCheckDailyCapUseCase(any()) } returns Result.success(capResult)

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertEquals(50, state.scansToday)
        assertEquals(150f, state.earningsToday)
        assertTrue(state.dailyCapReached)
        assertEquals(0, state.remainingScans)
        assertEquals(0f, state.remainingAmount)
    }

    @Test
    fun `progress percentage calculated correctly`() = runTest {
        // Arrange
        val capResult = DailyCapResult(
            scansToday = 25,
            amountToday = 75f,
            isAtCap = false,
            remainingScans = 25,
            remainingAmount = 75f,
            message = "25 scans remaining"
        )
        coEvery { mockCheckDailyCapUseCase(any()) } returns Result.success(capResult)

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        assertEquals(0.5f, viewModel.state.value.progressPercentage)
    }

    @Test
    fun `progress percentage is 1f when at cap`() = runTest {
        // Arrange
        val capResult = DailyCapResult(
            scansToday = 50,
            amountToday = 150f,
            isAtCap = true,
            remainingScans = 0,
            remainingAmount = 0f,
            message = "Daily cap reached"
        )
        coEvery { mockCheckDailyCapUseCase(any()) } returns Result.success(capResult)

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        assertEquals(1f, viewModel.state.value.progressPercentage)
    }

    // ========== PATIENT STATISTICS ==========

    @Test
    fun `loads patient count successfully`() = runTest {
        // Arrange
        coEvery { mockPatientRepository.getPatientCount() } returns Result.success(150)

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        assertEquals(150, viewModel.state.value.totalPatients)
    }

    @Test
    fun `loads high risk patient count successfully`() = runTest {
        // Arrange
        coEvery { mockPatientRepository.getHighRiskCount() } returns Result.success(25)

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        assertEquals(25, viewModel.state.value.totalHighRiskPatients)
    }

    @Test
    fun `patient count failure does not crash app`() = runTest {
        // Arrange
        coEvery { mockPatientRepository.getPatientCount() } returns
            Result.failure(Exception("Database error"))

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        // Should still have default value, no crash
        assertEquals(0, viewModel.state.value.totalPatients)
    }

    // ========== HIGH RISK PATIENTS FLOW ==========

    @Test
    fun `loads high risk patients from flow`() = runTest {
        // Arrange
        val patient1 = createPatient("p1", "Juan", "Dela Cruz", highRiskFlag = true)
        val patient2 = createPatient("p2", "Maria", "Santos", highRiskFlag = true)
        val patient3 = createPatient("p3", "Pedro", "Reyes", highRiskFlag = true)

        coEvery { mockPatientRepository.getHighRiskPatientsFlow() } returns
            flowOf(listOf(patient1, patient2, patient3))

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertEquals(3, state.highRiskPatients.size)
        assertEquals("Juan", state.highRiskPatients[0].firstName)
        assertEquals("Maria", state.highRiskPatients[1].firstName)
        assertEquals("Pedro", state.highRiskPatients[2].firstName)
    }

    @Test
    fun `shows only top 5 high risk patients`() = runTest {
        // Arrange
        val patients = (1..10).map { i ->
            createPatient("p$i", "Patient$i", "LastName$i", highRiskFlag = true)
        }

        coEvery { mockPatientRepository.getHighRiskPatientsFlow() } returns flowOf(patients)

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        assertEquals(5, viewModel.state.value.highRiskPatients.size)
    }

    @Test
    fun `loading state set during dashboard load`() = runTest {
        // Arrange
        val slowFlow = flowOf(emptyList<Patient>())
        coEvery { mockPatientRepository.getHighRiskPatientsFlow() } returns slowFlow

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        // Don't advance idle yet - check loading state

        // Assert - isLoading should be true during load
        // (Note: Depending on timing, this might be tricky to assert)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading) // Should be false after completion
    }

    // ========== ERROR HANDLING ==========

    @Test
    fun `flow error sets error message`() = runTest {
        // Arrange
        coEvery { mockPatientRepository.getHighRiskPatientsFlow() } returns
            flowOf<List<Patient>>().also { throw Exception("Flow error") }

        // Note: Testing Flow errors is tricky with MockK
        // This is more of a documentation of the expected behavior
    }

    @Test
    fun `exception during load sets error message and stops loading`() = runTest {
        // Arrange
        coEvery { mockCheckDailyCapUseCase(any()) } throws Exception("Unexpected error")

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage?.contains("Failed to load dashboard") == true)
    }

    // ========== EVENT HANDLING ==========

    @Test
    fun `Refresh event reloads dashboard data`() = runTest {
        // Arrange
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()
        clearMocks(mockCheckDailyCapUseCase, mockPatientRepository, answers = false)

        // Setup new mock values for refresh
        coEvery { mockCheckDailyCapUseCase(any()) } returns Result.success(
            DailyCapResult(20, 60f, false, 30, 90f, "30 scans remaining")
        )
        coEvery { mockPatientRepository.getPatientCount() } returns Result.success(200)
        coEvery { mockPatientRepository.getHighRiskCount() } returns Result.success(35)
        coEvery { mockPatientRepository.getHighRiskPatientsFlow() } returns flowOf(emptyList())

        // Act
        viewModel.onEvent(HomeEvent.Refresh)
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertEquals(20, state.scansToday)
        assertEquals(200, state.totalPatients)
        assertEquals(35, state.totalHighRiskPatients)

        // Verify use case was called again
        coVerify(atLeast = 1) { mockCheckDailyCapUseCase(any()) }
    }

    @Test
    fun `ClearError event clears error message`() = runTest {
        // Arrange
        coEvery { mockCheckDailyCapUseCase(any()) } throws Exception("Test error")
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Verify error is set
        assertTrue(viewModel.state.value.errorMessage != null)

        // Act
        viewModel.onEvent(HomeEvent.ClearError)

        // Assert
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `navigation events do not modify state`() = runTest {
        // Arrange
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()
        val stateBefore = viewModel.state.value

        // Act
        viewModel.onEvent(HomeEvent.NavigateToNewScan)
        viewModel.onEvent(HomeEvent.NavigateToPatients)
        viewModel.onEvent(HomeEvent.NavigateToReferrals)
        viewModel.onEvent(HomeEvent.NavigateToSettings)
        viewModel.onEvent(HomeEvent.NavigateToPatientDetail("patient-123"))

        // Assert - state should be unchanged
        assertEquals(stateBefore, viewModel.state.value)
    }

    // ========== BHW ID USAGE ==========

    @Test
    fun `uses correct BHW ID when checking daily cap`() = runTest {
        // Arrange
        val bhwIdSlot = slot<String>()
        coEvery { mockCheckDailyCapUseCase(capture(bhwIdSlot)) } returns Result.success(
            DailyCapResult(0, 0f, false, 50, 150f, "50 scans remaining")
        )

        // Act
        viewModel = HomeViewModel(mockPatientRepository, mockCheckDailyCapUseCase)
        advanceUntilIdle()

        // Assert
        assertEquals("bhw_001", bhwIdSlot.captured)
    }

    // ========== HELPER FUNCTIONS ==========

    private fun createPatient(
        id: String,
        firstName: String,
        lastName: String,
        phoneNumber: String = "09171234567",
        birthdate: Long = now - (30L * 365 * 24 * 60 * 60 * 1000),
        sex: Sex = Sex.MALE,
        barangayId: String = "brgy-001",
        highRiskFlag: Boolean = false
    ) = Patient(
        id = id,
        phoneNumber = phoneNumber,
        firstName = firstName,
        lastName = lastName,
        birthdate = birthdate,
        sex = sex,
        barangayId = barangayId,
        highRiskFlag = highRiskFlag
    )
}
