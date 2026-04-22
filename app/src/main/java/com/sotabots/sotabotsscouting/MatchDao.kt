package com.sotabots.sotabotsscouting

import androidx.room.*

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches")
    suspend fun getAll(): List<MatchData>

    @Insert(onConflict = OnConflictStrategy.REPLACE) // This is the magic line
    suspend fun insert(match: MatchData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<MatchData>)

    @Query("SELECT * FROM matches WHERE matchNumber = :num LIMIT 1")
    suspend fun getMatchByNumber(num: Int): MatchData?

    @Update
    suspend fun update(match: MatchData) // Added for editing

    @Delete
    suspend fun delete(match: MatchData)

    @Query("DELETE FROM matches")
    suspend fun deleteAll()
}