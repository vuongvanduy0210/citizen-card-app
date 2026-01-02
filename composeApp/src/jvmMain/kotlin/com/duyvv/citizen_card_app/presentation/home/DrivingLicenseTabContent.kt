package com.duyvv.citizen_card_app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorGreen
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorHeaderBg
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorRed
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorTextSecondary
import org.koin.compose.koinInject

@Composable
fun DrivingLicenseTabContent(isCardConnected: Boolean) {
    val viewModel = koinInject<HomeViewModel>() // Hoặc koinViewModel()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.cardInfo) {
        if (uiState.cardInfo != null) {
            viewModel.loadLicensesFromCard()
        }
    }

    if (!isCardConnected) {
        ConnectCardPlaceholder()
        return
    }

    // 2. Nếu đã kết nối nhưng chưa có thông tin công dân (Thẻ trắng) -> Hiện màn hình tạo mới
    if (uiState.cardInfo == null) {
        EmptyCardScreen(onCreateInfoClick = { viewModel.showCreateInfoDialog(true) })
        return
    }

    // Giao diện chính: Chỉ hiện danh sách
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("DANH SÁCH GIẤY PHÉP LÁI XE", fontWeight = FontWeight.Bold, color = ColorHeaderBg)
        Spacer(Modifier.height(16.dp))

        if (uiState.cardLicenses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có dữ liệu trên thẻ", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.cardLicenses) { item ->
                    LicenseItemRow(item) {
                        viewModel.selectLicenseAndShowDetail(item)
                    }
                }
            }
        }
    }

    // Dialog Chi tiết (Popup khi click vào item)
    if (uiState.isShowLicenseDetailDialog && uiState.selectedLicense != null) {
        LicenseDetailDialog(
            item = uiState.selectedLicense!!,
            onDismiss = { viewModel.dismissLicenseDetail() },
            onDeductPoints = { viewModel.deductPoints(it) },
            onReset = { viewModel.resetCurrentLicense() }
        )
    }
}

@Composable
fun LicenseItemRow(item: LicenseUiItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(50.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.rank, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = ColorHeaderBg)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Số bằng: ${item.licenseId}", fontWeight = FontWeight.Bold)
                Text("Hết hạn: ${item.expiration}", fontSize = 14.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${item.score}/12", fontWeight = FontWeight.Bold,
                    color = if(item.isRevoked) ColorRed else ColorGreen, fontSize = 18.sp)
                if(item.isRevoked) Text("ĐÃ KHÓA", color = ColorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// File: DrivingLicenseTabContent.kt

@Composable
fun LicenseDetailDialog(
    item: LicenseUiItem,
    onDismiss: () -> Unit,
    onDeductPoints: (Int) -> Unit,
    onReset: () -> Unit
) {
    // State cho ô nhập điểm
    var pointsInput by remember { mutableStateOf("") }
    // State lưu thông báo lỗi
    var inputError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(450.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CHI TIẾT & XỬ LÝ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Spacer(Modifier.height(16.dp))

                // Visual Card
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFFFBC02D), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("GPLX / DRIVING LICENSE", fontSize = 12.sp, color = ColorRed, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(item.licenseId, fontSize = 22.sp, fontWeight = FontWeight.Black, color = ColorRed)
                        Text("Hạng: ${item.rank}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.weight(1f))
                        Text("Có giá trị đến: ${item.expiration}", fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Điểm số
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { item.score / 12f },
                            modifier = Modifier.size(90.dp),
                            color = if (item.isRevoked) ColorRed else ColorGreen,
                            strokeWidth = 8.dp
                        )
                        Text("${item.score}", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Điểm hiện tại", color = Color.Gray, fontSize = 14.sp)
                        if (item.isRevoked) {
                            Text("ĐÃ BỊ TƯỚC", color = ColorRed, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Đang hoạt động", color = ColorGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Khu vực xử lý vi phạm
                if (item.isRevoked) {
                    Text(
                        "Bằng lái này đã bị tước quyền sử dụng.",
                        color = ColorRed,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Button(
                        onClick = onReset,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorHeaderBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Khôi phục điểm (Mở khóa)")
                    }
                } else {
                    // --- Ô NHẬP LIỆU CÓ KIỂM TRA LỖI ---
                    OutlinedTextField(
                        value = pointsInput,
                        onValueChange = {
                            // Chỉ cho nhập số
                            if (it.all { char -> char.isDigit() }) {
                                pointsInput = it
                                inputError = null // Xóa lỗi khi người dùng sửa lại
                            }
                        },
                        label = { Text("Nhập số điểm trừ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        isError = inputError != null, // Đổi màu đỏ nếu có lỗi
                        supportingText = { // Hiển thị dòng text báo lỗi bên dưới
                            if (inputError != null) {
                                Text(
                                    text = inputError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Nút Trừ điểm (Theo input)
                        Button(
                            onClick = {
                                val p = pointsInput.toIntOrNull()
                                if (p != null) {
                                    // --- KIỂM TRA LOGIC TẠI ĐÂY ---
                                    if (p > item.score) {
                                        inputError = "Không thể trừ quá số điểm hiện có (${item.score})"
                                    } else if (p <= 0) {
                                        inputError = "Số điểm phải lớn hơn 0"
                                    } else {
                                        onDeductPoints(p)
                                        pointsInput = "" // Reset ô nhập
                                        inputError = null
                                    }
                                } else {
                                    inputError = "Vui lòng nhập số điểm"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726)),
                            modifier = Modifier.weight(1f),
                            // enabled = pointsInput.isNotEmpty() // Có thể bỏ dòng này để cho phép bấm và hiện lỗi
                        ) {
                            Text("Trừ điểm")
                        }

                        // Nút Tước bằng nhanh (Trừ 12 điểm)
                        Button(
                            onClick = { onDeductPoints(12) },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tước bằng")
                        }
                    }

                    TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text("Reset về 12 điểm (Test)", color = Color.Gray)
                    }
                }
            }
        }
    }
}

// --- Màn hình chưa kết nối thẻ (Tách ra cho gọn) ---
@Composable
fun ConnectCardPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = "Connect Card",
                modifier = Modifier.size(150.dp),
                tint = ColorTextSecondary.copy(alpha = 0.5f)
            )
            Text(
                text = "Vui lòng kết nối thẻ để xem dữ liệu",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ColorTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}