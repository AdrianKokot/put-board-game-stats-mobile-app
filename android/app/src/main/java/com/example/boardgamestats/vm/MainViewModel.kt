package com.example.boardgamestats.vm

import android.content.ContentResolver
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.boardgamestats.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SyncState(
    val isSyncing: Boolean = false, val isSyncEnabled: Boolean = true
)

class MainViewModel : ViewModel() {
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
            Log.d(
                "MainViewModel",
                "Sync status changed [Mask: $it, isEmpty: ${ContentResolver.getCurrentSyncs().isEmpty()}]"
            )
            if (ContentResolver.getCurrentSyncs().isEmpty()) {
                _syncState.update { currentState ->
                    currentState.copy(isSyncing = false)
                }
            }
        }
    }

    fun startSync() {
        SyncManager.forceSync()
        _syncState.update { currentState ->
            currentState.copy(isSyncing = true)
        }
    }
}