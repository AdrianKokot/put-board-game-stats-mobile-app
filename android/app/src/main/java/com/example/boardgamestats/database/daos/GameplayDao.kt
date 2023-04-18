package com.example.boardgamestats.database.daos

import androidx.room.*
import com.example.boardgamestats.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameplayDao {
    @Query("SELECT * FROM gameplay WHERE boardGameId = :boardGameId and deletedAt is null ORDER BY date DESC")
    fun getAllForGame(boardGameId: Int): Flow<List<GameplayWithPlayers>>

    @Transaction
    @Query("SELECT * FROM Gameplay WHERE id = :gameplayId")
    fun getGameplayWithPlayers(gameplayId: Int): Flow<GameplayWithPlayers>

    @Transaction
    @Query("SELECT * FROM Gameplay WHERE id = :gameplayId")
    fun getGameplayDetails(gameplayId: Int): Flow<GameplayDetails>

    @Transaction
    @Query("SELECT * FROM Gameplay WHERE id = :gameplayId")
    fun getGameplay(gameplayId: Int): Gameplay


    @Transaction
    @Insert
    fun insertAll(vararg gameplay: Gameplay)

    @Query("SELECT * FROM player WHERE name = :name LIMIT 1")
    fun getPlayerByName(name: String): Player?

    @Transaction
    @Query("UPDATE gameplay SET deletedAt = :deletedAt WHERE id = :gameplay")
    suspend fun delete(gameplay: Int, deletedAt: Long = System.currentTimeMillis())

    @Transaction
    @Insert
    suspend fun insert(gameplay: Gameplay, playersDto: List<PlayerWithScoreDto>) {
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
    suspend fun insertPlayerWithScore(vararg player: PlayerWithScore)

    @Insert
    @Transaction
    suspend fun insertGameplay(gameplay: Gameplay): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayers(vararg players: Player)

    @Transaction
    @Query("SELECT * FROM gameplay WHERE id in (SELECT id FROM gameplay WHERE deletedAt is null GROUP BY boardGameId) ORDER BY date DESC")
    fun getGameplayListWithUniqueGames(): Flow<List<GameplayWithGame>>
}