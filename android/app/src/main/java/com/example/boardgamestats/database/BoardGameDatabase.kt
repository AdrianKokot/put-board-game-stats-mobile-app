package com.example.boardgamestats.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.boardgamestats.database.daos.*
import com.example.boardgamestats.models.BoardGame
import com.example.boardgamestats.models.Gameplay
import com.example.boardgamestats.models.Player
import com.example.boardgamestats.models.PlayerWithScore
import java.util.concurrent.Executors

@Database(
    entities = [BoardGame::class, Player::class, Gameplay::class, PlayerWithScore::class, SyncInfo::class],
    version = 1,
    exportSchema = false
)
abstract class BoardGameDatabase : RoomDatabase() {
    abstract fun boardGameDao(): BoardGameDao
    abstract fun playerDao(): PlayerDao
    abstract fun gameplayDao(): GameplayDao
    abstract fun syncDao(): SyncDao

    companion object {
        @Volatile
        private var Instance: BoardGameDatabase? = null

        private fun seedDatabaseCallback(context: Context): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    Executors.newSingleThreadExecutor().execute {
                        getDatabase(context).syncDao().verifySyncInfo()
                    }
                }
            }
        }

        fun getDatabase(context: Context): BoardGameDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, BoardGameDatabase::class.java, "board-game-stats-40")
                    .addCallback(seedDatabaseCallback(context))
                    .build()
                    .also { Instance = it }
            }
        }
    }
}