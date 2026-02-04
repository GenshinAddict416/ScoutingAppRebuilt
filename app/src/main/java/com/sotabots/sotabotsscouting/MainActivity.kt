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
                var currentScreen by remember { mutableStateOf("form") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "form" -> ScoutingForm(
                            modifier = Modifier.padding(innerPadding),
                            db = db,
                            onViewMatches = { currentScreen = "view" }
                        )

                        "view" -> ViewMatchesScreen(
                            db = db,
                            onBack = { currentScreen = "form" }
                        )
                    }
                }
            }
        }
    }
}