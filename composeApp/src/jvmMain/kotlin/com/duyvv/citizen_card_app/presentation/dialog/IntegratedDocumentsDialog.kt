package com.duyvv.citizen_card_app.presentation.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.duyvv.citizen_card_app.data.local.entity.DrivingLicense
import com.duyvv.citizen_card_app.data.local.entity.HealthInsurance
import com.duyvv.citizen_card_app.data.local.entity.VehicleRegister
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBtnCancelBg
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBtnCancelText
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorHeaderBg
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorTextPrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IntegratedDocumentsDialog(
    citizenId: String, // <-- THÊM THAM SỐ NÀY
    existingVehicle: VehicleRegister?,
    existingLicense: DrivingLicense?,
    existingInsurance: HealthInsurance?,
    onDismiss: () -> Unit,
    onSaveVehicle: (VehicleRegister) -> Unit, // Callback
    onSaveDriving: (DrivingLicense) -> Unit,  // Callback
    onSaveHealth: (HealthInsurance) -> Unit   // Callback
) {
    // State quản lý việc hiển thị các dialog con
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
                .height(400.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Nút đóng (X)
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
                    Text(
                        text = "GIẤY TỜ TÍCH HỢP",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary,
                        modifier = Modifier.padding(bottom = 40.dp)
                    )

                    // 3 Nút chọn loại giấy tờ
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MenuIconButton(
                            text = "Giấy đăng ký xe",
                            icon = Icons.Default.DirectionsCar,
                            color = Color(0xFF4CAF50), // Green
                            onClick = { showVehicleDialog = true }
                        )

                        MenuIconButton(
                            text = "Giấy phép lái xe",
                            icon = Icons.Default.Badge,
                            color = Color(0xFF2196F3), // Blue
                            onClick = { showDrivingDialog = true }
                        )

                        MenuIconButton(
                            text = "Bảo hiểm y tế",
                            icon = Icons.Default.MedicalServices,
                            color = Color(0xFFFF9800), // Orange
                            onClick = { showHealthDialog = true }
                        )
                    }
                }
            }
        }
    }

    // --- HIỂN THỊ DIALOG CON (NẾU ĐƯỢC CHỌN) ---

    if (showVehicleDialog) {
        VehicleRegisterDialog(
            citizenId = citizenId,
            existingItem = existingVehicle, // <-- Truyền vào đây
            onDismiss = { showVehicleDialog = false },
            onSave = {
                onSaveVehicle(it)
                showVehicleDialog = false
            }
        )
    }

    if (showDrivingDialog) {
        DrivingLicenseDialog(
            citizenId = citizenId,
            existingItem = existingLicense, // <-- Truyền vào đây
            onDismiss = { showDrivingDialog = false },
            onSave = {
                onSaveDriving(it)
                showDrivingDialog = false
            }
        )
    }

    if (showHealthDialog) {
        HealthInsuranceDialog(
            citizenId = citizenId,
            existingItem = existingInsurance, // <-- Truyền vào đây
            onDismiss = { showHealthDialog = false },
            onSave = {
                onSaveHealth(it)
                showHealthDialog = false
            }
        )
    }
}

