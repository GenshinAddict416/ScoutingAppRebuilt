package com.sotabots.sotabotsscouting

import androidx.room.*

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches")
    suspend fun getAll(): List<MatchData>

    @Insert
    suspend fun insert(match: MatchData)

    @Update
    suspend fun update(match: MatchData) // Added for editing

    @Delete
    suspend fun delete(match: MatchData)
}