package com.duyvv.citizen_card_app.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.duyvv.citizen_card_app.presentation.dialog.FormInput
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorTextPrimary

@Composable
fun IntegratedDocumentsDialog(
    onDismiss: () -> Unit
) {
    var showVehicleDialog by remember { mutableStateOf(false) }
    var showDrivingDialog by remember { mutableStateOf(false) }
    var showHealthDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(700.dp)
                .height(450.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Nút đóng (X) ở góc phải
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // --- TITLE ---
                    Text(
                        text = "GIẤY TỜ TÍCH HỢP",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary,
                        modifier = Modifier.padding(bottom = 40.dp)
                    )

                    // --- MENU BUTTONS (Row) ---
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nút 1: Giấy đăng ký xe
                        MenuIconButton(
                            text = "Giấy đăng ký xe",
                            icon = Icons.Default.DirectionsCar, // Thay bằng ảnh xe nếu có
                            color = Color(0xFF4CAF50),
                            onClick = { showVehicleDialog = true }
                        )

                        // Nút 2: Giấy phép lái xe
                        MenuIconButton(
                            text = "Giấy phép lái xe",
                            icon = Icons.Default.Badge, // Icon thẻ ID/Bằng lái
                            color = Color(0xFF2196F3),
                            onClick = { showDrivingDialog = true }
                        )

                        // Nút 3: Bảo hiểm y tế
                        MenuIconButton(
                            text = "Bảo hiểm y tế",
                            icon = Icons.Default.MedicalServices, // Icon y tế
                            color = Color(0xFFFF9800),
                            onClick = { showHealthDialog = true }
                        )
                    }
                }
            }
        }
    }

    // --- HIỂN THỊ DIALOG CON ---
    if (showVehicleDialog) {
        VehicleRegisterDialog(onDismiss = { showVehicleDialog = false })
    }
    if (showDrivingDialog) {
        DrivingLicenseDialog(onDismiss = { showDrivingDialog = false })
    }
    if (showHealthDialog) {
        HealthInsuranceDialog(onDismiss = { showHealthDialog = false })
    }
}

// --- 1. DIALOG GIẤY ĐĂNG KÝ XE (Vehicle Register) ---
@Composable
fun VehicleRegisterDialog(onDismiss: () -> Unit) {
    // Các trường dữ liệu
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var frame by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf("") }
    var engine by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var expiredDate by remember { mutableStateOf("") }

    BaseFormDialog(
        title = "Giấy đăng ký xe",
        onDismiss = onDismiss,
        onSave = { /* TODO: Save logic */ }
    ) {
        // Layout Grid 2 cột
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Tên nhãn hiệu", brand, { brand = it }, Modifier.weight(1f))
            FormInput("Model", model, { model = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Màu xe", color, { color = it }, Modifier.weight(1f))
            FormInput("Biển số xe", plate, { plate = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Số khung", frame, { frame = it }, Modifier.weight(1f))
            FormInput("Ngày cấp", issueDate, { issueDate = it }, Modifier.weight(1f), icon = Icons.Default.DateRange)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Số máy", engine, { engine = it }, Modifier.weight(1f))
            FormInput("Nơi cấp", place, { place = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Dung tích", capacity, { capacity = it }, Modifier.weight(1f))
            FormInput(
                "Thời hạn",
                expiredDate,
                { expiredDate = it },
                Modifier.weight(1f),
                icon = Icons.Default.DateRange
            )
        }
    }
}

// --- 2. DIALOG GIẤY PHÉP LÁI XE (Driving License) ---
@Composable
fun DrivingLicenseDialog(onDismiss: () -> Unit) {
    var id by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var expiredDate by remember { mutableStateOf("") }
    var createdBy by remember { mutableStateOf("") }

    BaseFormDialog(
        title = "Giấy phép lái xe",
        onDismiss = onDismiss,
        onSave = { /* TODO */ }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Mã số thẻ", id, { id = it }, Modifier.weight(1f))
            FormInput("Hạng giấy phép", level, { level = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Ngày cấp", issueDate, { issueDate = it }, Modifier.weight(1f), icon = Icons.Default.DateRange)
            FormInput("Nơi cấp", place, { place = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Thời hạn", expiredDate, { expiredDate = it }, Modifier.weight(1f))
            FormInput("Người cấp", createdBy, { createdBy = it }, Modifier.weight(1f))
        }
    }
}

// --- 3. DIALOG BẢO HIỂM Y TẾ (Health Insurance) ---
@Composable
fun HealthInsuranceDialog(onDismiss: () -> Unit) {
    var id by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf("") }
    var expiredDate by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }

    BaseFormDialog(
        title = "Bảo hiểm y tế",
        onDismiss = onDismiss,
        onSave = { /* TODO */ }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Mã số thẻ", id, { id = it }, Modifier.weight(1f))
            FormInput("Địa chỉ", address, { address = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Ngày cấp", issueDate, { issueDate = it }, Modifier.weight(1f), icon = Icons.Default.DateRange)
            FormInput("Thời hạn", expiredDate, { expiredDate = it }, Modifier.weight(1f))
        }
        // Trường Full Width
        FormInput("Nơi đăng ký khám/chữa bệnh", hospital, { hospital = it }, Modifier.fillMaxWidth())
    }
}

// --- HELPER: KHUNG DIALOG CHUNG (Base Shell) ---
@Composable
fun BaseFormDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(700.dp) // ~ prefWidth="660" trong FXML
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )

                // Nội dung form (các TextField)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    content()
                }

                Spacer(Modifier.height(10.dp))

                // Các nút Thoát / Lưu
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff6b6b)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.width(120.dp).height(40.dp)
                    ) {
                        Text("Thoát", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.width(40.dp))

                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.width(120.dp).height(40.dp)
                    ) {
                        Text("Lưu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- HELPER: ICON BUTTON CHO MENU CHÍNH ---
@Composable
fun MenuIconButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Bạn có thể thay Icon bằng Image(painterResource("car.png")...) nếu muốn
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = color,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = ColorTextPrimary
        )
    }
}