// ==========================================
// 1. DIALOG GIẤY ĐĂNG KÝ XE
// ==========================================
@Composable
fun VehicleRegisterDialog(
    citizenId: String,
    existingItem: VehicleRegister?,
    onDismiss: () -> Unit,
    onSave: (VehicleRegister) -> Unit
) {
    // State
    var id by remember { mutableStateOf(existingItem?.vehicleRegisterId ?: "") }
    var brand by remember { mutableStateOf(existingItem?.vehicleBrand ?: "") }
    var model by remember { mutableStateOf(existingItem?.vehicleModel ?: "") }
    var color by remember { mutableStateOf(existingItem?.vehicleColor ?: "") }
    var plate by remember { mutableStateOf(existingItem?.vehiclePlate ?: "") }
    var frame by remember { mutableStateOf(existingItem?.vehicleFrame ?: "") }
    var engine by remember { mutableStateOf(existingItem?.vehicleEngine ?: "") }
    var issueDate by remember { mutableStateOf(existingItem?.vehicleRegisterDate ?: "") }
    var expiredDate by remember { mutableStateOf(existingItem?.vehicleExpiredDate ?: "") }
    var place by remember { mutableStateOf(existingItem?.vehicleRegisterPlace ?: "") }
    var capacity by remember { mutableStateOf(existingItem?.vehicleCapacity ?: "") }

    BaseFormDialog(
        title = "Giấy đăng ký xe",
        onDismiss = onDismiss,
        onSave = {
            // Tạo object Entity
            val item = VehicleRegister(
                citizenId = citizenId,
                vehicleRegisterId = id,
                vehicleBrand = brand,
                vehicleModel = model,
                vehicleColor = color,
                vehiclePlate = plate,
                vehicleFrame = frame,
                vehicleEngine = engine,
                vehicleRegisterDate = issueDate,
                vehicleExpiredDate = expiredDate,
                vehicleRegisterPlace = place,
                vehicleCapacity = capacity
            )
            onSave(item)
        }
    ) {
        FormInput("Số đăng ký xe (ID)", id, { id = it }, Modifier.fillMaxWidth())
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
            DateInput("Ngày cấp", issueDate, { issueDate = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Số máy", engine, { engine = it }, Modifier.weight(1f))
            FormInput("Nơi cấp", place, { place = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Dung tích", capacity, { capacity = it }, Modifier.weight(1f))
            DateInput("Thời hạn", expiredDate, { expiredDate = it }, Modifier.weight(1f))
        }
    }
}

// ==========================================
// 2. DIALOG GIẤY PHÉP LÁI XE
// ==========================================
@Composable
fun DrivingLicenseDialog(
    citizenId: String,
    existingItem: DrivingLicense?,
    onDismiss: () -> Unit,
    onSave: (DrivingLicense) -> Unit
) {
    var id by remember { mutableStateOf(existingItem?.licenseId ?: "") }
    var level by remember { mutableStateOf(existingItem?.licenseLevel ?: "") }
    var issueDate by remember { mutableStateOf(existingItem?.createdAt ?: "") }
    var place by remember { mutableStateOf(existingItem?.createPlace ?: "") }
    var expiredDate by remember { mutableStateOf(existingItem?.expiredAt ?: "") }
    var createdBy by remember { mutableStateOf(existingItem?.createdBy ?: "") }

    BaseFormDialog(
        title = "Giấy phép lái xe",
        onDismiss = onDismiss,
        onSave = {
            val item = DrivingLicense(
                citizenId = citizenId,
                licenseId = id,
                licenseLevel = level,
                createdAt = issueDate,
                createPlace = place,
                expiredAt = expiredDate,
                createdBy = createdBy
            )
            onSave(item)
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Mã số thẻ", id, { id = it }, Modifier.weight(1f))
            FormInput("Hạng giấy phép", level, { level = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            DateInput("Ngày cấp", issueDate, { issueDate = it }, Modifier.weight(1f))
            FormInput("Nơi cấp", place, { place = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            DateInput("Thời hạn", expiredDate, { expiredDate = it }, Modifier.weight(1f))
            FormInput("Người cấp", createdBy, { createdBy = it }, Modifier.weight(1f))
        }
    }
}

// ==========================================
// 3. DIALOG BẢO HIỂM Y TẾ
// ==========================================
@Composable
fun HealthInsuranceDialog(
    citizenId: String,
    existingItem: HealthInsurance?,
    onDismiss: () -> Unit,
    onSave: (HealthInsurance) -> Unit
) {
    var id by remember { mutableStateOf(existingItem?.insuranceId ?: "") }
    var address by remember { mutableStateOf(existingItem?.address ?: "") }
    var issueDate by remember { mutableStateOf(existingItem?.createDate ?: "") }
    var expiredDate by remember { mutableStateOf(existingItem?.expiredDate ?: "") }
    var hospital by remember { mutableStateOf(existingItem?.examinationPlace ?: "") }

    BaseFormDialog(
        title = "Bảo hiểm y tế",
        onDismiss = onDismiss,
        onSave = {
            val item = HealthInsurance(
                citizenId = citizenId,
                insuranceId = id,
                address = address,
                createDate = issueDate,
                expiredDate = expiredDate,
                examinationPlace = hospital
            )
            onSave(item)
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FormInput("Mã số thẻ", id, { id = it }, Modifier.weight(1f))
            FormInput("Địa chỉ", address, { address = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            DateInput("Ngày cấp", issueDate, { issueDate = it }, Modifier.weight(1f))
            DateInput("Thời hạn", expiredDate, { expiredDate = it }, Modifier.weight(1f))
        }
        // Trường Full Width
        FormInput("Nơi đăng ký khám/chữa bệnh", hospital, { hospital = it }, Modifier.fillMaxWidth())
    }
}

// ==========================================
// CÁC COMPONENT HỖ TRỢ (HELPERS)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        onValueChange(formatter.format(Date(millis)))
                    }
                    showDatePicker = false
                }) { Text("Chọn", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Tái sử dụng FormInput nhưng thêm logic click
    FormInput(
        label = label,
        value = value,
        onValueChange = {}, // Readonly
        modifier = modifier,
        icon = Icons.Default.DateRange,
        readOnly = true,
        onClick = { showDatePicker = true }
    )
}

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
                .width(600.dp) // ↓ gọn hơn
                .shadow(8.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ===== TITLE =====
                Text(
                    text = title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )

                Spacer(Modifier.height(20.dp))

                // ===== FORM (SCROLLABLE) =====
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp) // giới hạn chiều cao
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }

                Spacer(Modifier.height(24.dp))

                // ===== BUTTONS =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorBtnCancelBg,
                            contentColor = ColorBtnCancelText
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(120.dp)
                            .height(40.dp)
                    ) {
                        Text("Thoát", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.width(32.dp))

                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorHeaderBg,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(120.dp)
                            .height(40.dp)
                    ) {
                        Text("Lưu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


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