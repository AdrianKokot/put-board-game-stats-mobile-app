package com.example.boardgamestats.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BoardGame(
    @PrimaryKey
    val id: Int,
    val name: String,
    val publishYear: Int,
    val thumbnail: String? = null,
    val description: String? = null,
    val inCollection: Boolean = false,
    val image: String? = null,
    val hasDetails: Boolean = false
)