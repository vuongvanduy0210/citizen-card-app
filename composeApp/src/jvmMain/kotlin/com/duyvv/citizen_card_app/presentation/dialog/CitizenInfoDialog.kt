package com.duyvv.citizen_card_app.presentation.dialog

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.presentation.home.ActionButton
import com.duyvv.citizen_card_app.presentation.home.AvatarDisplay
import com.duyvv.citizen_card_app.presentation.home.InfoRow
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBgContainer
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBgRoot
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBorderContainer

@Composable
fun CitizenInfoDialog(
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
                .width(1000.dp)
                .padding(100.dp)
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