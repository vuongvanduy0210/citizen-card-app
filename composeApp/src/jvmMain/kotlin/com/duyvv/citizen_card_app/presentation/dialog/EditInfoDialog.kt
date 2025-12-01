package com.duyvv.citizen_card_app.presentation.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.UploadFile
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
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.domain.model.ImageFile
import com.duyvv.citizen_card_app.presentation.ui.theme.*
import com.duyvv.citizen_card_app.utils.pickImageFromSystem
import java.text.SimpleDateFormat
import java.util.*

@Composable
@Preview
fun preview2() {
    EditInfoDialog(null, {}, {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInfoDialog(
    citizen: Citizen? = null,
    onDismiss: () -> Unit,
    onSave: (Citizen) -> Unit
) {
    // State cho các trường nhập liệu
    var name by remember { mutableStateOf(citizen?.fullName ?: "") }
    var gender by remember { mutableStateOf(citizen?.gender ?: "Nam") }
    var birthDate by remember { mutableStateOf(citizen?.birthDate ?: "") }
    var hometown by remember { mutableStateOf(citizen?.hometown ?: "") }
    var address by remember { mutableStateOf(citizen?.address ?: "") }
    var nationality by remember { mutableStateOf(citizen?.nationality ?: "") }
    var ethnicity by remember { mutableStateOf(citizen?.ethnicity ?: "") }
    var religion by remember { mutableStateOf(citizen?.religion ?: "") }
    var identification by remember { mutableStateOf(citizen?.identification ?: "") }

    // Avatar state
    var newAvatar by remember {
        mutableStateOf(
            citizen?.avatar?.let {
                ImageFile(bytes = it, "avatar")
            }
        )
    }

    // DatePicker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // 1. State lưu thông báo lỗi
    var errorMessage by remember { mutableStateOf("") }

    // Logic hiển thị DatePicker
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        birthDate = convertMillisToDate(millis)
                    }
                    showDatePicker = false
                }) {
                    Text("Chọn", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(680.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- TITLE ---
                Text(
                    text = if (citizen == null) "Thêm mới công dân" else "Chỉnh sửa thông tin",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )

                // --- FORM FIELDS ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Row 1
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        FormInput("Họ tên", name, { name = it }, Modifier.weight(1f))
                        FormDropdown(
                            "Giới tính",
                            gender,
                            listOf("Nam", "Nữ", "Khác"),
                            { gender = it },
                            Modifier.weight(1f)
                        )
                    }

                    // Row 2
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        FormInput(
                            label = "Ngày sinh",
                            value = birthDate,
                            onValueChange = {},
                            icon = Icons.Default.CalendarToday,
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            onClick = { showDatePicker = true }
                        )
                        FormInput("Quê quán", hometown, { hometown = it }, Modifier.weight(1f))
                    }

                    // Row 3
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        FormInput("Địa chỉ", address, { address = it }, Modifier.weight(1f))
                        FormInput("Quốc tịch", nationality, { nationality = it }, Modifier.weight(1f))
                    }

                    // Row 4
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        FormInput("Dân tộc", ethnicity, { ethnicity = it }, Modifier.weight(1f))
                        FormInput("Tôn giáo", religion, { religion = it }, Modifier.weight(1f))
                    }

                    // Row 5
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Avatar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorTextPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Button(
                                onClick = {
                                    val picked = pickImageFromSystem()
                                    if (picked != null) newAvatar = picked
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (newAvatar != null || citizen?.avatar != null) ColorGreen else Color(
                                        0xFF2196F3
                                    )
                                ),
                                shape = RoundedCornerShape(5.dp),
                                modifier = Modifier.height(55.dp).fillMaxWidth()
                            ) {
                                if (newAvatar != null) {
                                    Icon(Icons.Default.Check, null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text(newAvatar!!.name, color = Color.White, maxLines = 1)
                                } else if (citizen?.avatar != null) {
                                    Icon(Icons.Default.Check, null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Đã có ảnh", color = Color.White)
                                } else {
                                    Icon(Icons.Default.UploadFile, null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Chọn File", color = Color.White)
                                }
                            }
                        }
                        FormInput("Đặc điểm nhận dạng", identification, { identification = it }, Modifier.weight(1f))
                    }
                }

                // 2. Hiển thị thông báo lỗi nếu có (Animation)
                AnimatedVisibility(
                    visible = errorMessage.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(if (errorMessage.isNotEmpty()) 5.dp else 10.dp))

                // --- ACTION BUTTONS ---
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
                        modifier = Modifier.width(120.dp).height(40.dp)
                    ) {
                        Text("Thoát", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.width(40.dp))

                    Button(
                        onClick = {
                            // 3. Logic Validate
                            if (name.isBlank() || gender.isBlank() || birthDate.isBlank() ||
                                hometown.isBlank() || address.isBlank() || nationality.isBlank() ||
                                ethnicity.isBlank() || religion.isBlank() || identification.isBlank()
                            ) {
                                errorMessage = "Vui lòng nhập đầy đủ thông tin!"
                                return@Button
                            }

                            // Nếu đang tạo mới (citizen == null) mà chưa chọn ảnh -> báo lỗi
                            // Nếu đang sửa (citizen != null) -> có thể dùng ảnh cũ
                            if (citizen == null && newAvatar == null) {
                                errorMessage = "Vui lòng chọn ảnh đại diện!"
                                return@Button
                            }

                            // Validate OK -> Xóa lỗi và Lưu
                            errorMessage = ""
                            val finalCitizen = Citizen(
                                citizenId = citizen?.citizenId ?: "123456789121",
                                fullName = name,
                                gender = gender,
                                birthDate = birthDate,
                                address = address,
                                hometown = hometown,
                                nationality = nationality,
                                ethnicity = ethnicity,
                                religion = religion,
                                identification = identification,
                                avatar = newAvatar?.bytes ?: citizen?.avatar
                            )
                            onSave(finalCitizen)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorHeaderBg,
                            contentColor = Color.White
                        ),
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

// ... (Giữ nguyên các hàm convertMillisToDate, FormInput, FormDropdown)
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Composable
fun FormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ColorTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val interactionSource = remember { MutableInteractionSource() }

        if (onClick != null) {
            LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Release) {
                        onClick()
                    }
                }
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = ColorInputBorder,
                unfocusedBorderColor = ColorInputBorder,
                cursorColor = ColorInputText
            ),
            trailingIcon = if (icon != null) {
                {
                    IconButton(onClick = { onClick?.invoke() }) {
                        Icon(icon, contentDescription = null, tint = ColorTextSecondary)
                    }
                }
            } else null,
            readOnly = readOnly,
            interactionSource = interactionSource
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ColorTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = ColorInputBorder,
                    unfocusedBorderColor = ColorInputBorder
                ),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}