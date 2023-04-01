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

    @Query("SELECT * FROM boardgame WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): BoardGame

    @Query("SELECT * FROM boardgame WHERE id = :id LIMIT 1")
    fun get(id: Int): Flow<BoardGame>

    @Insert
    fun insertAll(vararg boardGames: BoardGame)

    @Delete
    fun delete(boardGame: BoardGame)
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
                                thumbnail = "https://cf.geekdo-images.com/x3zxjr-Vw5iU4yDPg70Jgw__thumb/img/o18rjEemoWaVru9Y2TyPwuIaRfE=/fit-in/200x150/filters:strip_icc()/pic3490053.jpg",
                                inCollection = true,
                                image = "https://cf.geekdo-images.com/x3zxjr-Vw5iU4yDPg70Jgw__original/img/FpyxH41Y6_ROoePAilPNEhXnzO8=/0x0/filters:format(jpeg)/pic3490053.jpg"
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
                Room.databaseBuilder(context, BoardGameDatabase::class.java, "board-game-stats")
                    .addCallback(seedDatabaseCallback(context))
                    .build()
                    .also { Instance = it }
            }
        }
    }
}