package com.donfuy.fakeartistny.data

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.donfuy.fakeartistny.model.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player")
    fun getPlayers(): Flow<List<Player>>

    @Query("SELECT * FROM player WHERE id = :id")
    fun getPlayer(id: Long): Flow<Player>

    @Insert(onConflict = REPLACE)
    suspend fun insert(player: Player)

    @Update
    suspend fun update(player: Player)

    @Delete
    suspend fun delete(player: Player)

}