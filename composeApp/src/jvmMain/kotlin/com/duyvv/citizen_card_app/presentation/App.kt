package com.duyvv.citizen_card_app.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.duyvv.citizen_card_app.presentation.home.MainScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        MainScreen()
    }
}