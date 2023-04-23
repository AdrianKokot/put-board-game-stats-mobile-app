package com.example.boardgamestats.vm

import android.app.Application
import android.content.ContentResolver
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.sync.SyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SyncState(
    val isSyncing: Boolean = false, val isSyncEnabled: Boolean = true
)

data class UserState(
    val isUserLoggedIn: Boolean = false, val photoUrl: String? = null, val idToken: String? = null
)

data class UserSettingsState(
    val isSyncEnabled: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _database = BoardGameDatabase.getDatabase(application)
    private val _settingsDao = _database.settingsDao()
    private val _syncState = MutableStateFlow(SyncState())
    private val _userState = MutableStateFlow(UserState())
    private val _userSettingsState = MutableStateFlow(UserSettingsState())

    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    val userState: StateFlow<UserState> = _userState.asStateFlow()
    val userSettingsState: StateFlow<UserSettingsState> = _userSettingsState.asStateFlow()

    init {
        viewModelScope.launch {
            _settingsDao.getUserSettings().filterNotNull().collect { settings ->
                updateUserSettingsSate(settings.isSyncEnabled)
            }
        }

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
        if (_syncState.value.isSyncEnabled) {
            SyncManager.forceSync()
            _syncState.update { currentState ->
                currentState.copy(isSyncing = true)
            }
        }
    }

    fun fetchUser(idToken: String?, photoUrl: String?) {
        _userState.update {
            it.copy(
                isUserLoggedIn = idToken != null, photoUrl = photoUrl, idToken = idToken
            )
        }
        _syncState.update {
            it.copy(isSyncEnabled = idToken != null && _userSettingsState.value.isSyncEnabled)
        }
    }

    private fun updateUserSettingsSate(isSyncEnabled: Boolean) {
        _userSettingsState.update { currentState ->
            currentState.copy(isSyncEnabled = isSyncEnabled)
        }

        _syncState.update { currentState ->
            currentState.copy(isSyncEnabled = _userState.value.isUserLoggedIn && isSyncEnabled)
        }
    }
}