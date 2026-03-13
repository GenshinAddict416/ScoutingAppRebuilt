package com.sotabots.sotabotsscouting

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MatchData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val teamNumber: Int,
    val matchNumber: Int,

    val alliance: String,

    val autoFuel: Int,
    val autoAmount: String,
    val teleopFuel: Int,
    val teleopAmount: String,

    val autoClimb: String,
    val endgame: String,

    val fouls: String, // dropdown: None / Some / A Lot

    val inactiveHub: String,
    val activeHub: String,

    val comments: String
)
