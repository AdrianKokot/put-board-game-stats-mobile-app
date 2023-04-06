package com.example.boardgamestats.database

import android.content.Context
import android.util.Log.d
import androidx.room.*
import com.example.boardgamestats.models.BoardGame
import com.example.boardgamestats.models.BoardGameExpansion
import com.example.boardgamestats.models.Player
import kotlinx.coroutines.flow.Flow


@Entity
data class Gameplay(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val boardGameId: Int,
    val winnerName: String? = null,
    val date: Long,
    val notes: String = ""
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

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player")
    fun getAll(): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg players: Player)
}

@Dao
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
        val insertedGameplayId = insertGameplay(gameplay.copy(winnerName = playersDto.maxBy{it.score}.playerName)).toInt()

        insertPlayers(*playersDto.map{Player(name = it.playerName)}.toTypedArray())
        insertPlayerWithScore(*playersDto.map { PlayerWithScore(gameplayId = insertedGameplayId, score = it.score, playerName = it.playerName) }.toTypedArray())
    }

    @Insert
    fun insertPlayerWithScore(vararg player: PlayerWithScore)

    @Insert
    @Transaction
    fun insertGameplay(gameplay: Gameplay): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPlayers(vararg players: Player)


}

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

@Database(
    entities = [BoardGame::class, BoardGameExpansion::class, Player::class, Gameplay::class, PlayerWithScore::class],
    version = 1,
    exportSchema = false
)
abstract class BoardGameDatabase : RoomDatabase() {
    abstract fun boardGameDao(): BoardGameDao
    abstract fun boardGameExpansionDao(): BoardGameExpansionDao
    abstract fun playerDao(): PlayerDao
    abstract fun gameplayDao(): GameplayDao

    companion object {
        @Volatile
        private var Instance: BoardGameDatabase? = null

        fun getDatabase(context: Context): BoardGameDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, BoardGameDatabase::class.java, "board-game-stats-20")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}