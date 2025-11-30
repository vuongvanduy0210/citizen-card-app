package com.duyvv.citizen_card_app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBgContainer
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBgRoot
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBorderContainer
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorFieldBg
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorFieldBorder
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorLabelText
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun HomeTabContentPreview() {
    CitizenInfoScreen()
}

@Composable
fun CitizenInfoScreen(
    citizen: Citizen? = null,
    onPinChangeClick: () -> Unit = {},
    onEditInfoClick: () -> Unit = {},
    onIntegratedDocumentClick: () -> Unit = {},
    onLockCardClick: () -> Unit = {},
    onUnlockCardClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBgRoot),
        contentAlignment = Alignment.Center
    ) {
        // --- Main Container ---
        Card(
            modifier = Modifier
                .width(900.dp)
                .padding(20.dp)
                .border(2.dp, ColorBorderContainer, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = ColorBgContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- Row 1: Avatar and Fields ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(30.dp)
                ) {
                    AvatarDisplay(citizen?.avatar)

                    // 2. FORM SECTION
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        InfoRow(
                            label1 = "Mã công dân:", val1 = citizen?.citizenId,
                            label2 = "Họ tên:", val2 = citizen?.fullName
                        )
                        InfoRow(
                            label1 = "Ngày sinh:", val1 = citizen?.birthDate,
                            label2 = "Giới tính:", val2 = citizen?.gender
                        )
                        InfoRow(
                            label1 = "Địa chỉ thường trú:", val1 = citizen?.address,
                            label2 = "Quê quán:", val2 = citizen?.hometown
                        )
                        InfoRow(
                            label1 = "Dân tộc:", val1 = citizen?.ethnicity,
                            label2 = "Quốc tịch:", val2 = citizen?.nationality
                        )
                        InfoRow(
                            label1 = "Đặc điểm nhận dạng:", val1 = citizen?.identification,
                            label2 = "Tôn giáo:", val2 = citizen?.religion
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- Row 2: Action Buttons ---
                Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButton("Đổi mã PIN", onPinChangeClick)
                    ActionButton("Chỉnh sửa", onEditInfoClick)
                    ActionButton("Giấy tờ tích hợp", onIntegratedDocumentClick)
                    ActionButton("Khóa thẻ", onLockCardClick)
                    ActionButton("Mở khóa thẻ", onUnlockCardClick)
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label1: String, val1: String?,
    label2: String, val2: String?
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Cột trái
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label1,
                color = ColorLabelText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(140.dp)
            )
            // Thay thế bằng hàm InfoValueBox mới
            InfoValueBox(
                value = val1 ?: "",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(30.dp))

        // Cột phải
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label2,
                color = ColorLabelText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(80.dp)
            )
            // Thay thế bằng hàm InfoValueBox mới
            InfoValueBox(
                value = val2 ?: "",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Composable mới: Dùng Box thay vì TextField nhưng giữ nguyên style
@Composable
fun InfoValueBox(value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(45.dp) // Chiều cao chuẩn cho ô hiển thị (nhỏ hơn input 1 chút cho gọn)
            .border(1.dp, ColorFieldBorder, RoundedCornerShape(4.dp)) // Viền
            .background(ColorFieldBg, RoundedCornerShape(4.dp)) // Màu nền
            .padding(horizontal = 12.dp), // Padding nội dung bên trong
        contentAlignment = Alignment.CenterStart // Căn chữ sang trái, giữa dòng
    ) {
        Text(
            text = value,
            color = Color.Black,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis // Nếu dài quá thì hiện dấu ...
        )
    }
}