package com.duyvv.citizen_card_app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.duyvv.citizen_card_app.data.local.table.CitizenTable
import com.duyvv.citizen_card_app.data.local.table.DrivingLicenseTable
import com.duyvv.citizen_card_app.data.local.table.HealthInsuranceTable
import com.duyvv.citizen_card_app.data.local.table.VehicleRegisterTable
import com.duyvv.citizen_card_app.di.appModule
import com.duyvv.citizen_card_app.presentation.App
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.context.startKoin
import java.io.PrintStream
import java.nio.charset.StandardCharsets

fun main() = application {
    System.setOut(PrintStream(System.out, true, StandardCharsets.UTF_8))
    initKoin()
    Database.connect(
        url = "jdbc:sqlite:data.db", // Tên file database (sẽ tự tạo nếu chưa có)
        driver = "org.sqlite.JDBC"   // Driver của thư viện org.xerial
    )
    transaction {
        // Hàm này sẽ tự kiểm tra "IF NOT EXISTS" nên chạy nhiều lần không sao
        SchemaUtils.create(
            CitizenTable,
            DrivingLicenseTable,
            VehicleRegisterTable,
            HealthInsuranceTable
        )

        println("Database tables created successfully!")
    }
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