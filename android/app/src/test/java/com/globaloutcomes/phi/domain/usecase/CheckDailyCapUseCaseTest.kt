package com.globaloutcomes.phi.domain.usecase

import com.globaloutcomes.phi.data.local.dao.BhwIncentiveDao
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
class CheckDailyCapUseCaseTest {

    private lateinit var useCase: CheckDailyCapUseCase
    private lateinit var mockBhwIncentiveDao: BhwIncentiveDao

    private val testBhwId = "bhw-123"

    @Before
    fun setup() {
        mockBhwIncentiveDao = mockk()
        useCase = CheckDailyCapUseCase(mockBhwIncentiveDao)
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    // ========== ZERO SCANS (NOT AT CAP) ==========

    @Test
    fun `zero scans today returns not at cap with full remaining`() = runTest {
        // Arrange
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(testBhwId, any()) } returns 0
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(testBhwId, any()) } returns 0f

        // Act
        val result = useCase(testBhwId)

        // Assert
        assertTrue(result.isSuccess)
        val capResult = result.getOrNull()!!
        assertEquals(0, capResult.scansToday)
        assertEquals(0f, capResult.amountToday)
        assertFalse(capResult.isAtCap)
        assertEquals(50, capResult.remainingScans)
        assertEquals(150.00f, capResult.remainingAmount)
        assertTrue(capResult.message.contains("50 scans remaining"))
        assertTrue(capResult.message.contains("150.00"))
    }

    // ========== BELOW CAP ==========

    @Test
    fun `1 scan today returns correct remaining counts`() = runTest {
        // Arrange
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(testBhwId, any()) } returns 1
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(testBhwId, any()) } returns 3.00f

        // Act
        val result = useCase(testBhwId)

        // Assert
        assertTrue(result.isSuccess)
        val capResult = result.getOrNull()!!
        assertEquals(1, capResult.scansToday)
        assertEquals(3.00f, capResult.amountToday)
        assertFalse(capResult.isAtCap)
        assertEquals(49, capResult.remainingScans)
        assertEquals(147.00f, capResult.remainingAmount)
    }

    @Test
    fun `10 scans today returns correct calculations`() = runTest {
        // Arrange
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(testBhwId, any()) } returns 10
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(testBhwId, any()) } returns 30.00f

        // Act
        val result = useCase(testBhwId)

        // Assert
        assertTrue(result.isSuccess)
        val capResult = result.getOrNull()!!
        assertEquals(10, capResult.scansToday)
        assertEquals(30.00f, capResult.amountToday)
        assertFalse(capResult.isAtCap)
        assertEquals(40, capResult.remainingScans)
        assertEquals(120.00f, capResult.remainingAmount)
        assertTrue(capResult.message.contains("40 scans remaining"))
    }

    @Test
    fun `25 scans today (halfway) returns correct remaining`() = runTest {
        // Arrange
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(testBhwId, any()) } returns 25
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(testBhwId, any()) } returns 75.00f

        // Act
        val result = useCase(testBhwId)

        // Assert
        assertTrue(result.isSuccess)
        val capResult = result.getOrNull()!!
        assertEquals(25, capResult.scansToday)
        assertEquals(75.00f, capResult.amountToday)
        assertFalse(capResult.isAtCap)
        assertEquals(25, capResult.remainingScans)
        assertEquals(75.00f, capResult.remainingAmount)
    }

    @Test
    fun `49 scans today (one away from cap) returns correct values`() = runTest {
        // Arrange
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(testBhwId, any()) } returns 49
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(testBhwId, any()) } returns 147.00f

        // Act
        val result = useCase(testBhwId)

        // Assert
        assertTrue(result.isSuccess)
        val capResult = result.getOrNull()!!
        assertEquals(49, capResult.scansToday)
        assertEquals(147.00f, capResult.amountToday)
        assertFalse(capResult.isAtCap)
        assertEquals(1, capResult.remainingScans)
        assertEquals(3.00f, capResult.remainingAmount)
        assertTrue(capResult.message.contains("1 scans remaining"))
    }

    // ========== EXACTLY AT CAP ==========

    @Test
    fun `exactly 50 scans today returns at cap`() = runTest {
        // Arrange
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(testBhwId, any()) } returns 50
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(testBhwId, any()) } returns 150.00f

        // Act
        val result = useCase(testBhwId)

        // Assert
        assertTrue(result.isSuccess)
        val capResult = result.getOrNull()!!
        assertEquals(50, capResult.scansToday)
        assertEquals(150.00f, capResult.amountToday)
        assertTrue(capResult.isAtCap)
        assertEquals(0, capResult.remainingScans)
        assertEquals(0f, capResult.remainingAmount)
        assertTrue(capResult.message.contains("Daily cap reached"))
        assertTrue(capResult.message.contains("₱150.00"))
    }

    // ========== ABOVE CAP (EDGE CASE) ==========

    @Test
    fun `above 50 scans (edge case) still returns at cap`() = runTest {
        // Arrange
        // This shouldn't happen in production, but test the edge case
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(testBhwId, any()) } returns 55
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(testBhwId, any()) } returns 165.00f

        // Act
        val result = useCase(testBhwId)

        // Assert
        assertTrue(result.isSuccess)
        val capResult = result.getOrNull()!!
        assertEquals(55, capResult.scansToday)
        assertEquals(165.00f, capResult.amountToday)
        assertTrue(capResult.isAtCap)
        assertEquals(0, capResult.remainingScans)
        assertEquals(0f, capResult.remainingAmount)
        assertTrue(capResult.message.contains("Daily cap reached"))
    }

    // ========== NULL AMOUNT HANDLING ==========

    @Test
    fun `null total amount from database defaults to zero`() = runTest {
        // Arrange
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(testBhwId, any()) } returns 0
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(testBhwId, any()) } returns null

        // Act
        val result = useCase(testBhwId)

        // Assert
        assertTrue(result.isSuccess)
        val capResult = result.getOrNull()!!
        assertEquals(0f, capResult.amountToday)
        assertEquals(150.00f, capResult.remainingAmount)
    }

    // ========== DIFFERENT BHW IDs ==========

    @Test
    fun `different BHW IDs maintain separate counts`() = runTest {
        // Arrange
        val bhw1 = "bhw-001"
        val bhw2 = "bhw-002"

        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(bhw1, any()) } returns 10
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(bhw1, any()) } returns 30.00f

        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(bhw2, any()) } returns 25
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(bhw2, any()) } returns 75.00f

        // Act
        val result1 = useCase(bhw1)
        val result2 = useCase(bhw2)

        // Assert
        assertTrue(result1.isSuccess)
        val cap1 = result1.getOrNull()!!
        assertEquals(10, cap1.scansToday)
        assertEquals(40, cap1.remainingScans)

        assertTrue(result2.isSuccess)
        val cap2 = result2.getOrNull()!!
        assertEquals(25, cap2.scansToday)
        assertEquals(25, cap2.remainingScans)
    }

    // ========== ERROR HANDLING ==========

    @Test
    fun `database error returns failure`() = runTest {
        // Arrange
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(any(), any()) } throws Exception("Database connection failed")

        // Act
        val result = useCase(testBhwId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Database connection failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getTotalEarnedForDay error returns failure`() = runTest {
        // Arrange
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(any(), any()) } returns 10
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(any(), any()) } throws Exception("Query failed")

        // Act
        val result = useCase(testBhwId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Query failed", result.exceptionOrNull()?.message)
    }

    // ========== VERIFY CORRECT DATE CALCULATION ==========

    @Test
    fun `queries database with start of day timestamp`() = runTest {
        // Arrange
        val timestampSlot = slot<Long>()
        coEvery { mockBhwIncentiveDao.getEarnedCountForDay(testBhwId, capture(timestampSlot)) } returns 5
        coEvery { mockBhwIncentiveDao.getTotalEarnedForDay(testBhwId, capture(timestampSlot)) } returns 15.00f

        // Act
        useCase(testBhwId)

        // Assert
        // Verify that the timestamp is a start-of-day value (should be at midnight)
        val capturedTimestamp = timestampSlot.captured
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = capturedTimestamp

        assertEquals(0, calendar.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(java.util.Calendar.MINUTE))
        assertEquals(0, calendar.get(java.util.Calendar.SECOND))
        assertEquals(0, calendar.get(java.util.Calendar.MILLISECOND))
    }

    // ========== CONSTANTS VERIFICATION ==========

    @Test
    fun `constants have correct values`() {
        assertEquals(50, CheckDailyCapUseCase.MAX_SCANS_PER_DAY)
        assertEquals(150.00f, CheckDailyCapUseCase.MAX_DAILY_AMOUNT)
        assertEquals(3.00f, CheckDailyCapUseCase.AMOUNT_PER_SCAN)
    }

    @Test
    fun `max daily amount equals max scans times amount per scan`() {
        val calculatedMaxAmount = CheckDailyCapUseCase.MAX_SCANS_PER_DAY * CheckDailyCapUseCase.AMOUNT_PER_SCAN
        assertEquals(CheckDailyCapUseCase.MAX_DAILY_AMOUNT, calculatedMaxAmount)
    }
}
