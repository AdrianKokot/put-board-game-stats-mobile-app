package com.example.boardgamestats.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BoardGameExpansion(
    @PrimaryKey
    override val id: Int,
    val boardGameId: Int,
    override val name: String,
    override val publishYear: Int,
    override val thumbnail: String? = null,
    override val description: String? = null,
    override val inCollection: Boolean = false,
    override val image: String? = null,
    override val hasDetails: Boolean = false,
) : BoardGameThingItem