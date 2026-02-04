package com.sotabots.sotabotsscouting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ViewMatchesScreen(
    db: AppDatabase,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var matches by remember { mutableStateOf<List<MatchData>>(emptyList()) }
    val context = LocalContext.current

    fun refreshMatches() {
        scope.launch {
            matches = db.matchDao().getAll()
        }
    }

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
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export CSV")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Team ${match.teamNumber} — Match ${match.matchNumber}",
                                style = MaterialTheme.typography.headlineSmall
                            )

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
                                    contentDescription = "Delete match"
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text("Auto: ${match.autoFuel}%", style = MaterialTheme.typography.bodyMedium)
                        Text("Teleop: ${match.teleopFuel}%", style = MaterialTheme.typography.bodyMedium)
                        Text("Auto Climb: ${match.autoClimb}", style = MaterialTheme.typography.bodyMedium)
                        Text("Endgame: ${match.endgame}", style = MaterialTheme.typography.bodyMedium)
                        Text("Comments: ${match.comments}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
