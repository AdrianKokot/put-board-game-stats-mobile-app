package com.example.boardgamestats.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.boardgamestats.models.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player")
    fun getAll(): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg players: Player)
}