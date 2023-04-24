package com.example.boardgamestats.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.boardgamestats.models.BoardGame
import com.example.boardgamestats.models.BoardGameStats
import com.example.boardgamestats.models.BoardGameWithPlaysInfo
import com.example.boardgamestats.models.PlayerStats
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

    @Query("SELECT * FROM boardgame WHERE hasDetails = true and name LIKE '%' || :text || '%'")
    fun searchBoardGames(text: String): List<BoardGame>

    @Query("SELECT * FROM (SELECT max(p.score) as highestScore, min(p.score) as lowestScore, round(avg(p.score), 0) as avgScore FROM Gameplay as g JOIN PlayerWithScore as p ON g.id = p.gameplayId WHERE g.boardGameId = :boardGameId) as g JOIN (SELECT count(id) as playsCount, max(date) as lastPlay, round(avg(playtime), 0) as avgPlaytime FROM Gameplay WHERE boardGameId = :boardGameId) as g2")
    fun getBoardGamePlaysStats(boardGameId: Int): Flow<BoardGameStats>

    @Query("SELECT p.playerName as name, max(p.score) as highestScore, count(*) as playCount, count(CASE WHEN p.score = g.winScore THEN 1 END) as winCount FROM \n" +
            "(SELECT g.*, max(p.score) as winScore FROM Gameplay as g JOIN PlayerWithScore as p ON g.id = p.gameplayId WHERE g.boardGameId = :boardGameId GROUP BY g.id) as g JOIN PlayerWithScore as p ON g.id = p.gameplayId GROUP BY p.playerName ORDER BY p.playerName ASC")
    fun getBoardGamePlayerStats(boardGameId: Int): Flow<List<PlayerStats>>
}

