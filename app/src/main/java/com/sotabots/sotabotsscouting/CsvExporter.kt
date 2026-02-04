package com.sotabots.sotabotsscouting

import android.content.Context
import android.util.Log
import java.io.File

object CsvExporter {

    fun exportToCsv(context: Context, matches: List<MatchData>) {

        // Build CSV
        val csvBuilder = StringBuilder()
        csvBuilder.append(
            "Team,Match,Alliance,AutoFuel,TeleopFuel,AutoClimb,Endgame,Fouls,Comments\n"
        )


        for (match in matches) {
            val safeComments = match.comments
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\"", "'")

            val row =
                "${match.teamNumber}," +
                        "${match.matchNumber}," +
                        "${match.alliance}," +
                        "${match.autoFuel}," +
                        "${match.teleopFuel}," +
                        "${match.autoClimb}," +
                        "${match.endgame}," +
                        "${match.fouls}," +
                        "\"$safeComments\""

            Log.d("CSV_ROW", row)
            csvBuilder.append("$row\n")
        }

        // ⭐ SAFE: getExternalFilesDir(null) can return null
        val dir: File? = context.getExternalFilesDir(null)

        if (dir == null) {
            Log.e("CSV_EXPORT", "ERROR: getExternalFilesDir(null) returned null")
            return
        }

        Log.d("CSV_EXPORT", "dir path = ${dir.absolutePath}, exists=${dir.exists()}")


        // CHANGE THIS IF YOU ARE EDITING THE CODE TO MATCH THE APPROPRIATE TABLET
        val file = File(dir, "scouting_export_tablet1.csv")
        /*                                                          ^    */
        Log.d("CSV_EXPORT", "target file = ${file.absolutePath}")

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
}