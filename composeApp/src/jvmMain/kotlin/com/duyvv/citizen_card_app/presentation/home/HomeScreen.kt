package com.duyvv.citizen_card_app.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.domain.ApplicationState
import com.duyvv.citizen_card_app.presentation.dialog.ChangePinDialog
import com.duyvv.citizen_card_app.presentation.dialog.EditInfoDialog
import com.duyvv.citizen_card_app.presentation.dialog.EnterPinDialog
import com.duyvv.citizen_card_app.presentation.dialog.NoticeDialog
import com.duyvv.citizen_card_app.presentation.ui.theme.*
import kotlinx.coroutines.flow.combine
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.skia.Image
import org.koin.compose.viewmodel.koinViewModel


@Composable
@Preview
fun MainScreen() {
    // Material 3 Theme Wrapper
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
        // Dùng Box để xếp chồng các lớp (App chính ở dưới, Dialog animation ở trên)
        Box(modifier = Modifier.fillMaxSize()) {
            SystemManagerApp(
                citizen = uiState.cardInfo,
                isCardConnected = isCardConnected,
                onClickConnectCard = {
                    if (isCardConnected) {
                        viewModel.disconnectCard()
                    } else {
                        viewModel.showPinDialog(true)
                    }
                },
                onClickCreateInfo = {
                    viewModel.showEditInfoDialog(true)
                }
            )

            // --- 0. SCRIM (Lớp phủ mờ nền) ---
            // Hiển thị khi bất kỳ dialog nào đang mở
            val isAnyDialogVisible = uiState.isShowPinDialog ||
                    uiState.isShowErrorPinCodeDialog ||
                    uiState.isShowNoticeDialog ||
                    uiState.isShowEditInfoDialog

            AnimatedVisibility(
                visible = isAnyDialogVisible,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)) // Màu đen mờ 40%
                        // Chặn click xuống màn hình dưới
                        .clickable(enabled = false) {}
                )
            }

            // --- 1. PIN Dialog Animation (Spring Pop-up) ---
            // Hiệu ứng nảy (Bouncy) khi hiện ra
            AnimatedVisibility(
                visible = uiState.isShowPinDialog,
                enter = fadeIn(tween(200)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut(tween(0)) + scaleOut(targetScale = 0.9f)
            ) {
                // Để Dialog hiển thị chính giữa màn hình (nếu không dùng Window Dialog)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

            // --- 2. Error PIN Dialog Animation ---
            AnimatedVisibility(
                visible = uiState.isShowErrorPinCodeDialog,
                enter = fadeIn(tween(200)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ),
                exit = fadeOut(tween(0)) + scaleOut(targetScale = 0.95f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EnterPinDialog(
                        label = "Bạn đã nhập sai mã PIN, vui lòng thử lại",
                        hint = "Mã pin",
                        leftLabel = "Huỷ",
                        rightLabel = "Xác nhận",
                        onClickLeftBtn = {
                            viewModel.showErrorPinCodeDialog(false)
                        },
                        onClickRightBtn = { pinCode ->
                            viewModel.verifyPinCard(pinCode)
                        }
                    )
                }
            }

            // --- 3. Notice Dialog Animation ---
            AnimatedVisibility(
                visible = uiState.isShowNoticeDialog,
                enter = fadeIn(tween(200)) + slideInVertically(
                    initialOffsetY = { -40 }, // Trượt nhẹ từ trên xuống
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                exit = fadeOut(tween(150))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    NoticeDialog(
                        message = uiState.noticeMessage,
                        textButton = "OK"
                    ) {
                        viewModel.showNoticeDialog(false)
                    }
                }
            }

            // --- 4. Edit Info Dialog Animation (Slide Up + Expand) ---
            AnimatedVisibility(
                visible = uiState.isShowEditInfoDialog,
                enter = fadeIn(tween(300)) + slideInVertically(
                    initialOffsetY = { it }, // Trượt từ đáy màn hình lên
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut(tween(200)) + slideOutVertically(
                    targetOffsetY = { it }, // Trượt xuống đáy
                    animationSpec = tween(200)
                )
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EditInfoDialog(
                        citizen = uiState.cardInfo,
                        onDismiss = { viewModel.showEditInfoDialog(false) },
                        onSave = { citizen ->
                            viewModel.createCitizen = citizen
                            viewModel.showSetupPinDialog(true)
                            viewModel.showEditInfoDialog(false)
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.isShowSetupPinDialog,
                enter = fadeIn(tween(300)) + slideInVertically(
                    initialOffsetY = { it }, // Trượt từ đáy màn hình lên
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut(tween(200)) + slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(200)
                )
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ChangePinDialog(
                        isChangePin = false,
                        onDismiss = {
                            viewModel.showSetupPinDialog(false)
                        },
                        onConfirm = { _, newPin ->
                            viewModel.createCitizen?.let { viewModel.setupPinCode(newPin, it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SystemManagerApp(
    citizen: Citizen?,
    isCardConnected: Boolean,
    onClickConnectCard: () -> Unit = {},
    onClickCreateInfo: () -> Unit
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
                    0 -> HomeTabContent(isCardConnected, citizen, onClickCreateInfo)
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

@Composable
fun HomeTabContent(isCardConnected: Boolean, citizen: Citizen?, onClickCreateInfo: () -> Unit) {
    if (isCardConnected) {
        if (citizen == null) {
            EmptyCardScreen(onClickCreateInfo)
        } else {
            CitizenInfoScreen(citizen)
        }
    } else {
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
}


@Composable
fun AvatarDisplay(avatarBytes: ByteArray?) {
    Box(
        modifier = Modifier
            .size(120.dp) // fitHeight="100.0" fitWidth="100.0" (Tăng nhẹ lên 120 cho đẹp)
            .background(ColorAvatarBg, RoundedCornerShape(5.dp))
            .border(2.dp, ColorAvatarBorder, RoundedCornerShape(5.dp))
            .clip(RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (avatarBytes != null && avatarBytes.isNotEmpty()) {
            val bitmap = remember(avatarBytes) {
                try {
                    Image.makeFromEncoded(avatarBytes).toComposeImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.AccountBox, null, tint = Color.Gray, modifier = Modifier.size(60.dp))
            }
        } else {
            Icon(Icons.Default.AccountBox, null, tint = Color.Gray, modifier = Modifier.size(60.dp))
        }
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE0E0E0), // Màu xám nhẹ cho nút
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(4.dp),
        // border = BorderStroke(1.dp, Color.Gray), // Nếu muốn viền như nút cũ
        modifier = Modifier.height(40.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                fontSize = 18.sp,
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