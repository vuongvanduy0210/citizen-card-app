package com.duyvv.citizen_card_app.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duyvv.citizen_card_app.domain.ApplicationState
import com.duyvv.citizen_card_app.presentation.dialog.EnterPinDialog
import com.duyvv.citizen_card_app.presentation.ui.theme.*
import kotlinx.coroutines.flow.combine
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

//import org.koin.compose.viewmodel.koinViewModel


@Composable
@Preview
fun MainScreen() {
    // Material 3 Theme Wrapper (Nên bao bọc app trong này)
    val viewModel = koinViewModel<HomeViewModel>()
    val isCardConnected by
    combine(ApplicationState.isCardVerified, ApplicationState.isCardInserted) { isCardInserted, isCardVerified ->
        isCardInserted && isCardVerified
    }.collectAsStateWithLifecycle(false)
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = ColorHeaderBg,
            background = ColorBackground,
            surface = Color.White
        )
    ) {
        SystemManagerApp(
            isCardConnected = isCardConnected,
            onClickConnectCard = {
                if (isCardConnected) {
                    viewModel.disconnectCard()
                } else {
                    viewModel.showPinDialog(true)
                }
            }
        )
        if (uiState.isShowPinDialog) {
            EnterPinDialog(
                label = "Nhập mã pin (6 ký tự)",
                hint = "Mã pin",
                leftLabel = "Huỷ",
                rightLabel = "Xác nhận",
                onClickLeftBtn = {
                    viewModel.showPinDialog(false)
                },
                onClickRightBtn = { pinCode ->
                    viewModel.connectCard(pinCode)
                }
            )
        }
    }
}

@Composable
fun SystemManagerApp(
    isCardConnected: Boolean,
    onClickConnectCard: () -> Unit = {},
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { AppHeader(isCardConnected, onClickConnectCard) },
        containerColor = ColorBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = ColorHeaderBg,
                divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f)) }
            ) {
                TabItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = "🏠 Trang chủ"
                )
                TabItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = "👥 Danh sách công dân"
                )
            }

            // --- TAB CONTENT ---
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> HomeTabContent()
                    1 -> ManageCitizenTabContent()
                }
            }
        }
    }
}

// --- 1. HEADER COMPONENT ---
@Composable
fun AppHeader(isCardConnected: Boolean, onClickConnectCard: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(ColorHeaderBg)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo / Title
        Text(
            text = "🛡 SYSTEM MANAGER",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Right Side Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Administrator",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFECEFF1)
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("●", color = ColorGreen, fontSize = 10.sp)
                    Text("Online", color = ColorGreen, style = MaterialTheme.typography.labelSmall)
                }
            }

            // Avatar Icon
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )

            // Divider Vertical
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = Color.Gray.copy(alpha = 0.5f)
            )

            // Buttons
            Button(
                onClick = {
                    onClickConnectCard.invoke()
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isCardConnected) ColorOrange else ColorRed),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isCardConnected) {
                        "Ngắt kết nối thẻ"
                    } else {
                        "Kết nối thẻ"
                    }, fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = { /* TODO: Logout Logic */ },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorRed),
                border = BorderStroke(1.dp, ColorRed),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Đăng xuất")
            }
        }
    }
}

// --- 2. HOME TAB ---
@Composable
fun HomeTabContent() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Placeholder Image (Dùng Icon lớn thay cho ảnh)
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = "Insert Card",
                modifier = Modifier.size(180.dp),
                tint = ColorTextSecondary.copy(alpha = 0.5f)
            )

            Text(
                text = "HỆ THỐNG QUẢN LÝ THẺ CÔNG DÂN",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
            )

            Text(
                text = "Vui lòng kết nối thẻ để tiếp tục thao tác",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ColorTextSecondary
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class) // Cho FlowRow
@Composable
fun ManageCitizenTabContent() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- FILTER SECTION (Card) ---
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "BỘ LỌC TÌM KIẾM",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorTextPrimary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    M3TextField(placeholder = "Mã công dân", width = 160.dp, icon = Icons.Default.Search)
                    M3TextField(placeholder = "Họ và tên", width = 180.dp, icon = Icons.Default.Person)

                    // Giả lập ComboBox Giới tính
                    M3DropdownGender(width = 130.dp)

                    M3TextField(placeholder = "Quê quán", width = 180.dp, icon = Icons.Default.Home)

                    // Giả lập DatePicker
                    M3TextField(
                        placeholder = "Ngày sinh",
                        width = 160.dp,
                        icon = Icons.Default.DateRange,
                        readOnly = true
                    )

                    // Action Buttons
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = ColorHeaderBg),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tìm kiếm")
                    }

                    FilledTonalButton(
                        onClick = {},
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFCFD8DC),
                            contentColor = ColorTextPrimary
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Làm mới")
                    }
                }
            }
        }

        // --- TABLE SECTION ---
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableTextCell("Mã công dân", 1.2f, isHeader = true)
                    TableTextCell("Họ tên", 2f, isHeader = true, alignLeft = true)
                    TableTextCell("Ngày sinh", 1.3f, isHeader = true)
                    TableTextCell("Giới tính", 1f, isHeader = true)
                    TableTextCell("Quê quán", 2f, isHeader = true, alignLeft = true)
                    TableTextCell("Thao tác", 1.2f, isHeader = true)
                }

                HorizontalDivider()

                // Table Content (LazyColumn)
                LazyColumn {
                    items(15) { index ->
                        TableRowItem(index)
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun TabItem(selected: Boolean, onClick: () -> Unit, text: String) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                color = if (selected) ColorHeaderBg else Color.Gray
            )
        }
    )
}

@Composable
fun M3TextField(
    placeholder: String,
    width: androidx.compose.ui.unit.Dp,
    icon: ImageVector? = null,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        leadingIcon = if (icon != null) {
            { Icon(icon, contentDescription = null, tint = Color.Gray) }
        } else null,
        modifier = Modifier.width(width).height(50.dp), // Height nhỏ hơn mặc định chút
        singleLine = true,
        readOnly = readOnly,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = Color.LightGray
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3DropdownGender(width: androidx.compose.ui.unit.Dp) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Giới tính") }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.width(width).height(50.dp)
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            listOf("Nam", "Nữ", "Khác").forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOption = option
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RowScope.TableTextCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    alignLeft: Boolean = false
) {
    Text(
        text = text,
        style = if (isHeader) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) ColorTextPrimary else Color.Black,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        textAlign = if (alignLeft) TextAlign.Start else TextAlign.Center
    )
}

@Composable
fun TableRowItem(index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle row click */ }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableTextCell("012345678$index", 1.2f)
        TableTextCell("Nguyễn Văn A $index", 2f, alignLeft = true)
        TableTextCell("20/10/1995", 1.3f)
        TableTextCell(if (index % 2 == 0) "Nam" else "Nữ", 1f)
        TableTextCell("Hà Nội, Việt Nam", 2f, alignLeft = true)

        // Actions Column
        Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallFloatingActionButton(
                    onClick = {},
                    containerColor = Color.White,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = ColorOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
                SmallFloatingActionButton(
                    onClick = {},
                    containerColor = Color.White,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ColorRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}