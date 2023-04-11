package com.example.boardgamestats.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation


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
    val hasDetails: Boolean = false,
    val isExpansion: Boolean = false
)


data class BoardGameWithPlaysInfo(
    @Embedded val boardGame: BoardGame,
    val playsCount: Int,
    val lastPlay: Long
)
