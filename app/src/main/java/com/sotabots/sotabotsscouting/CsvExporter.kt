package com.sotabots.sotabotsscouting

import android.content.Context
import android.util.Log
import java.io.File

object CsvExporter {

    const val TABLET = "scouting_export_tablet3.csv"

    fun exportToCsv(context: Context, matches: List<MatchData>) {

        // Build CSV
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
            if (match.win) {
                rp += 3
            }
            if (match.energized) {
                rp += 1
            }
            if (match.supercharged) {
                rp += 1
                if (!match.energized) {
                    rp += 1
                }
            }
            if (match.traversal) {
                rp += 1
            }

            val row =
                "${match.teamNumber}," +
                        "${match.matchNumber}," +
                        "${match.alliance}," +
                        "${match.autoFuel}," +
                        "${match.autoAmount}," +
                        "${match.teleopFuel}," +
                        "${match.teleopAmount}," +
                        "${match.autoClimb}," +
                        "${match.endgame}," +
                        "${match.fouls}," +
                        "${match.activeHub}," +
                        "${match.inactiveHub}," +
                        "${match.win}," +
                        "${match.energized}," +
                        "${match.supercharged}," +
                        "${match.traversal}," +
                        "$rp," +
                        "\"$safeComments\""

            Log.d("CSV_ROW", row)
            csvBuilder.append("$row\n")
        }

        val dir: File? = context.getExternalFilesDir(null)

        if (dir == null) {
            Log.e("CSV_EXPORT", "ERROR: getExternalFilesDir(null) returned null")
            return
        }

        Log.d("CSV_EXPORT", "dir path = ${dir.absolutePath}, exists=${dir.exists()}")


        // CHANGE THIS IF YOU ARE EDITING THE CODE TO MATCH THE APPROPRIATE TABLET
        val file = File(dir, TABLET)
        /*                                                          ^    */
        Log.d("CSV_EXPORT", "target file = ${file.absolutePath}")


        // Creating file and logging it for debugging
        val success = try {
            if (file.exists()) {
                Log.d("CSV_EXPORT", "Old file exists, deleting")
                file.delete()
            }

            file.outputStream().use { out ->
                val bytes = csvBuilder.toString().toByteArray()
                Log.d("CSV_EXPORT", "Writing ${bytes.size} bytes")
                out.write(bytes)
                out.flush()
            }

            Log.d("CSV_EXPORT", "Write finished, file.exists=${file.exists()}, length=${file.length()}")
            true

        } catch (e: Exception) {
            Log.e("CSV_EXPORT", "Write failed", e)
            false
        }

        Log.d("CSV_EXPORT", "Write success=$success")
    }

    fun importFromCsv(context: Context): List<MatchData> {
        val dir = context.getExternalFilesDir(null) ?: return emptyList()
        val file = File(dir, TABLET)

        if (!file.exists()) return emptyList()

        val importedMatches = mutableListOf<MatchData>()

        try {
            val lines = file.readLines()
            if (lines.size <= 1) return emptyList() // Only header or empty

            // Skip the header (index 0)
            for (i in 1 until lines.size) {
                val line = lines[i]
                if (line.isBlank()) continue

                // This regex splits by comma but ignores commas inside quotes
                val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())

                if (tokens.size >= 17) {
                    importedMatches.add(MatchData(
                        id = 0, // Database will generate new IDs
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
                        // tokens[16] is the calculated RP, which we don't need for the model
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