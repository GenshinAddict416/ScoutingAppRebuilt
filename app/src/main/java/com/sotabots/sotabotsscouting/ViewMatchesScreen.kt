package com.sotabots.sotabotsscouting

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ViewMatchesScreen(
    db: AppDatabase,
    onBack: () -> Unit,
    onEdit: (MatchData) -> Unit
) {
    val scope = rememberCoroutineScope()
    var matches by remember { mutableStateOf<List<MatchData>>(emptyList()) }
    val context = LocalContext.current

    // Helper to refresh the list from the database
    fun refreshMatches() {
        scope.launch {
            matches = db.matchDao().getAll()
        }
    }

    // Load matches when screen opens
    LaunchedEffect(Unit) {
        refreshMatches()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Saved Matches",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(12.dp))



        Button(
            onClick = {
                scope.launch {
                    CsvExporter.exportToCsv(context, matches)
                    Toast.makeText(context, "Exported to CSV", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export CSV")
        }

        Spacer(Modifier.height(8.dp))

        // RESTORE BUTTON
        OutlinedButton(
            onClick = {
                scope.launch {
                    val imported = CsvExporter.importFromCsv(context)
                    if (imported.isEmpty()) {
                        Toast.makeText(context, "No backup file found or file is empty", Toast.LENGTH_LONG).show()
                    } else {
                        var addedCount = 0
                        imported.forEach { importedMatch ->
                            // Check for duplicates: matching Team + Match Number
                            val alreadyExists = matches.any {
                                it.teamNumber == importedMatch.teamNumber &&
                                        it.matchNumber == importedMatch.matchNumber
                            }

                            if (!alreadyExists) {
                                db.matchDao().insert(importedMatch)
                                addedCount++
                            }
                        }
                        refreshMatches()
                        Toast.makeText(context, "Restored $addedCount new matches", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Restore from CSV")
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Back to Form")
        }

        Spacer(Modifier.height(16.dp))

        // --- MATCH LIST ---

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(matches) { match ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Team ${match.teamNumber}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Match ${match.matchNumber} (${match.alliance})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (match.alliance.contains("Red", ignoreCase = true))
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            }

                            Row {
                                // EDIT BUTTON
                                IconButton(onClick = { onEdit(match) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit match",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // DELETE BUTTON
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            db.matchDao().delete(match)
                                            refreshMatches()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete match",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Quick Stats
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Auto: ${match.autoFuel}%", style = MaterialTheme.typography.bodySmall)
                            Text("Teleop: ${match.teleopFuel}%", style = MaterialTheme.typography.bodySmall)
                            Text("Endgame: ${match.endgame}", style = MaterialTheme.typography.bodySmall)
                        }

                        if (match.comments.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Notes: ${match.comments}",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}