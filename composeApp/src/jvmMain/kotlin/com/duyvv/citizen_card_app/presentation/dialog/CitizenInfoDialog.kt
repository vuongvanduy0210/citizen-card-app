package com.duyvv.citizen_card_app.presentation.dialog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.presentation.home.ActionButton
import com.duyvv.citizen_card_app.presentation.home.AvatarDisplay
import com.duyvv.citizen_card_app.presentation.home.InfoRow
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBgContainer
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBorderContainer
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorTextPrimary

@Composable
fun CitizenInfoDialog(
    citizen: Citizen,
    showActions: Boolean, // <--- THÊM THAM SỐ NÀY
    onDismiss: () -> Unit,
    onPinChangeClick: () -> Unit = {},
    onEditInfoClick: () -> Unit = {},
    onIntegratedDocumentClick: () -> Unit = {},
    onLockCardClick: () -> Unit = {},
    onUnlockCardClick: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(1000.dp)
                .padding(20.dp)
                .border(2.dp, ColorBorderContainer, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = ColorBgContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }

                Column(
                    modifier = Modifier.padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "THÔNG TIN CÔNG DÂN",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                    )

                    // Row 1: Thông tin (Luôn hiển thị)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(30.dp)
                    ) {
                        AvatarDisplay(citizen.avatar)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            InfoRow(label1 = "Mã công dân:", val1 = citizen.citizenId, label2 = "Họ tên:", val2 = citizen.fullName)
                            InfoRow(label1 = "Ngày sinh:", val1 = citizen.birthDate, label2 = "Giới tính:", val2 = citizen.gender)
                            InfoRow(label1 = "Địa chỉ thường trú:", val1 = citizen.address, label2 = "Quê quán:", val2 = citizen.hometown)
                            InfoRow(label1 = "Dân tộc:", val1 = citizen.ethnicity, label2 = "Quốc tịch:", val2 = citizen.nationality)
                            InfoRow(label1 = "Đặc điểm nhận dạng:", val1 = citizen.identification, label2 = "Tôn giáo:", val2 = citizen.religion)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- Row 2: Action Buttons (CHỈ HIỆN KHI LÀ THẺ CỦA MÌNH) ---
                    if (showActions) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ActionButton("Đặt lại mã PIN", onPinChangeClick)
                            ActionButton("Chỉnh sửa", onEditInfoClick)
                            ActionButton("Giấy tờ tích hợp", onIntegratedDocumentClick)
                            ActionButton("Khóa thẻ", onLockCardClick)
                            ActionButton("Mở khóa thẻ", onUnlockCardClick)
                        }
                    } else {
                        // (Tùy chọn) Hiển thị thông báo nhỏ nếu chỉ đang xem
                        Text(
                            text = "* Chỉ xem thông tin (Vui lòng kết nối thẻ để thao tác)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}