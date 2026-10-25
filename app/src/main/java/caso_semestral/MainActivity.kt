package com.example.caso_semestral

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.caso_semestral.ui.AppRoot
import com.example.caso_semestral.ui.theme.Caso_semestralTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Caso_semestralTheme {
                AppRoot()
            }
        }
    }
}