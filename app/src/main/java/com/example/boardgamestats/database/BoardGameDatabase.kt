package com.example.boardgamestats.database

import android.content.Context
import androidx.room.*
import com.example.boardgamestats.models.BoardGame
import com.example.boardgamestats.models.BoardGameExpansion
import kotlinx.coroutines.flow.Flow

//
//@Entity
//data class Gameplay(
//    @PrimaryKey(autoGenerate = true)
//    val id: Int,
//    val boardGameId: String,
//    val winnerName: String,
//    val date: Instant,
//    val notes: String)
//
//@Entity
//data class Player(
//    @PrimaryKey
//    val name: String
//)
//
//@Entity(primaryKeys = ["gameplayId", "playerName"])
//data class GameplayPlayers(
//    val gameplayId: Int,
//    val playerName: String,
//    val score: Int
//)
//
//data class GameplayWithPlayers(
//    @Embedded val gameplay: Gameplay,
//    @Relation(
//        parentColumn = "gameplayId",
//        entityColumn = "playerName",
//        associateBy = Junction(GameplayPlayers::class)
//    )
//    val players: List<Player>
//)

@Dao
interface BoardGameExpansionDao {
    @Query("SELECT * FROM boardgameexpansion")
    fun getAll(): Flow<List<BoardGameExpansion>>

    @Query("SELECT * FROM boardgameexpansion WHERE inCollection = TRUE")
    fun getCollection(): Flow<List<BoardGameExpansion>>

    @Query("SELECT * FROM boardgameexpansion WHERE id = :id LIMIT 1")
    fun get(id: Int): Flow<BoardGameExpansion>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg boardGames: BoardGameExpansion)

    @Delete
    fun delete(boardGame: BoardGameExpansion)

    @Query("UPDATE boardgameexpansion SET inCollection = :inCollection WHERE id = :id")
    fun updateCollection(id: Int, inCollection: Boolean)

    @Query("UPDATE boardgameexpansion SET thumbnail = :thumbnail, image = :image, description = :description, hasDetails = true WHERE id = :id")
    fun updateBoardGameDetails(id: Int, thumbnail: String, image: String, description: String)
}

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

@Database(entities = [BoardGame::class, BoardGameExpansion::class], version = 1, exportSchema = false)
abstract class BoardGameDatabase : RoomDatabase() {
    abstract fun boardGameDao(): BoardGameDao
    abstract fun boardGameExpansionDao(): BoardGameExpansionDao

    companion object {
        @Volatile
        private var Instance: BoardGameDatabase? = null

        fun getDatabase(context: Context): BoardGameDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, BoardGameDatabase::class.java, "board-game-stats-3")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}