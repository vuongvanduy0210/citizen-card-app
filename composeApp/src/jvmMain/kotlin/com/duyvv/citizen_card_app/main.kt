package com.duyvv.citizen_card_app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.duyvv.citizen_card_app.di.appModule
import com.duyvv.citizen_card_app.presentation.App
import org.koin.core.context.startKoin
import java.io.PrintStream
import java.nio.charset.StandardCharsets

fun main() = application {
    System.setOut(PrintStream(System.out, true, StandardCharsets.UTF_8))
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "citizen_card_app",
    ) {
        App()
    }
}

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}