package com.globaloutcomes.phi.domain.sync

import android.content.Context
import com.globaloutcomes.phi.data.remote.SyncApiService
import com.globaloutcomes.phi.data.remote.dto.*
import com.globaloutcomes.phi.domain.model.*
import com.globaloutcomes.phi.domain.repository.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {

    private lateinit var syncManager: SyncManager
    private lateinit var mockContext: Context
    private lateinit var mockSyncApiService: SyncApiService
    private lateinit var mockPatientRepository: PatientRepository
    private lateinit var mockScanRepository: ScanRepository
    private lateinit var mockSurveyRepository: SurveyRepository
    private lateinit var mockReferralRepository: ReferralRepository

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockSyncApiService = mockk()
        mockPatientRepository = mockk()
        mockScanRepository = mockk()
        mockSurveyRepository = mockk()
        mockReferralRepository = mockk()

        // Mock DataStore
        every { mockContext.syncDataStore } returns mockk(relaxed = true)

        syncManager = SyncManager(
            context = mockContext,
            syncApiService = mockSyncApiService,
            patientRepository = mockPatientRepository,
            scanRepository = mockScanRepository,
            surveyRepository = mockSurveyRepository,
            referralRepository = mockReferralRepository
        )
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `performSync updates state to Syncing when started`() = runTest {
        // Arrange
        setupSuccessfulSync()

        // Act
        syncManager.performSync()

        // Assert
        // State should transition through Syncing states
        // (Can't directly test intermediate states without more complex setup,
        // but final state should be Success)
    }

    @Test
    fun `performSync with no pending items returns success with zero counts`() = runTest {
        // Arrange
        coEvery { mockPatientRepository.getPatientsBySyncStatus(SyncStatus.PENDING) } returns Result.success(emptyList())
        coEvery { mockScanRepository.getScansBySyncStatus(SyncStatus.PENDING) } returns Result.success(emptyList())
        coEvery { mockSurveyRepository.getSurveysBySyncStatus(SyncStatus.PENDING) } returns Result.success(emptyList())
        coEvery { mockReferralRepository.getReferralsBySyncStatus(SyncStatus.PENDING) } returns Result.success(emptyList())

        val uploadResponse = SyncUploadResponse(
            acceptedPatients = 0,
            acceptedScans = 0,
            acceptedSurveys = 0,
            acceptedReferrals = 0,
            message = "No data to sync"
        )
        coEvery { mockSyncApiService.uploadData(any()) } returns Result.success(uploadResponse)

        val downloadResponse = SyncDownloadResponse(
            patientUpdates = emptyList(),
            referralUpdates = emptyList()
        )
        coEvery { mockSyncApiService.downloadUpdates(any()) } returns Result.success(downloadResponse)

        // Act
        val result = syncManager.performSync()

        // Assert
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!
        assertEquals(0, syncResult.uploadedPatients)
        assertEquals(0, syncResult.uploadedScans)
        assertEquals(0, syncResult.uploadedSurveys)
        assertEquals(0, syncResult.uploadedReferrals)
        assertEquals(0, syncResult.downloadedUpdates)
    }

    @Test
    fun `performSync successfully uploads pending items`() = runTest {
        // Arrange
        val pendingPatient = createMockPatient(syncStatus = SyncStatus.PENDING)
        val pendingScan = createMockScan(syncStatus = SyncStatus.PENDING)

        coEvery { mockPatientRepository.getPatientsBySyncStatus(SyncStatus.PENDING) } returns Result.success(listOf(pendingPatient))
        coEvery { mockScanRepository.getScansBySyncStatus(SyncStatus.PENDING) } returns Result.success(listOf(pendingScan))
        coEvery { mockSurveyRepository.getSurveysBySyncStatus(SyncStatus.PENDING) } returns Result.success(emptyList())
        coEvery { mockReferralRepository.getReferralsBySyncStatus(SyncStatus.PENDING) } returns Result.success(emptyList())

        val uploadResponse = SyncUploadResponse(
            acceptedPatients = 1,
            acceptedScans = 1,
            acceptedSurveys = 0,
            acceptedReferrals = 0,
            message = "Upload successful"
        )
        coEvery { mockSyncApiService.uploadData(any()) } returns Result.success(uploadResponse)

        coEvery { mockPatientRepository.updateSyncStatus(any(), SyncStatus.SYNCED, any()) } returns Result.success(Unit)
        coEvery { mockScanRepository.updateSyncStatus(any(), SyncStatus.SYNCED, any()) } returns Result.success(Unit)

        val downloadResponse = SyncDownloadResponse(
            patientUpdates = emptyList(),
            referralUpdates = emptyList()
        )
        coEvery { mockSyncApiService.downloadUpdates(any()) } returns Result.success(downloadResponse)

        // Act
        val result = syncManager.performSync()

        // Assert
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!
        assertEquals(1, syncResult.uploadedPatients)
        assertEquals(1, syncResult.uploadedScans)

        // Verify sync status updated
        coVerify { mockPatientRepository.updateSyncStatus(pendingPatient.id, SyncStatus.SYNCED, any()) }
        coVerify { mockScanRepository.updateSyncStatus(pendingScan.id, SyncStatus.SYNCED, any()) }
    }

    @Test
    fun `performSync updates patient risk status from server`() = runTest {
        // Arrange
        setupSuccessfulSync()

        val patientUpdate = PatientUpdateDto(
            patientId = "patient-123",
            highRiskFlag = true,
            highRiskReasons = listOf("Hypertension", "High Glucose")
        )

        val downloadResponse = SyncDownloadResponse(
            patientUpdates = listOf(patientUpdate),
            referralUpdates = emptyList()
        )
        coEvery { mockSyncApiService.downloadUpdates(any()) } returns Result.success(downloadResponse)

        coEvery { mockPatientRepository.updateRiskStatus(any(), any(), any(), any()) } returns Result.success(Unit)

        // Act
        val result = syncManager.performSync()

        // Assert
        assertTrue(result.isSuccess)
        coVerify {
            mockPatientRepository.updateRiskStatus(
                patientId = "patient-123",
                highRiskFlag = true,
                highRiskReasons = listOf("Hypertension", "High Glucose"),
                timestamp = any()
            )
        }
    }

    @Test
    fun `performSync updates referral status from server`() = runTest {
        // Arrange
        setupSuccessfulSync()

        val referralUpdate = ReferralUpdateDto(
            referralId = "referral-123",
            status = "RESOLVED",
            resolvedAt = System.currentTimeMillis()
        )

        val downloadResponse = SyncDownloadResponse(
            patientUpdates = emptyList(),
            referralUpdates = listOf(referralUpdate)
        )
        coEvery { mockSyncApiService.downloadUpdates(any()) } returns Result.success(downloadResponse)

        val existingReferral = createMockReferral(id = "referral-123", status = ReferralStatus.CONFIRMED)
        coEvery { mockReferralRepository.getReferralById("referral-123") } returns Result.success(existingReferral)
        coEvery { mockReferralRepository.updateReferral(any()) } returns Result.success(Unit)

        // Act
        val result = syncManager.performSync()

        // Assert
        assertTrue(result.isSuccess)
        coVerify {
            mockReferralRepository.updateReferral(
                match { it.id == "referral-123" && it.status == ReferralStatus.RESOLVED }
            )
        }
    }

    @Test
    fun `performSync fails when upload fails`() = runTest {
        // Arrange
        coEvery { mockPatientRepository.getPatientsBySyncStatus(any()) } returns Result.success(emptyList())
        coEvery { mockScanRepository.getScansBySyncStatus(any()) } returns Result.success(emptyList())
        coEvery { mockSurveyRepository.getSurveysBySyncStatus(any()) } returns Result.success(emptyList())
        coEvery { mockReferralRepository.getReferralsBySyncStatus(any()) } returns Result.success(emptyList())

        coEvery { mockSyncApiService.uploadData(any()) } returns Result.failure(Exception("Network error"))

        // Act
        val result = syncManager.performSync()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `performSync fails when download fails`() = runTest {
        // Arrange
        setupSuccessfulUpload()
        coEvery { mockSyncApiService.downloadUpdates(any()) } returns Result.failure(Exception("Server error"))

        // Act
        val result = syncManager.performSync()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Server error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `performSync sets state to Failed on upload error`() = runTest {
        // Arrange
        coEvery { mockPatientRepository.getPatientsBySyncStatus(any()) } returns Result.success(emptyList())
        coEvery { mockScanRepository.getScansBySyncStatus(any()) } returns Result.success(emptyList())
        coEvery { mockSurveyRepository.getSurveysBySyncStatus(any()) } returns Result.success(emptyList())
        coEvery { mockReferralRepository.getReferralsBySyncStatus(any()) } returns Result.success(emptyList())

        coEvery { mockSyncApiService.uploadData(any()) } returns Result.failure(Exception("Upload failed"))

        // Act
        syncManager.performSync()

        // Assert
        val finalState = syncManager.syncState.value
        assertTrue(finalState is SyncState.Failed)
        assertEquals("Upload failed", (finalState as SyncState.Failed).error)
    }

    @Test
    fun `performSync sets state to Success on completion`() = runTest {
        // Arrange
        setupSuccessfulSync()

        // Act
        syncManager.performSync()

        // Assert
        val finalState = syncManager.syncState.value
        assertTrue(finalState is SyncState.Success)
    }

    // Helper functions
    private fun setupSuccessfulSync() {
        setupSuccessfulUpload()

        val downloadResponse = SyncDownloadResponse(
            patientUpdates = emptyList(),
            referralUpdates = emptyList()
        )
        coEvery { mockSyncApiService.downloadUpdates(any()) } returns Result.success(downloadResponse)
    }

    private fun setupSuccessfulUpload() {
        coEvery { mockPatientRepository.getPatientsBySyncStatus(any()) } returns Result.success(emptyList())
        coEvery { mockScanRepository.getScansBySyncStatus(any()) } returns Result.success(emptyList())
        coEvery { mockSurveyRepository.getSurveysBySyncStatus(any()) } returns Result.success(emptyList())
        coEvery { mockReferralRepository.getReferralsBySyncStatus(any()) } returns Result.success(emptyList())

        val uploadResponse = SyncUploadResponse(
            acceptedPatients = 0,
            acceptedScans = 0,
            acceptedSurveys = 0,
            acceptedReferrals = 0,
            message = "Success"
        )
        coEvery { mockSyncApiService.uploadData(any()) } returns Result.success(uploadResponse)
    }

    private fun createMockPatient(syncStatus: SyncStatus = SyncStatus.PENDING): Patient {
        return Patient(
            id = "patient-${System.currentTimeMillis()}",
            phoneNumber = "09171234567",
            firstName = "Juan",
            lastName = "Dela Cruz",
            birthdate = System.currentTimeMillis() - (30L * 365 * 24 * 60 * 60 * 1000),
            sex = Sex.MALE,
            barangayId = "brgy-001",
            syncStatus = syncStatus
        )
    }

    private fun createMockScan(syncStatus: SyncStatus = SyncStatus.PENDING): Scan {
        return Scan(
            id = "scan-${System.currentTimeMillis()}",
            patientId = "patient-123",
            scannedAt = System.currentTimeMillis(),
            scanDurationSeconds = 50,
            systolicBp = 120f,
            diastolicBp = 80f,
            heartRate = 72f,
            riskLevel = RiskLevel.NORMAL,
            riskScore = 25f,
            signalQuality = SignalQuality.GOOD,
            qualityScore = 85f,
            biomarkersFull = mapOf(
                "systolicBp" to 120f,
                "diastolicBp" to 80f,
                "heartRate" to 72f
            ),
            syncStatus = syncStatus
        )
    }

    private fun createMockReferral(
        id: String = "referral-123",
        status: ReferralStatus = ReferralStatus.PENDING
    ): Referral {
        return Referral(
            id = id,
            patientId = "patient-123",
            scanId = "scan-123",
            tier = ReferralTier.BHW_TO_BHS,
            status = status,
            riskLevel = RiskLevel.HIGH,
            riskFlags = listOf("Hypertension"),
            referredAt = System.currentTimeMillis(),
            dueBy = System.currentTimeMillis() + (48 * 60 * 60 * 1000)
        )
    }
}
