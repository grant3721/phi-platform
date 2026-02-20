# PHASE 9: Backend Sync Integration - COMPLETE

## Overview
Implemented complete backend sync infrastructure for offline-first data synchronization with Azure backend. The sync system uploads pending local data and downloads updates from the server.

## Requirements Implemented

### 1. API Configuration
- ✅ Base URL configuration (https://phi-platform-api.azurewebsites.net/api/)
- ✅ Endpoint definitions (auth, sync, config)
- ✅ Timeout constants (30 seconds for connect/read/write)

### 2. Data Transfer Objects (DTOs)
- ✅ SyncUploadRequest with patients, scans, surveys, referrals lists
- ✅ SyncUploadResponse with accepted counts and rejected lists
- ✅ SyncDownloadRequest with lastSyncTimestamp
- ✅ SyncDownloadResponse with config updates, patient updates, referral updates
- ✅ Individual DTOs: PatientDto, ScanDto, SurveyDto, ReferralDto
- ✅ Update DTOs: PatientUpdateDto, ReferralUpdateDto

### 3. HTTP Client Setup
- ✅ Ktor HttpClient with Android engine
- ✅ ContentNegotiation with JSON serialization
- ✅ Logging (LogLevel.INFO)
- ✅ HttpTimeout configuration
- ✅ Auth token management via DataStore
- ✅ DefaultRequest with automatic auth header injection

### 4. Sync API Service
- ✅ uploadData() - POST to sync/upload
- ✅ downloadUpdates() - POST to sync/download
- ✅ getConfigThresholds() - GET from config/thresholds
- ✅ All methods return Result<T> for error handling

### 5. Sync Manager
- ✅ performSync() - orchestrates upload then download
- ✅ uploadLocalData() - queries PENDING items, maps to DTOs, uploads, updates sync status
- ✅ downloadUpdates() - fetches updates, applies to local database
- ✅ SyncState sealed class (Idle, Syncing, Success, Failed) with progress tracking
- ✅ DataStore integration for last sync timestamp
- ✅ Handles patient risk updates from server
- ✅ Handles referral status updates from server

### 6. Repository Sync Methods
- ✅ PatientRepository: getPatientsBySyncStatus(), updateSyncStatus()
- ✅ ScanRepository: getScansBySyncStatus(), updateSyncStatus()
- ✅ SurveyRepository: getSurveysBySyncStatus(), updateSyncStatus()
- ✅ ReferralRepository: getReferralsBySyncStatus(), updateSyncStatus()

### 7. Dependency Injection
- ✅ RepositoryModule updated with Survey and Referral repository bindings
- ✅ All repositories injectable via Hilt

## Files Created

### Domain Layer
1. `domain/repository/SurveyRepository.kt` - Survey repository interface
2. `domain/repository/ReferralRepository.kt` - Referral repository interface
3. `domain/model/Referral.kt` - Referral domain model with enums

### Data Layer
1. `data/remote/ApiConfig.kt` - API configuration constants
2. `data/remote/dto/SyncDto.kt` - All sync DTOs
3. `data/remote/HttpClientProvider.kt` - Ktor HTTP client setup
4. `data/remote/SyncApiService.kt` - API service
5. `data/repository/SurveyRepositoryImpl.kt` - Survey repository implementation
6. `data/repository/ReferralRepositoryImpl.kt` - Referral repository implementation

### Sync Infrastructure
1. `domain/sync/SyncManager.kt` - Complete sync orchestration

### Dependency Injection
1. `di/RepositoryModule.kt` - Updated with Survey and Referral repositories

## Files Modified

### DAOs
1. `data/local/dao/ReferralDao.kt` - Added getBySyncStatus() and updateSyncStatus()

### Repositories
1. `domain/repository/PatientRepository.kt` - Added sync methods
2. `data/repository/PatientRepositoryImpl.kt` - Implemented sync methods
3. `domain/repository/ScanRepository.kt` - Added sync methods
4. `data/repository/ScanRepositoryImpl.kt` - Implemented sync methods

## Key Design Decisions

### 1. Offline-First Architecture
- All data operations go to local Room database first
- Sync runs in background to upload/download
- App remains fully functional offline
- SyncStatus enum tracks item sync state: PENDING → SYNCED

### 2. Sync Flow
```
Upload Flow:
1. Query all PENDING items from local database
2. Map domain models to DTOs
3. Batch upload to server
4. On success, update local items to SYNCED status

Download Flow:
1. Send lastSyncTimestamp to server
2. Server returns updates since last sync
3. Apply patient risk updates to local database
4. Apply referral status updates to local database
5. Update lastSyncTimestamp in DataStore
```

### 3. Conflict Resolution Strategy
- **Config/thresholds**: SERVER WINS (always use server config)
- **New scans/surveys**: CLIENT WINS (append-only, no conflicts)
- **Referral status**: LATEST TIMESTAMP WINS
- **Patient flags**: MERGE (union of flags)

### 4. Authentication
- JWT token stored in encrypted DataStore
- Automatically injected into all API requests via HttpClient DefaultRequest plugin
- Token management methods: saveAuthToken(), clearAuthToken()

### 5. Error Handling
- All API methods return Result<T>
- Network failures caught and returned as Result.failure()
- SyncState tracks overall sync progress and errors
- Progress tracking (0.0 to 1.0) for UI feedback

## Technical Implementation Details

### SyncManager State Flow
```kotlin
private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

// Progress updates:
// 0.0 - Starting
// 0.1 - Uploading data
// 0.6 - Downloading updates
// 1.0 - Success
```

### DTO Mapping Example
```kotlin
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
```

### DataStore Integration
```kotlin
private val LAST_SYNC_KEY = longPreferencesKey("last_sync_timestamp")

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
```

## Testing Strategy

### Unit Tests Required
- [ ] SyncManager.uploadLocalData() with mock repositories
- [ ] SyncManager.downloadUpdates() with mock repositories
- [ ] SyncManager.performSync() end-to-end flow
- [ ] Repository sync method implementations
- [ ] DTO mapping functions (toEntity, toDomain)

### Integration Tests Required
- [ ] Full sync cycle with real Room database
- [ ] Conflict resolution scenarios
- [ ] Network failure handling
- [ ] Auth token injection

### Manual Tests Required
- [ ] Sync with real backend (when available)
- [ ] Offline → online transition
- [ ] Multiple pending items sync
- [ ] Server-side updates applied correctly

## Next Steps (Phase 10+)

### Immediate Next Steps
1. **WorkManager Integration** - Implement periodic background sync (every 15 minutes)
2. **Sync UI** - Create sync status indicator and manual sync button
3. **Auth Flow** - Implement OTP login screen
4. **Error Recovery** - Retry logic with exponential backoff

### Future Enhancements
1. **Conflict Resolution UI** - Show user conflicts that need manual resolution
2. **Sync Queue Priority** - High-risk referrals sync first
3. **Partial Sync** - Sync only changed data
4. **Compression** - Compress large payloads
5. **Analytics** - Track sync success rates, failures, timing

## Business Logic Verification

### Sync Status Lifecycle
1. **New record created** → syncStatus = PENDING
2. **Upload successful** → syncStatus = SYNCED
3. **Modified after sync** → syncStatus = PENDING (needs re-sync)

### Data Consistency Rules
1. All PENDING items uploaded in batch
2. Server response indicates which items accepted/rejected
3. Only accepted items marked as SYNCED
4. Rejected items remain PENDING for retry

### Progress Tracking
- 0% - 10%: Uploading patients, scans, surveys, referrals
- 10% - 60%: Server processing
- 60% - 90%: Downloading updates
- 90% - 100%: Applying updates, updating timestamp

## Known Issues / Limitations

1. **No WorkManager yet** - Sync must be triggered manually, not periodic
2. **No retry logic** - Failed sync requires manual retry
3. **No sync queue** - All PENDING items uploaded at once (could be large)
4. **No compression** - Large scan batches may be slow over poor networks
5. **SurveyRepository TODO** - getByScanId and getByPatientId use Flow, need suspend versions
6. **ReferralRepository TODO** - getReferralsByPatientId needs implementation

## Dependencies

### New Dependencies (if not already in build.gradle)
```kotlin
// Ktor Client for Android
implementation("io.ktor:ktor-client-android:2.3.5")
implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
implementation("io.ktor:ktor-client-logging:2.3.5")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")

// DataStore for auth token and sync preferences
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Kotlin Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
```

## Summary

Phase 9 establishes a complete sync infrastructure following offline-first principles. All local data can be uploaded to the server, and server updates can be downloaded and applied. The architecture uses clean separation of concerns with repositories, use cases, and DTOs. Hilt provides dependency injection throughout. The sync system is ready for integration with UI and WorkManager for periodic background sync.

**Status: ✅ COMPLETE**

---

**Next Phase**: Phase 10 - WorkManager Integration & Sync UI
