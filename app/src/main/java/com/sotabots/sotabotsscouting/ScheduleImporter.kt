package com.sotabots.sotabotsscouting

object ScheduleImporter {
    // We add 'tabletIndex' here (1-6)
    fun parseQrToMatches(rawString: String, tabletIndex: Int): List<MatchData> {
        val matches = mutableListOf<MatchData>()
        val blocks = rawString.split(";")
        for (block in blocks) {
            if (block.isBlank()) continue
            val p = block.split(",")
            if (p.size == 7) {
                matches.add(MatchData(
                    matchNumber = p[0].toIntOrNull() ?: 0,
                    // Grabs the team based on which tablet this is!
                    teamNumber = p[tabletIndex].toIntOrNull() ?: 0,
                    alliance = if (tabletIndex <= 3) "Red" else "Blue",
                    autoFuel = 0, autoAmount = "None", teleopFuel = 0, teleopAmount = "None",
                    autoClimb = "No", endgame = "None", fouls = "None",
                    inactiveHub = "None", activeHub = "None", win = false,
                    energized = false, supercharged = false, traversal = false, comments = "QR Import"
                ))
            }
        }
        return matches
    }
}