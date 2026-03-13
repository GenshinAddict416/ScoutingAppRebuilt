package com.sotabots.sotabotsscouting

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutingForm(
    modifier: Modifier = Modifier,
    db: AppDatabase,
    onViewMatches: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var teamNumber by remember { mutableStateOf("") }
    var matchNumber by remember { mutableStateOf("") }

    var autoFuel by remember { mutableStateOf(0f) }
    var teleopFuel by remember { mutableStateOf(0f)}

    var autoFuelAmount by remember { mutableStateOf("None") }
    var teleopFuelAmount by remember { mutableStateOf("None") }

    val autoFuelOptions = listOf(
        "None",
        "1-15",
        "16-30",
        "31-45",
        "46-60",
        "61-75",
        "76-90"
    )
    val teleopFuelOptions = listOf(
        "None",
        "1-15",
        "16-30",
        "31-45",
        "46-60",
        "61-75",
        "76-90"
    )

    var autoClimb by remember { mutableStateOf("No") }
    val yesNoOptions = listOf("No", "Yes")

    var alliance by remember { mutableStateOf("Red") }
    val redorblue = listOf("Red", "Blue")

    var inactiveHub by remember { mutableStateOf("None") }
    var activeHub by remember { mutableStateOf("None") }
    val hubOptions = listOf(
        "None",
        "Collecting Balls For Next Round (offense)",
        "Defense",
        "Other"
    )
    val actHubOptions = listOf(
        "None",
        "Offense",
        "Defense",
        "Other (Specify in Comments)"
    )

    var fouls by remember { mutableStateOf("None") }
    val foulOptions = listOf(
        "None",
        "Some (<4 Minor / <2 Major)",
        "A Lot (>3 Minor / >1 Major)"
    )

    var endgame by remember { mutableStateOf("None") }
    val endgameOptions = listOf("None", "L1", "L2", "L3")

    var comments by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = teamNumber,
            onValueChange = { teamNumber = it },
            label = { Text("Team Number") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = matchNumber,
            onValueChange = { matchNumber = it },
            label = { Text("Match Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Dropdown(
            label = "Alliance",
            options = redorblue,
            selected = alliance,
            onSelect = { alliance = it }
        )

        Text("In Auto, how much of the shot fuel was scored? ${autoFuel.toInt()}%")
        Slider(
            value = autoFuel,
            onValueChange = { autoFuel = it },
            valueRange = 0f..100f,
            steps = 99
        )

        Dropdown(
            label = "Guess/Estimate: How much fuel was scored in Auto?",
            options = autoFuelOptions,
            selected = autoFuelAmount,
            onSelect = { autoFuelAmount = it }
        )

        Dropdown(
            label = "Auto Climb",
            options = yesNoOptions,
            selected = autoClimb,
            onSelect = { autoClimb = it }
        )

        Text("In Teleop, how much of the shot fuel was scored? ${teleopFuel.toInt()}%")
        Slider(
            value = teleopFuel,
            onValueChange = { teleopFuel = it },
            valueRange = 0f..100f,
            steps = 99
        )

        Dropdown(
            label = "Guess/Estimate: How Much Fuel was scored in TeleOp?",
            options = teleopFuelOptions,
            selected = teleopFuelAmount,
            onSelect = { teleopFuelAmount = it }
        )

        Dropdown(
            label = "Inactive Hub Strategy",
            options = hubOptions,
            selected = inactiveHub,
            onSelect = { inactiveHub = it }
        )

        Dropdown(
            label = "Active Hub Strategy",
            options = actHubOptions,
            selected = activeHub,
            onSelect = { activeHub = it }
        )



        Dropdown(
            label = "Fouls",
            options = foulOptions,
            selected = fouls,
            onSelect = { fouls = it }
        )

        Dropdown(
            label = "Endgame",
            options = endgameOptions,
            selected = endgame,
            onSelect = { endgame = it }
        )

        OutlinedTextField(
            value = comments,
            onValueChange = { comments = it },
            label = { Text("Comments") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
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
                    comments = comments
                )

                scope.launch {
                    db.matchDao().insert(match)
                }

                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()

                teamNumber = ""
                matchNumber = ""
                alliance =  "Red"
                autoFuel = 0f
                autoFuelAmount = "None"
                teleopFuel = 0f
                teleopFuelAmount = "None"
                autoClimb = "No"
                inactiveHub = "None"
                activeHub = "None"
                fouls = "None"
                endgame = "None"
                comments = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }

        Button(
            onClick = onViewMatches,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Matches")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
