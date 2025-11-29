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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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

@Preview
@Composable
fun CitizenItemPreview() {
    // Giả lập dữ liệu để xem trước 1 dòng
    EnterPinDialog(
        label = "Enter Pin",
        hint = "ggggg ",
        leftLabel = "Thoát",
        rightLabel = "Xác nhận",
        onClickLeftBtn = {},
        onClickRightBtn = {}
    )
}

@Composable
fun EnterPinDialog(
    label: String,
    hint: String,
    leftLabel: String,
    rightLabel: String,
    onClickLeftBtn: () -> Unit,
    onClickRightBtn: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    // 1. Biến trạng thái để kiểm soát việc ẩn/hiện mật khẩu
    var isPasswordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onClickLeftBtn() },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp, horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {

                // --- Labels ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nhập mã PIN",
                        color = ColorTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )

                    Text(
                        text = label,
                        color = ColorTextSecondary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                // --- Password Field ---
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.any { char -> !char.isDigit() }) return@OutlinedTextField
                        if (it.length <= 6) pin = it
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = hint,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        color = ColorInputText,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.SansSerif
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(55.dp), // Tăng nhẹ height lên 55dp để thoáng hơn

                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ColorInputBg,
                        unfocusedContainerColor = ColorInputBg,
                        focusedBorderColor = ColorInputBorder,
                        unfocusedBorderColor = ColorInputBorder,
                        cursorColor = ColorInputText
                    ),
                    trailingIcon = {
                        val image = if (isPasswordVisible)
                            Icons.Filled.Visibility
                        else
                            Icons.Filled.VisibilityOff

                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = if (isPasswordVisible) "Hide PIN" else "Show PIN",
                                tint = Color.Gray
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onClickRightBtn(pin) }
                    )
                )

                // --- Buttons ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Button(
                        onClick = onClickLeftBtn,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorBtnCancelBg,
                            contentColor = ColorBtnCancelText
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = leftLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onClickRightBtn(pin) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorHeaderBg,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = rightLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}