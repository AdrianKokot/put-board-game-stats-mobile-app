package com.example.boardgamestats.database.daos

import androidx.room.*
import com.example.boardgamestats.models.*
import kotlinx.serialization.Serializable

@Serializable
data class SyncData(
    val lastSync: Long,
    val boardGames: SyncedCollection,
    val plays: SyncedPlays,
    val currentSync: Long
)

@Serializable
data class AddedToCollectionBoardGame(
    val id: Int,
    val updatedAt: Long,
    val isExpansion: Boolean,
    val thumbnail: String? = null,
    val publishYear: Int,
    val name: String
)

@Serializable
data class RemovedFromCollectionBoardGame(
    val id: Int,
    val updatedAt: Long
)

@Serializable
data class SyncedCollection(
    val addedToCollection: List<AddedToCollectionBoardGame>,
    val removedFromCollection: List<RemovedFromCollectionBoardGame>
)

@Serializable
data class SyncedDeletedGameplay(
    val id: Int,
    val deletedAt: Long?
)

@Serializable
data class SyncedGameplay(
    val id: Int,
    val boardGameId: Int,
    val date: Long,
    val playtime: Long?,
    val createdAt: Long,
    val notes: String,
    val players: List<SyncedGameplayPlayer>
)

@Serializable
data class SyncedGameplayPlayer(
    val name: String,
    val score: Int
)

@Serializable
data class SyncedPlays(
    val added: List<SyncedGameplay>,
    val deleted: List<SyncedDeletedGameplay>
)

@Entity
data class SyncInfo(
    @PrimaryKey
    val id: Int,
    val lastSync: Long = 0
)

@Dao
interface SyncDao {
    @Query("SELECT * FROM SyncInfo WHERE id = 1")
    fun getLastSync(): SyncInfo

    @Query("INSERT OR IGNORE INTO SyncInfo(id, lastSync) VALUES (1, 0)")
    fun verifySyncInfo()

    @Query("SELECT * FROM boardgame WHERE updatedAt >= (SELECT lastSync FROM SyncInfo WHERE id = 1)")
    fun getUpdatedBoardGames(): List<BoardGame>

    @Transaction
    @Query("SELECT * FROM gameplay WHERE createdAt >= (SELECT lastSync FROM SyncInfo WHERE id = 1)")
    fun getNewPlays(): List<GameplayWithPlayers>

    @Transaction
    @Query("SELECT * FROM gameplay WHERE deletedAt >= (SELECT lastSync FROM SyncInfo WHERE id = 1)")
    fun getDeletedPlays(): List<Gameplay>

    @Transaction
    @Delete
    fun delete(gameplay: Gameplay)

    fun getSyncData(): SyncData {
        val lastSync = getLastSync()
        val updatedBoardGames = getUpdatedBoardGames()

        return SyncData(
            lastSync = lastSync.lastSync,
            currentSync = System.currentTimeMillis(),
            boardGames = SyncedCollection(
                addedToCollection = updatedBoardGames.filter { it.inCollection }.map {
                    AddedToCollectionBoardGame(
                        id = it.id,
                        updatedAt = it.updatedAt,
                        isExpansion = it.isExpansion,
                        thumbnail = it.thumbnail,
                        publishYear = it.publishYear,
                        name = it.name
                    )
                },
                removedFromCollection = updatedBoardGames.filter { !it.inCollection }.map {
                    RemovedFromCollectionBoardGame(
                        id = it.id,
                        updatedAt = it.updatedAt
                    )
                }
            ),
            plays = SyncedPlays(
                added = getNewPlays().map { gameplay ->
                    SyncedGameplay(
                        id = gameplay.gameplay.id,
                        boardGameId = gameplay.gameplay.boardGameId,
                        date = gameplay.gameplay.date,
                        playtime = gameplay.gameplay.playtime,
                        createdAt = gameplay.gameplay.createdAt,
                        notes = gameplay.gameplay.notes,
                        players = gameplay.playerResults.map { SyncedGameplayPlayer(it.playerName, it.score) }
                    )
                },
                deleted = getDeletedPlays().map { SyncedDeletedGameplay(it.id, it.deletedAt) }
            )
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBoardGame(boardGame: BoardGame)

    @Query("UPDATE boardgame SET inCollection = :inCollection, updatedAt = :updatedAt WHERE id = :id")
    fun updateBoardGame(id: Int, inCollection: Boolean, updatedAt: Long)

    @Transaction
    fun syncCollectionItem(boardGame: AddedToCollectionBoardGame) {
        insertBoardGame(
            BoardGame(
                id = boardGame.id,
                publishYear = boardGame.publishYear,
                name = boardGame.name,
                thumbnail = boardGame.thumbnail,
                inCollection = true,
                updatedAt = boardGame.updatedAt,
                isExpansion = boardGame.isExpansion
            )
        )
        updateBoardGame(boardGame.id, true, boardGame.updatedAt)
    }

    @Transaction
    fun syncCollectionItem(boardGame: RemovedFromCollectionBoardGame) {
        updateBoardGame(boardGame.id, false, boardGame.updatedAt)
    }

    @Query("SELECT count(*) == 1 FROM boardgame WHERE id = :id")
    fun boardGameExists(id: Int): Boolean

    @Query("DELETE FROM gameplay WHERE id = :id")
    fun deleteGameplay(id: Int)

    fun syncGameplay(gameplay: SyncedDeletedGameplay) {
        deleteGameplay(gameplay.id)
    }

    @Transaction
    fun syncGameplay(syncedGameplay: SyncedGameplay) {
        val insertedGameplayId = Gameplay(
            id = syncedGameplay.id,
            boardGameId = syncedGameplay.boardGameId,
            date = syncedGameplay.date,
            playtime = syncedGameplay.playtime,
            createdAt = syncedGameplay.createdAt,
            notes = syncedGameplay.notes
        ).let {
            insertGameplay(it).toInt()
        }

        if (insertedGameplayId == -1) {
            return
        }

        insertPlayers(*syncedGameplay.players.map { Player(name = it.name) }.toTypedArray())
        insertPlayerWithScore(*syncedGameplay.players.map {
            PlayerWithScore(
                gameplayId = insertedGameplayId,
                score = it.score,
                playerName = it.name
            )
        }.toTypedArray())
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPlayers(vararg players: Player)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPlayerWithScore(vararg player: PlayerWithScore)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertGameplay(gameplay: Gameplay): Long

    @Query("UPDATE SyncInfo SET lastSync = :lastSync WHERE id = 1")
    fun setLastSync(lastSync: Long)
}