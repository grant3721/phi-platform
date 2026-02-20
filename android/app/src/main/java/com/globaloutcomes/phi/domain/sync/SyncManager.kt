package com.globaloutcomes.phi.domain.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.globaloutcomes.phi.data.remote.SyncApiService
import com.globaloutcomes.phi.data.remote.dto.*
import com.globaloutcomes.phi.domain.model.SyncStatus
import com.globaloutcomes.phi.domain.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.syncDataStore by preferencesDataStore(name = "sync")

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncApiService: SyncApiService,
    private val patientRepository: PatientRepository,
    private val scanRepository: ScanRepository,
    private val surveyRepository: SurveyRepository,
    private val referralRepository: ReferralRepository
) {
    private val LAST_SYNC_KEY = longPreferencesKey("last_sync_timestamp")

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    /**
     * Performs full sync: upload then download
     */
    suspend fun performSync(): Result<SyncResult> {
        return try {
            _syncState.value = SyncState.Syncing(progress = 0f, message = "Starting sync...")

            // Step 1: Upload local data
            _syncState.value = SyncState.Syncing(progress = 0.1f, message = "Uploading data...")
            val uploadResult = uploadLocalData()
            if (uploadResult.isFailure) {
                _syncState.value = SyncState.Failed(uploadResult.exceptionOrNull()?.message ?: "Upload failed")
                return Result.failure(uploadResult.exceptionOrNull() ?: Exception("Upload failed"))
            }

            // Step 2: Download updates
            _syncState.value = SyncState.Syncing(progress = 0.6f, message = "Downloading updates...")
            val downloadResult = downloadUpdates()
            if (downloadResult.isFailure) {
                _syncState.value = SyncState.Failed(downloadResult.exceptionOrNull()?.message ?: "Download failed")
                return Result.failure(downloadResult.exceptionOrNull() ?: Exception("Download failed"))
            }

            // Step 3: Update last sync timestamp
            updateLastSyncTimestamp(System.currentTimeMillis())

            val result = SyncResult(
                uploadedPatients = uploadResult.getOrNull()?.acceptedPatients ?: 0,
                uploadedScans = uploadResult.getOrNull()?.acceptedScans ?: 0,
                uploadedSurveys = uploadResult.getOrNull()?.acceptedSurveys ?: 0,
                uploadedReferrals = uploadResult.getOrNull()?.acceptedReferrals ?: 0,
                downloadedUpdates = downloadResult.getOrNull()?.patientUpdates?.size ?: 0
            )

            _syncState.value = SyncState.Success(result)
            Result.success(result)
        } catch (e: Exception) {
            _syncState.value = SyncState.Failed(e.message ?: "Sync failed")
            Result.failure(e)
        }
    }

    private suspend fun uploadLocalData(): Result<SyncUploadResponse> {
        // Get all PENDING items from local database
        val patients = patientRepository.getPatientsBySyncStatus(SyncStatus.PENDING).getOrNull() ?: emptyList()
        val scans = scanRepository.getScansBySyncStatus(SyncStatus.PENDING).getOrNull() ?: emptyList()
        val surveys = surveyRepository.getSurveysBySyncStatus(SyncStatus.PENDING).getOrNull() ?: emptyList()
        val referrals = referralRepository.getReferralsBySyncStatus(SyncStatus.PENDING).getOrNull() ?: emptyList()

        // Map to DTOs
        val patientDtos = patients.map { patient ->
            PatientDto(
                id = patient.id,
                phoneNumber = patient.phoneNumber,
                firstName = patient.firstName,
                lastName = patient.lastName,
                birthdate = patient.birthdate,
                sex = patient.sex.name,
                barangayId = patient.barangayId,
                philHealthNumber = patient.philHealthNumber,
                philSysNumber = patient.philSysNumber,
                isPregnant = patient.isPregnant,
                gestationalAgeWeeks = patient.gestationalAgeWeeks,
                messengerOptIn = patient.messengerOptIn
            )
        }

        val scanDtos = scans.map { scan ->
            ScanDto(
                id = scan.id,
                patientId = scan.patientId,
                scannedAt = scan.scannedAt,
                scanDurationSeconds = scan.scanDurationSeconds,
                systolicBp = scan.systolicBp,
                diastolicBp = scan.diastolicBp,
                heartRate = scan.heartRate,
                spo2 = scan.spo2,
                bloodGlucose = scan.bloodGlucose,
                riskLevel = scan.riskLevel.name,
                riskScore = scan.riskScore,
                highRiskFlags = scan.highRiskFlags,
                biomarkersFull = scan.biomarkersFull
            )
        }

        val surveyDtos = surveys.map { survey ->
            SurveyDto(
                id = survey.id,
                patientId = survey.patientId,
                scanId = survey.scanId,
                surveyType = survey.surveyType.name,
                completedAt = survey.completedAt,
                responses = survey.responses.mapValues { it.value.toString() }
            )
        }

        val referralDtos = referrals.map { referral ->
            ReferralDto(
                id = referral.id,
                patientId = referral.patientId,
                scanId = referral.scanId,
                tier = referral.tier.name,
                status = referral.status.name,
                referredAt = referral.referredAt,
                dueBy = referral.dueBy,
                riskLevel = referral.riskLevel.name,
                riskFlags = referral.riskFlags
            )
        }

        val request = SyncUploadRequest(
            patients = patientDtos,
            scans = scanDtos,
            surveys = surveyDtos,
            referrals = referralDtos
        )

        // Upload to server
        val result = syncApiService.uploadData(request)

        // Update local sync status to SYNCED for successful uploads
        if (result.isSuccess) {
            val currentTime = System.currentTimeMillis()
            patients.forEach { patient ->
                patientRepository.updateSyncStatus(patient.id, SyncStatus.SYNCED, currentTime)
            }
            scans.forEach { scan ->
                scanRepository.updateSyncStatus(scan.id, SyncStatus.SYNCED, currentTime)
            }
            surveys.forEach { survey ->
                surveyRepository.updateSyncStatus(survey.id, SyncStatus.SYNCED, currentTime)
            }
            referrals.forEach { referral ->
                referralRepository.updateSyncStatus(referral.id, SyncStatus.SYNCED, currentTime)
            }
        }

        return result
    }

    private suspend fun downloadUpdates(): Result<SyncDownloadResponse> {
        val lastSync = getLastSyncTimestamp()
        val result = syncApiService.downloadUpdates(lastSync)

        // Apply downloaded updates to local database
        if (result.isSuccess) {
            val response = result.getOrNull()!!

            // Update patient risk statuses
            response.patientUpdates.forEach { update ->
                patientRepository.updateRiskStatus(
                    patientId = update.patientId,
                    highRiskFlag = update.highRiskFlag,
                    highRiskReasons = update.highRiskReasons,
                    timestamp = System.currentTimeMillis()
                )
            }

            // Update referral statuses
            response.referralUpdates.forEach { update ->
                val referral = referralRepository.getReferralById(update.referralId).getOrNull()
                if (referral != null) {
                    val updatedReferral = referral.copy(
                        status = com.globaloutcomes.phi.domain.model.ReferralStatus.valueOf(update.status),
                        resolvedAt = update.resolvedAt
                    )
                    referralRepository.updateReferral(updatedReferral)
                }
            }
        }

        return result
    }

    private suspend fun getLastSyncTimestamp(): Long {
        return context.syncDataStore.data.map { preferences ->
            preferences[LAST_SYNC_KEY] ?: 0L
        }.first()
    }

    private suspend fun updateLastSyncTimestamp(timestamp: Long) {
        context.syncDataStore.edit { preferences ->
            preferences[LAST_SYNC_KEY] = timestamp
        }
    }
}

/**
 * Sync state
 */
sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val progress: Float, val message: String) : SyncState()
    data class Success(val result: SyncResult) : SyncState()
    data class Failed(val error: String) : SyncState()
}

/**
 * Sync result
 */
data class SyncResult(
    val uploadedPatients: Int,
    val uploadedScans: Int,
    val uploadedSurveys: Int,
    val uploadedReferrals: Int,
    val downloadedUpdates: Int
)
