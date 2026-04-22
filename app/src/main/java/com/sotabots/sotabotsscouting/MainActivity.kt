package com.sotabots.sotabotsscouting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.sotabots.sotabotsscouting.ui.theme.ScoutingApp2026Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "scouting-db"
        )
            .fallbackToDestructiveMigration()
            .build()

        setContent {
            ScoutingApp2026Theme {
                // track which screen we are on
                var currentScreen by remember { mutableStateOf("form") }
                // track if we are editing an existing match or making a new one
                var matchToEdit by remember { mutableStateOf<MatchData?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "form" -> ScoutingForm(
                            modifier = Modifier.padding(innerPadding),
                            db = db,
                            editingMatch = matchToEdit,
                            onSaveComplete = {
                                // Reset the edit state and go to the list
                                matchToEdit = null
                                currentScreen = "view"
                            }
                        )

                        "view" -> ViewMatchesScreen(
                            db = db,
                            onBack = {
                                // Reset edit state when going back manually
                                matchToEdit = null
                                currentScreen = "form"
                            },
                            onEdit = { match ->
                                // Set the match to edit and swap to the form screen
                                matchToEdit = match
                                currentScreen = "form"
                            }
                        )
                    }
                }
            }
        }
    }
}