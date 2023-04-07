package com.example.boardgamestats.database.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.boardgamestats.models.*
import kotlinx.coroutines.flow.Flow

interface GameplayDao {
    @Query("SELECT * FROM gameplay WHERE boardGameId = :boardGameId")
    fun getAllForGame(boardGameId: Int): Flow<List<GameplayWithPlayers>>

    @Transaction
    @Query("SELECT * FROM Gameplay WHERE id = :gameplayId")
    fun getGameplayWithPlayers(gameplayId: Int): GameplayWithPlayers

    @Transaction
    @Insert
    fun insertAll(vararg gameplay: Gameplay)

    @Query("SELECT * FROM player WHERE name = :name LIMIT 1")
    fun getPlayerByName(name: String): Player?


    @Transaction
    @Insert
    fun insert(gameplay: Gameplay, playersDto: List<PlayerWithScoreDto>) {
        val insertedGameplayId = insertGameplay(gameplay).toInt()

        insertPlayers(*playersDto.map { Player(name = it.playerName) }.toTypedArray())
        insertPlayerWithScore(*playersDto.map {
            PlayerWithScore(
                gameplayId = insertedGameplayId,
                score = it.score,
                playerName = it.playerName
            )
        }.toTypedArray())
    }

    @Insert
    fun insertPlayerWithScore(vararg player: PlayerWithScore)

    @Insert
    @Transaction
    fun insertGameplay(gameplay: Gameplay): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPlayers(vararg players: Player)


}