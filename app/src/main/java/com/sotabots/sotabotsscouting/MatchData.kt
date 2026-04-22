package com.sotabots.sotabotsscouting

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "matches",
    primaryKeys = ["teamNumber", "matchNumber"]
)
data class MatchData(
    val teamNumber: Int,
    val matchNumber: Int,
    val alliance: String,
    val autoFuel: Int,
    val autoAmount: String,
    val teleopFuel: Int,
    val teleopAmount: String,
    val autoClimb: String,
    val endgame: String,
    val fouls: String,
    val inactiveHub: String,
    val activeHub: String,
    val win: Boolean,
    val energized: Boolean,
    val supercharged: Boolean,
    val traversal: Boolean,
    val comments: String
)