package com.example.boardgamestats.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.boardgamestats.models.BoardGame
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executors

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

@Database(entities = [BoardGame::class], version = 1, exportSchema = false)
abstract class BoardGameDatabase : RoomDatabase() {
    abstract fun boardGameDao(): BoardGameDao

    companion object {
        private fun seedDatabaseCallback(context: Context): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    Executors.newSingleThreadExecutor().execute {
                        val dao = getDatabase(context).boardGameDao()

                        dao.insertAll(
                            BoardGame(
                                224517,
                                "Brass: Birmingham",
                                2018,
                                inCollection = true
                            ),
                            BoardGame(28720, "Brass: Lancashire", 2007)
                        )
                    }
                }
            }
        }

        @Volatile
        private var Instance: BoardGameDatabase? = null

        fun getDatabase(context: Context): BoardGameDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, BoardGameDatabase::class.java, "board-game-stats-2")
                    .addCallback(seedDatabaseCallback(context))
                    .build()
                    .also { Instance = it }
            }
        }
    }
}