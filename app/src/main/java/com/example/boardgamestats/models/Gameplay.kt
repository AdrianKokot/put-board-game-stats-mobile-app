package com.example.boardgamestats.models

import androidx.room.*

@Entity
data class Gameplay(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val boardGameId: Int,
    val date: Long,
    val notes: String = "",
    val playtime: Long? = null
)

@Entity(
    primaryKeys = ["gameplayId", "playerName"],
    foreignKeys = [ForeignKey(
        entity = Gameplay::class,
        parentColumns = ["id"],
        childColumns = ["gameplayId"],
        onDelete = ForeignKey.CASCADE
    ),
        ForeignKey(
            entity = Player::class,
            parentColumns = ["name"],
            childColumns = ["playerName"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class PlayerWithScore(
    val gameplayId: Int,
    val playerName: String,
    val score: Int
)

data class PlayerWithScoreDto(
    val playerName: String,
    val score: Int
)

data class GameplayWithPlayers(
    @Embedded val gameplay: Gameplay,
    @Relation(
        parentColumn = "id",
        entityColumn = "gameplayId"
    )
    val playerResults: List<PlayerWithScore>
)