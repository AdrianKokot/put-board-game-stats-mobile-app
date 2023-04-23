package com.example.boardgamestats.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserSettings(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val isSyncEnabled: Boolean = false
)
