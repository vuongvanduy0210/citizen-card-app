package com.duyvv.citizen_card_app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.duyvv.citizen_card_app.presentation.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "citizen_card_app",
    ) {
        App()
    }
}