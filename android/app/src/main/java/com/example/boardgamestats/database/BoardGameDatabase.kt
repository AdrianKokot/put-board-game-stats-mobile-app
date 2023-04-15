package com.example.boardgamestats.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.boardgamestats.database.daos.BoardGameDao
import com.example.boardgamestats.database.daos.GameplayDao
import com.example.boardgamestats.database.daos.PlayerDao
import com.example.boardgamestats.models.BoardGame
import com.example.boardgamestats.models.Gameplay
import com.example.boardgamestats.models.Player
import com.example.boardgamestats.models.PlayerWithScore

@Database(
    entities = [BoardGame::class, Player::class, Gameplay::class, PlayerWithScore::class],
    version = 1,
    exportSchema = false
)
abstract class BoardGameDatabase : RoomDatabase() {
    abstract fun boardGameDao(): BoardGameDao
    abstract fun playerDao(): PlayerDao
    abstract fun gameplayDao(): GameplayDao

    companion object {
        @Volatile
        private var Instance: BoardGameDatabase? = null

        fun getDatabase(context: Context): BoardGameDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, BoardGameDatabase::class.java, "board-game-stats-33").build()
                    .also { Instance = it }
            }
        }
    }
}