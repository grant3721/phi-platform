package com.globaloutcomes.phi.presentation.sync

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaloutcomes.phi.domain.sync.NetworkConnectivityObserver
import com.globaloutcomes.phi.domain.sync.SyncManager
import com.globaloutcomes.phi.domain.sync.SyncState
import com.globaloutcomes.phi.domain.sync.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing sync state and operations
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncManager: SyncManager,
    private val networkObserver: NetworkConnectivityObserver
) : ViewModel() {

    /**
     * Sync state from SyncManager
     */
    val syncState: StateFlow<SyncState> = syncManager.syncState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncState.Idle
        )

    /**
     * Network connectivity state
     */
    val isOnline: StateFlow<Boolean> = networkObserver.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * Trigger immediate sync
     */
    fun syncNow() {
        viewModelScope.launch {
            syncManager.performSync()
        }
    }

    /**
     * Trigger sync via WorkManager (background)
     */
    fun triggerBackgroundSync() {
        SyncWorker.syncNow(context)
    }

    /**
     * Schedule periodic background sync
     */
    fun schedulePeriodicSync() {
        SyncWorker.schedule(context)
    }

    /**
     * Cancel periodic background sync
     */
    fun cancelPeriodicSync() {
        SyncWorker.cancel(context)
    }

    /**
     * Dismiss success or failed state (return to idle)
     */
    fun dismissSyncState() {
        // SyncManager will transition to Idle after success/failure is dismissed
        // This is handled automatically by the UI
    }
}
