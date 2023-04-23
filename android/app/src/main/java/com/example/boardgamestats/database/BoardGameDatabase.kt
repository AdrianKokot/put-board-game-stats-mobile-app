package com.example.boardgamestats.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.boardgamestats.database.daos.*
import com.example.boardgamestats.models.*
import java.util.concurrent.Executors

@Database(
    entities = [BoardGame::class, Player::class, Gameplay::class, PlayerWithScore::class, SyncInfo::class, UserSettings::class],
    version = 1,
    exportSchema = false
)
abstract class BoardGameDatabase : RoomDatabase() {
    abstract fun boardGameDao(): BoardGameDao
    abstract fun playerDao(): PlayerDao
    abstract fun gameplayDao(): GameplayDao
    abstract fun syncDao(): SyncDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var Instance: BoardGameDatabase? = null

        private fun seedDatabaseCallback(context: Context): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    Executors.newSingleThreadExecutor().execute {
                        val database = getDatabase(context)
                        database.syncDao().verifySyncInfo()
                        database.settingsDao().verifySettings()
                    }
                }
            }
        }

        fun getDatabase(context: Context): BoardGameDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, BoardGameDatabase::class.java, "board-game-stats-53")
                    .enableMultiInstanceInvalidation()
                    .addCallback(seedDatabaseCallback(context))
                    .build()
                    .also { Instance = it }
            }
        }
    }
}