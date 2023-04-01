package com.example.boardgamestats.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BoardGame(
    @PrimaryKey
    val id: Int,
    val name: String,
    val publishYear: Int,
    var thumbnail: String? = null,
    var description: String? = null,
    val inCollection: Boolean = false,
    var image: String? = null
)