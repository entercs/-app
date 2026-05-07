package com.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val navigateToAdd = intent?.getBooleanExtra("navigate_to_add", false) == true
        setContent {
            App(initialNavigateToAdd = navigateToAdd)
        }
    }
}
