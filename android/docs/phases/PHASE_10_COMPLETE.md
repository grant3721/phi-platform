# PHASE 10: WorkManager Integration & Sync UI - COMPLETE

## Overview
Implemented periodic background sync with WorkManager and complete sync UI components. The app now automatically syncs every 15 minutes when online, provides visual feedback about sync status, and allows manual sync triggers.

## Requirements Implemented

### 1. Background Sync with WorkManager
- ✅ SyncWorker for periodic background sync (every 15 minutes)
- ✅ Network connectivity constraint (only sync when online)
- ✅ Exponential backoff retry strategy (30s initial delay)
- ✅ Max 3 retries before marking as failed
- ✅ One-time expedited sync for manual triggers
- ✅ Unique work policy (KEEP - don't replace existing periodic work)

### 2. Network Connectivity Monitoring
- ✅ NetworkConnectivityObserver using ConnectivityManager
- ✅ Flow-based reactive connectivity state
- ✅ Tracks active networks with internet capability
- ✅ Validates network has internet access (not just connected)
- ✅ Helper methods: isOnWifi(), isOnCellular()

### 3. Sync UI Components
- ✅ SyncStatusIndicator - Full-width banner with progress
- ✅ SyncStatusBadge - Compact icon button for app bar
- ✅ OfflineIndicator - Shows when device is offline
- ✅ Animated transitions (slide in/out, fade in/out)
- ✅ Progress tracking (0-100%)
- ✅ Auto-dismiss success banner after 3 seconds

### 4. Sync States Visualization
- ✅ **Idle** - Hidden, normal app operation
- ✅ **Syncing** - Progress indicator with message and percentage
- ✅ **Success** - Green banner with sync summary (auto-dismiss)
- ✅ **Failed** - Error banner with retry button

### 5. SyncViewModel
- ✅ Manages sync state from SyncManager
- ✅ Exposes network connectivity state
- ✅ Methods: syncNow(), triggerBackgroundSync(), schedulePeriodicSync(), cancelPeriodicSync()
- ✅ StateFlow for reactive UI updates

### 6. HomeScreen Integration
- ✅ SyncStatusBadge in TopAppBar
- ✅ OfflineIndicator banner when offline
- ✅ SyncStatusIndicator banner for sync progress/results
- ✅ Auto-schedule periodic sync on app launch
- ✅ Manual sync trigger via badge click

## Files Created

### Sync Infrastructure
1. `domain/sync/SyncWorker.kt` - WorkManager background worker
2. `domain/sync/NetworkConnectivityObserver.kt` - Network monitoring
3. `di/SyncModule.kt` - Hilt module for sync dependencies

### Presentation Layer
1. `presentation/components/SyncStatusIndicator.kt` - Sync UI components
   - SyncStatusIndicator (full banner)
   - SyncStatusBadge (compact icon)
   - OfflineIndicator (offline banner)
2. `presentation/sync/SyncViewModel.kt` - Sync state management ViewModel

## Files Modified

### Presentation
1. `presentation/home/HomeScreen.kt` - Integrated sync UI components

## Key Design Decisions

### 1. WorkManager Configuration
```kotlin
// Periodic work: every 15 minutes
val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    repeatInterval = 15,
    repeatIntervalTimeUnit = TimeUnit.MINUTES
)
.setConstraints(constraints)
.setBackoffCriteria(
    backoffPolicy = BackoffPolicy.EXPONENTIAL,
    backoffDelay = 30,
    timeUnit = TimeUnit.SECONDS
)
```

**Why 15 minutes?**
- Balance between data freshness and battery consumption
- Sufficient for operational needs (referral updates, patient flags)
- Within Android's minimum periodic work interval (15 minutes)

**Why exponential backoff?**
- Network failures often temporary (connectivity issues)
- Reduces server load from repeated failed requests
- 30s → 60s → 120s retry delays

### 2. Network Connectivity Strategy
```kotlin
val isOnline: Flow<Boolean> = callbackFlow {
    // Monitor all active networks
    // Only emit true if ANY network has validated internet
}
```

**Multiple network tracking:**
- Device can have WiFi + cellular simultaneously
- Track all networks, online if ANY has internet
- Use ConnectivityManager.NetworkCallback for real-time updates

**Validated internet check:**
- Not enough to check "connected" - must validate internet access
- Android validates by attempting to reach Google servers
- Prevents false positives (connected to WiFi with no internet)

### 3. Sync UI States

| State | Visual | Auto-Dismiss | User Action |
|-------|--------|--------------|-------------|
| **Idle** | Hidden | N/A | Can trigger sync via badge |
| **Syncing** | Blue banner + progress | No | None (in progress) |
| **Success** | Green banner + summary | 3 seconds | Can click to dismiss |
| **Failed** | Red banner + error | No | Retry button |

**Why auto-dismiss success?**
- Success is confirmation, not actionable
- Keeps UI clean
- 3 seconds enough to read summary

**Why no auto-dismiss failure?**
- User needs to know something went wrong
- Retry button provides action
- Persistent until addressed

### 4. Sync Summary Text Logic
```kotlin
private fun buildSyncSummary(result: SyncResult): String {
    val uploaded = result.uploadedPatients + result.uploadedScans +
                   result.uploadedSurveys + result.uploadedReferrals
    val downloaded = result.downloadedUpdates

    return when {
        uploaded > 0 && downloaded > 0 ->
            "Uploaded $uploaded items, downloaded $downloaded updates"
        uploaded > 0 ->
            "Uploaded $uploaded items"
        downloaded > 0 ->
            "Downloaded $downloaded updates"
        else ->
            "Everything up to date"
    }
}
```

**Human-readable feedback:**
- Users see what actually synced
- Differentiate upload vs download
- "Everything up to date" when no changes

### 5. Offline Indicator Strategy
```kotlin
AnimatedVisibility(
    visible = !isOnline,
    enter = slideInVertically() + fadeIn(),
    exit = slideOutVertically() + fadeOut()
)
```

**Persistent when offline:**
- Always visible when no network
- Explains why sync isn't happening
- Reassures that changes are saved locally

**Animated transitions:**
- Smooth slide in/out prevents jarring appearance
- Fade adds polish
- Material Design standard

## Technical Implementation Details

### SyncWorker Retry Logic
```kotlin
override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    val result = syncManager.performSync()

    if (result.isSuccess) {
        Result.success() // Reset retry count
    } else {
        val runAttemptCount = runAttemptCount
        if (runAttemptCount < MAX_RETRIES) {
            Result.retry() // Trigger exponential backoff
        } else {
            Result.failure() // Max retries exceeded
        }
    }
}
```

**Retry count resets on success:**
- Prevents accumulating failures from past sync attempts
- Each periodic sync starts fresh

**Max 3 retries:**
- 4 total attempts (initial + 3 retries)
- 30s → 60s → 120s delays
- Total: ~3.5 minutes before failure

### Network Callback Pattern
```kotlin
private val networks = mutableSetOf<Network>()

override fun onAvailable(network: Network) {
    networks.add(network)
    trySend(networks.isNotEmpty()) // Online if ANY network
}

override fun onLost(network: Network) {
    networks.remove(network)
    trySend(networks.isNotEmpty()) // Offline if NO networks
}
```

**Why track multiple networks?**
- Device can switch WiFi ↔ cellular seamlessly
- App stays "online" during network transitions
- More robust than single active network check

### HiltWorker Integration
```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(appContext, workerParams)
```

**Dependency injection in Worker:**
- `@HiltWorker` enables Hilt DI
- `@Assisted` for WorkManager parameters
- SyncManager injected automatically

**Requires:**
```gradle
implementation("androidx.hilt:hilt-work:1.1.0")
kapt("androidx.hilt:hilt-compiler:1.1.0")
```

### Compose State Management
```kotlin
val syncState: StateFlow<SyncState> = syncManager.syncState
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SyncState.Idle
    )
```

**WhileSubscribed(5000):**
- Keep Flow active 5 seconds after last subscriber
- Prevents rapid start/stop during config changes
- Balances responsiveness and resource usage

## Testing Strategy

### Unit Tests Required
- [ ] SyncWorker.doWork() with mock SyncManager
- [ ] SyncWorker retry logic (success after 1 retry, failure after 3 retries)
- [ ] NetworkConnectivityObserver with mock ConnectivityManager
- [ ] SyncViewModel state transitions

### Integration Tests Required
- [ ] WorkManager periodic sync triggers every 15 minutes
- [ ] Network constraint: sync only runs when online
- [ ] Exponential backoff timing verification
- [ ] Manual sync via syncNow() bypasses periodic schedule

### UI Tests Required
- [ ] SyncStatusIndicator appears during sync
- [ ] Success banner auto-dismisses after 3 seconds
- [ ] Failed banner shows retry button
- [ ] OfflineIndicator appears when network lost
- [ ] SyncStatusBadge click triggers sync

### Manual Tests Required
- [ ] Enable airplane mode → OfflineIndicator appears
- [ ] Disable airplane mode → auto-sync triggers
- [ ] Tap sync badge → immediate sync
- [ ] Background app for 20 minutes → 1 automatic sync occurred
- [ ] Kill app → sync continues in background
- [ ] Reboot device → periodic sync resumes

## Sync Behavior Matrix

| Scenario | Sync Triggered? | UI Feedback |
|----------|----------------|-------------|
| App launch (online) | Yes (immediate) | Progress banner |
| App launch (offline) | No | Offline indicator |
| Go online (from offline) | Yes (WorkManager) | Progress banner |
| Tap sync badge (online) | Yes (immediate) | Progress banner |
| Tap sync badge (offline) | No | Toast: "No network connection" |
| Periodic timer (online) | Yes (background) | Badge animates |
| Periodic timer (offline) | No (delayed) | Waits for network |
| App in background | Yes (WorkManager) | Notification (optional) |

## Performance Considerations

### Battery Impact
- **Periodic sync every 15 minutes:** Minimal (WorkManager optimized)
- **Network constraint:** Only runs when already online (no wakeup)
- **Exponential backoff:** Reduces repeated failures
- **Doze mode:** Android batches periodic work during deep sleep

### Network Usage
- **Upload size:** Varies by pending items (typically <100KB for 10 scans)
- **Download size:** Minimal (config + updates, typically <10KB)
- **Compression:** JSON (future: gzip)
- **WiFi preference:** WorkManager can prefer WiFi (not implemented yet)

### UI Responsiveness
- **Sync in background:** Doesn't block UI (coroutines + IO dispatcher)
- **StateFlow updates:** Reactive, no polling
- **Animated transitions:** Smooth (Compose animations)
- **Progress tracking:** Real-time updates

## Next Steps (Future Enhancements)

### Immediate Improvements
1. **Conflict resolution UI** - Show user when server data conflicts with local
2. **Sync history** - Log of past syncs (time, result, items synced)
3. **Sync settings** - User control over sync frequency
4. **WiFi-only mode** - Conserve cellular data

### Advanced Features
1. **Smart sync scheduling**
   - Sync immediately after high-risk scan
   - Sync before battery runs out
   - Sync during device charging
2. **Partial sync**
   - Sync only specific data types (e.g., referrals only)
   - Delta sync (only changed records)
3. **Push notifications**
   - Server notifies app of urgent updates
   - FCM integration
4. **Offline queue visualization**
   - Show pending items count
   - Preview what will sync next
5. **Compression**
   - Gzip JSON payloads
   - Reduce network usage by 70%

## Known Issues / Limitations

1. **No sync history** - Can't see past sync attempts
2. **No conflict UI** - Server conflicts silently resolved by strategy
3. **No retry queue** - Failed items not tracked separately
4. **No notification** - Background sync doesn't notify user
5. **No WiFi preference** - Syncs on cellular or WiFi equally
6. **No bandwidth throttling** - Could use excessive data on poor networks

## Dependencies

### New Dependencies (add to build.gradle)
```kotlin
// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.0")

// Hilt WorkManager integration
implementation("androidx.hilt:hilt-work:1.1.0")
kapt("androidx.hilt:hilt-compiler:1.1.0")
```

### Permissions Required (AndroidManifest.xml)
```xml
<!-- Network state monitoring -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Internet access (already required) -->
<uses-permission android:name="android.permission.INTERNET" />
```

## Summary

Phase 10 completes the sync infrastructure with automatic background sync, comprehensive UI feedback, and network awareness. The app now:

1. **Automatically syncs** every 15 minutes when online
2. **Shows real-time sync progress** with animated banners
3. **Handles network changes** gracefully (online/offline transitions)
4. **Retries failed syncs** with exponential backoff
5. **Allows manual sync** via app bar badge
6. **Persists across app restarts** (WorkManager survives process death)

The offline-first architecture is now complete:
- ✅ All data saved locally first
- ✅ Periodic background sync
- ✅ Network-aware sync triggering
- ✅ Visual feedback for sync state
- ✅ Retry logic for transient failures

**Status: ✅ COMPLETE**

---

**Next Phase Options:**
- **Phase 11:** Patient Detail Screen (view full patient history)
- **Phase 12:** Testing & Refinement (unit tests, integration tests)
- **Phase 13:** Advanced Features (conflict resolution UI, sync history, settings)
