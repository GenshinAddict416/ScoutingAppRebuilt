package com.sotabots.sotabotsscouting

import android.content.Context
import android.util.Log
import java.io.File

object CsvExporter {


    private fun getTabletFileName(context: Context): String {
        val prefs = context.getSharedPreferences("scouting_prefs", Context.MODE_PRIVATE)
        val tabletId = prefs.getInt("tablet_index", 1) // Defaults to 1
        return "scouting_export_tablet_$tabletId.csv"
    }

    fun exportToCsv(context: Context, matches: List<MatchData>) {
        val csvBuilder = StringBuilder()
        csvBuilder.append(
            "Team,Match,Alliance,AutoAccuracy,AutoAmount,TeleopAccuracy,TeleopAmount,AutoClimb,Endgame,Fouls,ActiveStrat,InactiveStrat,Win,Energized,Supercharged,Traversal,TotalRP,Comments\n"
        )

        for (match in matches) {
            val safeComments = match.comments
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\"", "'")

            var rp = 0
            if (match.win) rp += 3
            if (match.energized) rp += 1
            if (match.supercharged) {
                rp += 1
                if (!match.energized) rp += 1
            }
            if (match.traversal) rp += 1

            val row = "${match.teamNumber},${match.matchNumber},${match.alliance}," +
                    "${match.autoFuel},${match.autoAmount},${match.teleopFuel}," +
                    "${match.teleopAmount},${match.autoClimb},${match.endgame}," +
                    "${match.fouls},${match.activeHub},${match.inactiveHub}," +
                    "${match.win},${match.energized},${match.supercharged}," +
                    "${match.traversal},$rp,\"$safeComments\""

            csvBuilder.append("$row\n")
        }

        val dir: File? = context.getExternalFilesDir(null)
        if (dir == null) return

        // 2. USE THE DYNAMIC FILENAME
        val fileName = getTabletFileName(context)
        val file = File(dir, fileName)

        try {
            if (file.exists()) file.delete()
            file.outputStream().use { out ->
                out.write(csvBuilder.toString().toByteArray())
                out.flush()
            }
            Log.d("CSV_EXPORT", "Saved to $fileName")
        } catch (e: Exception) {
            Log.e("CSV_EXPORT", "Write failed", e)
        }
    }

    fun importFromCsv(context: Context): List<MatchData> {
        val dir = context.getExternalFilesDir(null) ?: return emptyList()

        // 3. USE THE DYNAMIC FILENAME HERE TOO
        val fileName = getTabletFileName(context)
        val file = File(dir, fileName)

        if (!file.exists()) return emptyList()

        val importedMatches = mutableListOf<MatchData>()
        try {
            val lines = file.readLines()
            if (lines.size <= 1) return emptyList()

            for (i in 1 until lines.size) {
                val line = lines[i]
                if (line.isBlank()) continue
                val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())

                if (tokens.size >= 17) {
                    importedMatches.add(MatchData(
                        teamNumber = tokens[0].toIntOrNull() ?: 0,
                        matchNumber = tokens[1].toIntOrNull() ?: 0,
                        alliance = tokens[2],
                        autoFuel = tokens[3].toIntOrNull() ?: 0,
                        autoAmount = tokens[4],
                        teleopFuel = tokens[5].toIntOrNull() ?: 0,
                        teleopAmount = tokens[6],
                        autoClimb = tokens[7],
                        endgame = tokens[8],
                        fouls = tokens[9],
                        activeHub = tokens[10],
                        inactiveHub = tokens[11],
                        win = tokens[12].toBoolean(),
                        energized = tokens[13].toBoolean(),
                        supercharged = tokens[14].toBoolean(),
                        traversal = tokens[15].toBoolean(),
                        comments = tokens.getOrNull(17)?.replace("\"", "") ?: ""
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("CSV_IMPORT", "Restore failed", e)
        }
        return importedMatches
    }
}