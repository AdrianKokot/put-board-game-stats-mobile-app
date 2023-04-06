package com.example.boardgamestats.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Player(
    @PrimaryKey
    val name: String
)