package com.example.boardgamestats.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.boardgamestats.models.BoardGame
import com.example.boardgamestats.models.BoardGameWithPlaysInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardGameDao {
    @Query("SELECT * FROM boardgame WHERE isExpansion = FALSE ORDER BY lower(trim(name)) ASC")
    fun getAllBoardGames(): Flow<List<BoardGame>>

    @Query(
        "SELECT boardgame.*, g.date as lastPlay, g.times as playsCount " +
                "FROM boardgame " +
                "LEFT JOIN (" +
                "   SELECT boardGameId, date, count(id) as times " +
                "   FROM gameplay WHERE deletedAt is null GROUP BY boardGameId ORDER BY date DESC" +
                ") as g ON g.boardGameId = boardgame.id " +
                "WHERE isExpansion = FALSE and inCollection = TRUE " +
                "ORDER BY lower(trim(name)) ASC"
    )
    @Transaction
    fun getBoardGamesCollectionWithPlayInformation(): Flow<List<BoardGameWithPlaysInfo>>

    @Query("SELECT * FROM boardgame WHERE isExpansion = TRUE ORDER BY lower(trim(name)) ASC")
    fun getAllExpansions(): Flow<List<BoardGame>>

    @Query("SELECT * FROM boardgame WHERE inCollection = TRUE and isExpansion = FALSE ORDER BY lower(trim(name)) ASC")
    fun getBoardGamesCollection(): Flow<List<BoardGame>>

    @Query("SELECT * FROM boardgame WHERE inCollection = TRUE and isExpansion = TRUE ORDER BY lower(trim(name)) ASC")
    fun getExpansionsCollection(): Flow<List<BoardGame>>

    @Query("SELECT * FROM boardgame WHERE id = :id LIMIT 1")
    fun get(id: Int): Flow<BoardGame>

    @Query("SELECT * FROM boardgame WHERE id = (SELECT boardGameId FROM Gameplay WHERE id = :gameplayId) LIMIT 1")
    fun getByGameplay(gameplayId: Int): Flow<BoardGame>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg boardGames: BoardGame)

    @Query("UPDATE boardgame SET inCollection = :inCollection, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCollection(id: Int, inCollection: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE boardgame SET thumbnail = :thumbnail, image = :image, description = :description, hasDetails = true, isExpansion = :isExpansion WHERE id = :id")
    fun updateDetails(id: Int, thumbnail: String, image: String, description: String, isExpansion: Boolean)
}