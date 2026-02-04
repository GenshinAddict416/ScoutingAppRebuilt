package com.sotabots.sotabotsscouting

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MatchData::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
}