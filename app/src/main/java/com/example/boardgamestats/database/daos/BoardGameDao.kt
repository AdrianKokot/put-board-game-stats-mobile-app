package com.example.boardgamestats.database.daos

import androidx.room.*
import com.example.boardgamestats.models.BoardGame
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardGameDao {
    @Query("SELECT * FROM boardgame")
    fun getAll(): Flow<List<BoardGame>>

    @Query("SELECT * FROM boardgame WHERE inCollection = TRUE")
    fun getCollection(): Flow<List<BoardGame>>

    @Query("SELECT * FROM boardgame WHERE id = :id LIMIT 1")
    fun get(id: Int): Flow<BoardGame>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg boardGames: BoardGame)

    @Delete
    fun delete(boardGame: BoardGame)

    @Query("UPDATE boardgame SET inCollection = :inCollection WHERE id = :id")
    fun updateCollection(id: Int, inCollection: Boolean)

    @Query("UPDATE boardgame SET thumbnail = :thumbnail, image = :image, description = :description, hasDetails = true WHERE id = :id")
    fun updateBoardGameDetails(id: Int, thumbnail: String, image: String, description: String)
}