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
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult


@Composable
fun ViewMatchesScreen(
    db: AppDatabase,
    onBack: () -> Unit,
    onEdit: (MatchData) -> Unit
) {
    val scope = rememberCoroutineScope()
    var matches by remember { mutableStateOf<List<MatchData>>(emptyList()) }
    val context = LocalContext.current

    val prefs = remember { context.getSharedPreferences("scouting_prefs", Context.MODE_PRIVATE) }
    var selectedTabletIndex by remember { mutableStateOf(prefs.getInt("tablet_index", 1)) }
    val tabletOptions = listOf("Red 1", "Red 2", "Red 3", "Blue 1", "Blue 2", "Blue 3")

    // 1. State for the Delete Confirmation
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun refreshMatches() {
        scope.launch {
            matches = db.matchDao().getAll()
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = com.journeyapps.barcodescanner.ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                val importedMatches = ScheduleImporter.parseQrToMatches(result.contents, selectedTabletIndex)
                scope.launch {
                    db.matchDao().insertAll(importedMatches)
                    refreshMatches()
                    Toast.makeText(context, "Synced ${importedMatches.size} matches!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        refreshMatches()
    }

    // 2. THE ALERT DIALOG (Invisible until showDeleteDialog is true)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all scouted matches. Make sure you've exported your CSV first!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            db.matchDao().deleteAll() // Make sure you added this to MatchDao.kt!
                            refreshMatches()
                            showDeleteDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "Saved Matches", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Text("Device Configuration", style = MaterialTheme.typography.labelLarge)
        SettingsDropdown(
            label = "This Tablet Is:",
            options = tabletOptions,
            selected = tabletOptions[selectedTabletIndex - 1],
            onSelect = { choice ->
                val newIndex = tabletOptions.indexOf(choice) + 1
                selectedTabletIndex = newIndex
                prefs.edit().putInt("tablet_index", newIndex).apply()
            }
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

        Button(
            onClick = {
                val options = com.journeyapps.barcodescanner.ScanOptions()
                options.setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.QR_CODE)
                options.setPrompt("Scan Scouting Schedule")
                options.setBeepEnabled(true)
                options.setOrientationLocked(false)
                options.setBarcodeImageEnabled(true)
                options.setCameraId(0)
                scanLauncher.launch(options) // Launch call goes AFTER setting options
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50))
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sync Schedule from QR")
        }

        OutlinedButton(
            onClick = {
                scope.launch {
                    val imported = CsvExporter.importFromCsv(context)
                    if (imported.isNotEmpty()) {
                        db.matchDao().insertAll(imported)
                        refreshMatches()
                        Toast.makeText(context, "Restored ${imported.size} matches", Toast.LENGTH_SHORT).show()
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

        // 3. THE "NUKE" BUTTON
        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Clear All Match Data")
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(matches) { match ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Team ${match.teamNumber}", style = MaterialTheme.typography.titleLarge)
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
                                IconButton(onClick = { onEdit(match) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { scope.launch { db.matchDao().delete(match); refreshMatches() } }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { item ->
                DropdownMenuItem(text = { Text(item) }, onClick = { onSelect(item); expanded = false })
            }
        }
    }
}