package com.duyvv.citizen_card_app.presentation.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorHeaderBg

@Composable
fun AdminResetPinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit // Trả về (PUK, NewPIN)
) {
    var pukCode by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RESET PIN (ADMIN)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorHeaderBg
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Ô nhập PUK
                OutlinedTextField(
                    value = pukCode,
                    onValueChange = { if (it.length <= 8) pukCode = it },
                    label = { Text("Nhập mã PUK (8 số)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ô nhập PIN mới
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6) newPin = it },
                    label = { Text("Nhập PIN mới (6 số)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (pukCode.length != 8) {
                                error = "Mã PUK phải có 8 ký tự"
                            } else if (newPin.length != 6) {
                                error = "Mã PIN phải có 6 ký tự"
                            } else {
                                onConfirm(pukCode, newPin)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorHeaderBg)
                    ) {
                        Text("Xác nhận")
                    }
                }
            }
        }
    }
}