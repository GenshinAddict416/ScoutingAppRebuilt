package com.sotabots.sotabotsscouting

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ScoutingForm(
    modifier: Modifier = Modifier,
    db: AppDatabase?,
    editingMatch: MatchData? = null, // The match being edited (null if new)
    onSaveComplete: () -> Unit       // Callback to return to the list
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val prefs = remember { context.getSharedPreferences("scouting_prefs", Context.MODE_PRIVATE) }
    val tabletIndex = prefs.getInt("tablet_index", 1) // Defaults to Red 1
    val autoAlliance = if (tabletIndex <= 3) "Red" else "Blue"
    // Initialize states: use editingMatch values if they exist, otherwise defaults
    var teamNumber by remember { mutableStateOf(editingMatch?.teamNumber?.toString() ?: "") }
    var matchNumber by remember { mutableStateOf(editingMatch?.matchNumber?.toString() ?: "") }
    // This watches the Match Number box. If it finds a match, it fills the Team box.
    LaunchedEffect(matchNumber) {
        val mNum = matchNumber.toIntOrNull()
        if (mNum != null && editingMatch == null && db != null) {
            val scheduledMatch = db.matchDao().getMatchByNumber(mNum)
            if (scheduledMatch != null) {
                teamNumber = scheduledMatch.teamNumber.toString()
            }
        }
    }
    var autoFuel by remember { mutableStateOf(editingMatch?.autoFuel?.toFloat() ?: 0f) }
    var teleopFuel by remember { mutableStateOf(editingMatch?.teleopFuel?.toFloat() ?: 0f) }
    var autoFuelAmount by remember { mutableStateOf(editingMatch?.autoAmount ?: "None") }
    var teleopFuelAmount by remember { mutableStateOf(editingMatch?.teleopAmount ?: "None") }
    var autoClimb by remember { mutableStateOf(editingMatch?.autoClimb ?: "No") }
    var alliance by remember { mutableStateOf(editingMatch?.alliance ?: autoAlliance) }
    var inactiveHub by remember { mutableStateOf(editingMatch?.inactiveHub ?: "None") }
    var activeHub by remember { mutableStateOf(editingMatch?.activeHub ?: "None") }
    var fouls by remember { mutableStateOf(editingMatch?.fouls ?: "None") }
    var endgame by remember { mutableStateOf(editingMatch?.endgame ?: "None") }
    var win by remember { mutableStateOf(editingMatch?.win ?: false) }
    var energized by remember { mutableStateOf(editingMatch?.energized ?: false) }
    var supercharged by remember { mutableStateOf(editingMatch?.supercharged ?: false) }
    var traversal by remember { mutableStateOf(editingMatch?.traversal ?: false) }
    var comments by remember { mutableStateOf(editingMatch?.comments ?: "") }

    val autoFuelOptions = listOf("None", "1-15", "16-30", "31-45", "46-60", "61-75", "76-90", "91-105", "106-120")
    val teleopFuelOptions = listOf("None", "1-15", "16-30", "31-45", "46-60", "61-75", "76-90", "91-105", "106-120")
    val yesNoOptions = listOf("No", "Yes")
    val redorblue = listOf("Red", "Blue")
    val hubOptions = listOf("None", "Collecting Balls For Next Round (offense)", "Defense", "Other")
    val actHubOptions = listOf("None", "Offense", "Defense", "Other (Specify in Comments)")
    val foulOptions = listOf("None", "Some (<4 Minor / <2 Major)", "A Lot (>3 Minor / >1 Major)")
    val endgameOptions = listOf("None", "L1", "L2", "L3")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (editingMatch == null) "New Scouting Entry" else "Editing Match ${editingMatch.matchNumber}",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = matchNumber,
            onValueChange = { matchNumber = it },
            label = { Text("Match Number") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = teamNumber,
            onValueChange = { teamNumber = it },
            label = { Text("Team Number") },
            modifier = Modifier.fillMaxWidth()
        )



        Dropdown(label = "Alliance", options = redorblue, selected = alliance, onSelect = { alliance = it })

        Text("Auto Scored: ${autoFuel.toInt()}%")
        Slider(value = autoFuel, onValueChange = { autoFuel = it }, valueRange = 0f..100f, steps = 99)

        Dropdown(label = "Est. Auto Fuel", options = autoFuelOptions, selected = autoFuelAmount, onSelect = { autoFuelAmount = it })
        Dropdown(label = "Auto Climb", options = yesNoOptions, selected = autoClimb, onSelect = { autoClimb = it })

        Text("Teleop Scored: ${teleopFuel.toInt()}%")
        Slider(value = teleopFuel, onValueChange = { teleopFuel = it }, valueRange = 0f..100f, steps = 99)

        Dropdown(label = "Est. Teleop Fuel", options = teleopFuelOptions, selected = teleopFuelAmount, onSelect = { teleopFuelAmount = it })
        Dropdown(label = "Inactive Hub Strategy", options = hubOptions, selected = inactiveHub, onSelect = { inactiveHub = it })
        Dropdown(label = "Active Hub Strategy", options = actHubOptions, selected = activeHub, onSelect = { activeHub = it })
        Dropdown(label = "Fouls", options = foulOptions, selected = fouls, onSelect = { fouls = it })
        Dropdown(label = "Endgame", options = endgameOptions, selected = endgame, onSelect = { endgame = it })

        Text("Ranking Points", style = MaterialTheme.typography.titleMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = win, onCheckedChange = { win = it })
            Text("Win (+3 RP)")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = energized, onCheckedChange = { energized = it })
            Text("Energized (+1 RP)")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = supercharged, onCheckedChange = { supercharged = it })
            Text("Supercharged (+1 RP)")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = traversal, onCheckedChange = { traversal = it })
            Text("Traversal (+1 RP)")
        }

        OutlinedTextField(
            value = comments,
            onValueChange = { comments = it },
            label = { Text("Comments") },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (teamNumber.isBlank() || matchNumber.isBlank()) {
                    Toast.makeText(context, "Team & Match required", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val match = MatchData(
                    teamNumber = teamNumber.toInt(),
                    matchNumber = matchNumber.toInt(),
                    alliance = alliance,
                    autoFuel = autoFuel.toInt(),
                    autoAmount = autoFuelAmount,
                    teleopFuel = teleopFuel.toInt(),
                    teleopAmount = teleopFuelAmount,
                    autoClimb = autoClimb,
                    endgame = endgame,
                    fouls = fouls,
                    inactiveHub = inactiveHub,
                    activeHub = activeHub,
                    win = win,
                    energized = energized,
                    supercharged = supercharged,
                    traversal = traversal,
                    comments = comments
                )

                scope.launch {
                    if (editingMatch == null) {
                        db?.matchDao()?.insert(match)
                    } else {
                        db?.matchDao()?.update(match)
                    }
                    onSaveComplete()
                }

                Toast.makeText(context, if (editingMatch == null) "Saved" else "Updated", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (editingMatch == null) "Submit" else "Save Changes")
        }

        OutlinedButton(
            onClick = onSaveComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel / View Matches")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScoutingFormPreview() {
    ScoutingForm(db = null, onSaveComplete = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
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
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); expanded = false })
            }
        }
    }
}