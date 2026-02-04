package com.sotabots.sotabotsscouting

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MatchDao {

    @Insert
    suspend fun insert(match: MatchData)

    @Query("SELECT * FROM MatchData")
    suspend fun getAll(): List<MatchData>

    @Delete
    suspend fun delete(match: MatchData)
}
