package com.example.boardgamestats.database.daos

import androidx.room.*
import com.example.boardgamestats.models.BoardGame
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardGameDao {
    @Query("SELECT * FROM boardgame WHERE isExpansion = FALSE")
    fun getAllBoardGames(): Flow<List<BoardGame>>

    @Query("SELECT * FROM boardgame WHERE isExpansion = TRUE")
    fun getAllExpansions(): Flow<List<BoardGame>>

    @Query("SELECT * FROM boardgame WHERE inCollection = TRUE and isExpansion = FALSE")
    fun getBoardGamesCollection(): Flow<List<BoardGame>>

    @Query("SELECT * FROM boardgame WHERE inCollection = TRUE and isExpansion = TRUE")
    fun getExpansionsCollection(): Flow<List<BoardGame>>

    @Query("SELECT * FROM boardgame WHERE id = :id LIMIT 1")
    fun get(id: Int): Flow<BoardGame>

    @Query("SELECT * FROM boardgame WHERE id = (SELECT boardGameId FROM Gameplay WHERE id = :gameplayId) LIMIT 1")
    fun getByGameplay(gameplayId: Int): Flow<BoardGame>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg boardGames: BoardGame)

    @Delete
    fun delete(boardGame: BoardGame)

    @Query("UPDATE boardgame SET inCollection = :inCollection WHERE id = :id")
    fun updateCollection(id: Int, inCollection: Boolean)

    @Query("UPDATE boardgame SET thumbnail = :thumbnail, image = :image, description = :description, hasDetails = true, isExpansion = :isExpansion WHERE id = :id")
    fun updateDetails(id: Int, thumbnail: String, image: String, description: String, isExpansion: Boolean)
}