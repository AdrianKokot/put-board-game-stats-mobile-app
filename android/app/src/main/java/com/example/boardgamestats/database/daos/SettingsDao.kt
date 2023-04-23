package com.example.boardgamestats.database.daos

import androidx.room.Dao
import androidx.room.Query
import com.example.boardgamestats.models.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM UserSettings WHERE id = 1")
    fun getUserSettings(): Flow<UserSettings>

    @Query("UPDATE UserSettings SET isSyncEnabled = :isSyncEnabled WHERE id = 1")
    fun updateUserSettings(isSyncEnabled: Boolean)

    @Query("INSERT OR IGNORE INTO UserSettings(id, isSyncEnabled) VALUES (1, 0)")
    fun verifySettings()
}