package com.duyvv.citizen_card_app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorAvatarBg
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBgContainer
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorBorderContainer
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorTextPrimary
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorTextSecondary
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun EmptyCardScreenPreview() {
    EmptyCardScreen(onCreateInfoClick = {})
}

@Composable
fun EmptyCardScreen(
    onCreateInfoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBgContainer),
        contentAlignment = Alignment.Center
    ) {
        // --- Main Container (VBox) ---
        Card(
            modifier = Modifier
                .width(600.dp) // Kích thước vừa phải, gọn gàng hơn 800px gốc
                .padding(20.dp)
                .border(2.dp, ColorBorderContainer, RoundedCornerShape(12.dp)), // Bo góc mềm mại hơn (12.dp)
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ColorBgContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 50.dp, horizontal = 20.dp), // Padding thoáng hơn
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp) // Khoảng cách giữa các phần tử
            ) {
                // 1. Placeholder Icon (Thay cho ImageView trống)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(ColorAvatarBg)
                        .border(2.dp, Color(0xFFCCCCCC), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCardOff, // Icon thẻ bị gạch chéo/trống
                        contentDescription = "Empty Card",
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }

                // 2. Text Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Thẻ trắng!",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Thẻ hiện chưa có dữ liệu công dân.\nVui lòng tạo thông tin mới để tiếp tục.",
                        fontSize = 16.sp,
                        color = ColorTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Create Button
                Button(
                    onClick = onCreateInfoClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF263238), // Màu tối chủ đạo của app (HeaderBg)
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(50.dp).width(200.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Tạo thông tin",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}