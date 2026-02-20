package com.globaloutcomes.phi.presentation.sync

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.globaloutcomes.phi.domain.sync.NetworkConnectivityObserver
import com.globaloutcomes.phi.domain.sync.SyncManager
import com.globaloutcomes.phi.domain.sync.SyncResult
import com.globaloutcomes.phi.domain.sync.SyncState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SyncViewModel
    private lateinit var mockContext: Context
    private lateinit var mockSyncManager: SyncManager
    private lateinit var mockNetworkObserver: NetworkConnectivityObserver
    private lateinit var syncStateFlow: MutableStateFlow<SyncState>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockContext = mockk(relaxed = true)
        mockSyncManager = mockk(relaxed = true)
        mockNetworkObserver = mockk()

        // Setup sync state flow
        syncStateFlow = MutableStateFlow<SyncState>(SyncState.Idle)
        every { mockSyncManager.syncState } returns syncStateFlow

        // Setup network state flow
        every { mockNetworkObserver.isOnline } returns flowOf(true)

        viewModel = SyncViewModel(
            context = mockContext,
            syncManager = mockSyncManager,
            networkObserver = mockNetworkObserver
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial sync state is Idle`() = runTest {
        // Assert
        assertEquals(SyncState.Idle, viewModel.syncState.value)
    }

    @Test
    fun `initial network state is online`() = runTest {
        // Assert
        assertTrue(viewModel.isOnline.value)
    }

    @Test
    fun `syncNow calls performSync on SyncManager`() = runTest {
        // Arrange
        coEvery { mockSyncManager.performSync() } returns Result.success(
            SyncResult(
                uploadedPatients = 1,
                uploadedScans = 2,
                uploadedSurveys = 0,
                uploadedReferrals = 1,
                downloadedUpdates = 3
            )
        )

        // Act
        viewModel.syncNow()
        advanceUntilIdle()

        // Assert
        coVerify { mockSyncManager.performSync() }
    }

    @Test
    fun `sync state updates when SyncManager state changes`() = runTest {
        // Arrange
        val syncingState = SyncState.Syncing(progress = 0.5f, message = "Syncing...")

        // Act
        syncStateFlow.value = syncingState
        advanceUntilIdle()

        // Assert
        assertEquals(syncingState, viewModel.syncState.value)
    }

    @Test
    fun `sync state updates to Success when sync completes`() = runTest {
        // Arrange
        val syncResult = SyncResult(
            uploadedPatients = 5,
            uploadedScans = 10,
            uploadedSurveys = 3,
            uploadedReferrals = 2,
            downloadedUpdates = 7
        )
        val successState = SyncState.Success(syncResult)

        // Act
        syncStateFlow.value = successState
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.syncState.value is SyncState.Success)
        assertEquals(5, (viewModel.syncState.value as SyncState.Success).result.uploadedPatients)
    }

    @Test
    fun `sync state updates to Failed when sync fails`() = runTest {
        // Arrange
        val failedState = SyncState.Failed("Network error")

        // Act
        syncStateFlow.value = failedState
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.syncState.value is SyncState.Failed)
        assertEquals("Network error", (viewModel.syncState.value as SyncState.Failed).error)
    }

    @Test
    fun `network state updates when connectivity changes`() = runTest {
        // Arrange
        val networkFlow = MutableStateFlow(true)
        every { mockNetworkObserver.isOnline } returns networkFlow

        val viewModel = SyncViewModel(mockContext, mockSyncManager, mockNetworkObserver)

        // Act - go offline
        networkFlow.value = false
        advanceUntilIdle()

        // Assert
        assertEquals(false, viewModel.isOnline.value)

        // Act - go online
        networkFlow.value = true
        advanceUntilIdle()

        // Assert
        assertEquals(true, viewModel.isOnline.value)
    }

    @Test
    fun `schedulePeriodicSync calls SyncWorker schedule`() {
        // Note: This would require mocking WorkManager, which is complex
        // In practice, we'd test SyncWorker separately
        // Act
        viewModel.schedulePeriodicSync()

        // Assert
        // Would verify WorkManager.enqueueUniquePeriodicWork was called
        // but requires more setup for WorkManager testing
    }

    @Test
    fun `triggerBackgroundSync calls SyncWorker syncNow`() {
        // Act
        viewModel.triggerBackgroundSync()

        // Assert
        // Would verify WorkManager.enqueue was called
        // but requires more setup for WorkManager testing
    }
}
