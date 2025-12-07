package com.duyvv.citizen_card_app.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.domain.ApplicationState
import com.duyvv.citizen_card_app.presentation.dialog.*
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
                    viewModel.showCreateInfoDialog(true)
                },
                onPinChangeClick = {
                    viewModel.showChangePinDialog(true)
                },
                onEditInfoClick = {
                    viewModel.showEditInfoDialog(true)
                },
                onLockCardClick = {
                    viewModel.isShowPinConfirmLockCardDialog(true)
                },
                onUnlockCardClick = {
                    viewModel.isShowPinConfirmUnlockCardDialog(true)
                },
                onIntegratedDocumentClick = {
                    viewModel.showIntegratedDocumentsDialog(true)
                }
            )

            val isAnyDialogVisible = uiState.isShowPinDialog ||
                    uiState.isShowErrorPinCodeDialog ||
                    uiState.isShowNoticeDialog ||
                    uiState.isCreateInfoDialog ||
                    uiState.isShowSetupPinDialog ||
                    uiState.isShowChangePinDialog ||
                    uiState.isShowEditInfoDialog ||
                    uiState.isShowPinConfirmChangeInfoDialog ||
                    uiState.isShowPinConfirmLockCardDialog ||
                    uiState.isShowPinConfirmUnlockCardDialog ||
                    uiState.isShowIntegratedDocumentsDialog


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
                visible = uiState.isShowPinDialog
                        || uiState.isShowPinConfirmChangeInfoDialog
                        || uiState.isShowPinConfirmLockCardDialog
                        || uiState.isShowPinConfirmUnlockCardDialog,
                enter = fadeIn(tween(200)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut(tween(0)) + scaleOut(targetScale = 0.9f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EnterPinDialog(
                        label = "Nhập mã pin (6 ký tự)",
                        hint = "Mã pin",
                        leftLabel = "Huỷ",
                        rightLabel = "Xác nhận",
                        onClickLeftBtn = {
                            when {
                                uiState.isShowPinDialog -> viewModel.showPinDialog(false)
                                uiState.isShowPinConfirmChangeInfoDialog -> viewModel.isShowPinConfirmDialog(false)
                                uiState.isShowPinConfirmLockCardDialog -> viewModel.isShowPinConfirmLockCardDialog(false)
                                uiState.isShowPinConfirmUnlockCardDialog -> viewModel.isShowPinConfirmUnlockCardDialog(
                                    false
                                )
                            }
                        },
                        onClickRightBtn = { pinCode ->
                            when {
                                uiState.isShowPinDialog -> viewModel.connectCard(pinCode)
                                uiState.isShowPinConfirmChangeInfoDialog -> {
                                    viewModel.updateCardInfo(pinCode)
                                }

                                uiState.isShowPinConfirmLockCardDialog -> {
                                    viewModel.lockCard(pinCode)
                                }

                                uiState.isShowPinConfirmUnlockCardDialog -> {
                                    viewModel.unlockCard(pinCode)
                                }
                            }
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
                            viewModel.verifyPinCode(pinCode)
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
                visible = uiState.isCreateInfoDialog,
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
                        onDismiss = { viewModel.showCreateInfoDialog(false) },
                        onSave = { citizen ->
                            viewModel.citizen = citizen
                            viewModel.showSetupPinDialog(true)
                            viewModel.showCreateInfoDialog(false)
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
                            viewModel.citizen?.let { viewModel.setupPinCode(newPin, it) }
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.isShowChangePinDialog,
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
                        isChangePin = true,
                        onDismiss = {
                            viewModel.showChangePinDialog(false)
                        },
                        onConfirm = { oldPin, newPin ->
                            viewModel.changePin(oldPin, newPin)
                        }
                    )
                }
            }

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
                            viewModel.citizen = citizen
                            viewModel.isShowPinConfirmDialog(true)
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.isShowIntegratedDocumentsDialog,
                enter = fadeIn(tween(300)) + slideInVertically(
                    initialOffsetY = { it },
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
                    val currentCitizenId = uiState.cardInfo?.citizenId ?: ""

                    if (currentCitizenId.isNotEmpty()) {
                        IntegratedDocumentsDialog(
                            citizenId = currentCitizenId,
                            existingVehicle = uiState.currentVehicle,
                            existingLicense = uiState.currentLicense,
                            existingInsurance = uiState.currentInsurance,
                            onDismiss = { viewModel.showIntegratedDocumentsDialog(false) },
                            onSaveVehicle = { vehicle ->
                                viewModel.saveVehicle(vehicle)
                            },
                            onSaveDriving = { license ->
                                viewModel.saveDrivingLicense(license)
                            },
                            onSaveHealth = { health ->
                                viewModel.saveHealthInsurance(health)
                            }
                        )
                    }
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
    onClickCreateInfo: () -> Unit,
    onPinChangeClick: () -> Unit = {},
    onEditInfoClick: () -> Unit = {},
    onIntegratedDocumentClick: () -> Unit = {},
    onLockCardClick: () -> Unit = {},
    onUnlockCardClick: () -> Unit = {}
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
                    0 -> HomeTabContent(
                        isCardConnected = isCardConnected,
                        citizen = citizen,
                        onClickCreateInfo = onClickCreateInfo,
                        onPinChangeClick = onPinChangeClick,
                        onEditInfoClick = onEditInfoClick,
                        onLockCardClick = onLockCardClick,
                        onUnlockCardClick = onUnlockCardClick,
                        onIntegratedDocumentClick = onIntegratedDocumentClick
                    )

                    1 -> ManageCitizenTabContent()
                }
            }
        }
    }
}

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