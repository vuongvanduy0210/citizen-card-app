package com.duyvv.citizen_card_app.presentation.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.duyvv.citizen_card_app.presentation.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun ChangePinDialogPreview() {
    ChangePinDialog(
        isChangePin = true, // Thử chế độ đổi PIN
        onDismiss = {},
        onConfirm = { oldPin, newPin ->
            println("Old: $oldPin, New: $newPin")
        }
    )
}

@Composable
fun ChangePinDialog(
    isChangePin: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    // State lưu giá trị các ô PIN
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    // State báo lỗi
    var errorMessage by remember { mutableStateOf("") }

    // Hàm validate và submit dùng chung
    fun validateAndSubmit() {
        errorMessage = "" // Reset lỗi trước khi kiểm tra

        // 1. Kiểm tra Rỗng
        if (isChangePin && oldPin.isBlank()) {
            errorMessage = "Vui lòng nhập mã PIN cũ"
            return
        }
        if (newPin.isBlank()) {
            errorMessage = "Vui lòng nhập mã PIN mới"
            return
        }
        if (confirmPin.isBlank()) {
            errorMessage = "Vui lòng nhập lại mã PIN mới"
            return
        }

        // 2. Kiểm tra Độ dài (Phải đủ 6 số)
        if (isChangePin && oldPin.length < 6) {
            errorMessage = "Mã PIN cũ phải đủ 6 ký tự"
            return
        }
        if (newPin.length < 6) {
            errorMessage = "Mã PIN mới phải đủ 6 ký tự"
            return
        }

        // 3. Kiểm tra Logic nghiệp vụ
        if (newPin != confirmPin) {
            errorMessage = "Mã PIN xác nhận không khớp"
            return
        }

        // Kiểm tra trùng PIN cũ (chỉ khi đang đổi PIN)
        if (isChangePin && oldPin == newPin) {
            errorMessage = "Mã PIN mới không được trùng với mã PIN cũ"
            return
        }

        // Nếu tất cả hợp lệ -> Gửi callback
        onConfirm(oldPin, newPin)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(420.dp)
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
                // --- TITLE ---
                Text(
                    text = if (isChangePin) "Đổi mã PIN" else "Thiết lập mã PIN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )

                // --- INPUT FIELDS ---
                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    // 1. Ô nhập mã PIN cũ (Chỉ hiện khi isChangePin = true)
                    if (isChangePin) {
                        PinInputField(
                            value = oldPin,
                            onValueChange = { if (it.length <= 6) oldPin = it },
                            placeholder = "Nhập mã PIN cũ",
                            imeAction = ImeAction.Next
                        )
                    }

                    // 2. Ô nhập mã PIN mới
                    PinInputField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6) newPin = it },
                        placeholder = "Nhập mã PIN mới",
                        imeAction = ImeAction.Next
                    )

                    // 3. Ô nhập lại mã PIN mới (Action Done -> Submit luôn)
                    PinInputField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 6) confirmPin = it },
                        placeholder = "Nhập lại mã PIN mới",
                        imeAction = ImeAction.Done,
                        onDone = { validateAndSubmit() }
                    )
                }

                // Hiển thị lỗi nếu có
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = ColorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                // --- BUTTONS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Nút Thoát
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorBtnCancelBg,
                            contentColor = ColorBtnCancelText
                        )
                    ) {
                        Text("Thoát", fontWeight = FontWeight.Bold)
                    }

                    // Nút Xác nhận
                    Button(
                        onClick = { validateAndSubmit() },
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorHeaderBg,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Xác nhận", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {}
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.any { char -> !char.isDigit() }) return@OutlinedTextField
            onValueChange.invoke(it)
        },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = ColorTextSecondary,
                fontSize = 16.sp
            )
        },
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            color = ColorInputText
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ColorInputBg,
            unfocusedContainerColor = ColorInputBg,
            focusedBorderColor = ColorInputBorder,
            unfocusedBorderColor = ColorInputBorder,
            cursorColor = ColorInputText
        ),
        // 3. Thêm nút con mắt (Trailing Icon)
        trailingIcon = {
            val image = if (isPasswordVisible)
                Icons.Filled.Visibility
            else
                Icons.Filled.VisibilityOff

            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = image,
                    contentDescription = if (isPasswordVisible) "Hide PIN" else "Show PIN",
                    tint = ColorTextSecondary
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}