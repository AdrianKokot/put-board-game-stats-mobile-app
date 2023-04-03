package com.example.boardgamestats.models

import androidx.room.Entity
import androidx.room.PrimaryKey

interface BoardGameThingItem {
    val id: Int
    val name: String
    val publishYear: Int
    val thumbnail: String?
    val description: String?
    val inCollection: Boolean
    val image: String?
    val hasDetails: Boolean
}

@Entity
data class BoardGame(
    @PrimaryKey
    override val id: Int,
    override val name: String,
    override val publishYear: Int,
    override val thumbnail: String? = null,
    override val description: String? = null,
    override val inCollection: Boolean = false,
    override val image: String? = null,
    override val hasDetails: Boolean = false
) : BoardGameThingItem