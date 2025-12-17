package com.duyvv.citizen_card_app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.presentation.dialog.CitizenInfoDialog
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorHeaderBg
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorOrange
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorRed
import com.duyvv.citizen_card_app.presentation.ui.theme.ColorTextPrimary
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ManageCitizenTabContent() {
    val viewModel = koinInject<ManageCitizenViewModel>()
    val homeViewModel = koinInject<HomeViewModel>()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(
        homeUiState.isShowEditInfoDialog,
        homeUiState.isShowIntegratedDocumentsDialog,
        homeUiState.isShowChangePinDialog,
        homeUiState.isShowPinConfirmLockCardDialog,
        homeUiState.isShowPinConfirmUnlockCardDialog
    ) {
        // Chỉ refresh khi các dialog này ĐÃ ĐÓNG (false) và đang có người được chọn
        if (!homeUiState.isShowEditInfoDialog &&
            !homeUiState.isShowIntegratedDocumentsDialog &&
            !homeUiState.isShowChangePinDialog &&
            !homeUiState.isShowPinConfirmLockCardDialog &&
            !homeUiState.isShowPinConfirmUnlockCardDialog &&
            uiState.selectedCitizen != null
        ) {
            viewModel.refreshSelectedCitizen()
        }
    }

    LaunchedEffect(homeUiState.isShowEditInfoDialog) {
        if (!homeUiState.isShowEditInfoDialog) {
            // Khi đóng dialog sửa, refresh lại toàn bộ danh sách để cập nhật tên/ngày sinh mới (nếu có)
            viewModel.refreshData()
            // Và refresh người đang chọn (nếu có)
            viewModel.refreshSelectedCitizen()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formattedDate = convertMillisToDate(millis)
                        viewModel.updateFilterDob(formattedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("Chọn", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.isShowDialogInfoCitizen && uiState.selectedCitizen != null) {
        val selected = uiState.selectedCitizen!!

        // 3. Kiểm tra logic:
        // - Thẻ phải đang kết nối (homeUiState.cardInfo != null)
        // - ID của người được chọn phải trùng với ID trong thẻ
        val isCardOwner = homeUiState.cardInfo != null &&
                homeUiState.cardInfo?.citizenId == selected.citizenId
        println("isCardOwner: $isCardOwner, ${homeUiState.cardInfo}")

        CitizenInfoDialog(
            citizen = selected,
            // 4. Truyền biến này vào Dialog để ẩn/hiện nút
            showActions = isCardOwner,

            onDismiss = { viewModel.closeCitizenDetail() },

            // Các action gọi sang HomeViewModel để xử lý (vì HomeViewModel giữ kết nối thẻ)
            onEditInfoClick = {
//                viewModel.closeCitizenDetail()
                homeViewModel.showEditInfoDialog(true)
            },
            onIntegratedDocumentClick = {
//                viewModel.closeCitizenDetail()
                homeViewModel.showIntegratedDocumentsDialog(true)
            },
            onPinChangeClick = {
//                viewModel.closeCitizenDetail()
                homeViewModel.citizen = selected
                homeViewModel.isShowResetPinDialog(true)
            },
            onLockCardClick = {
//                viewModel.closeCitizenDetail()
                homeViewModel.isShowPinConfirmLockCardDialog(true)
            },
            onUnlockCardClick = {
//                viewModel.closeCitizenDetail()
                homeViewModel.isShowPinConfirmUnlockCardDialog(true)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                    M3TextField(
                        value = uiState.filterId,
                        onValueChange = { viewModel.updateFilterId(it) },
                        placeholder = "Mã công dân", width = 160.dp, icon = Icons.Default.Search
                    )
                    M3TextField(
                        value = uiState.filterName,
                        onValueChange = { viewModel.updateFilterName(it) },
                        placeholder = "Họ và tên", width = 180.dp, icon = Icons.Default.Person
                    )

                    M3DropdownGender(
                        width = 130.dp,
                        selected = uiState.filterGender.ifEmpty { "Giới tính" },
                        onSelected = { viewModel.updateFilterGender(it) }
                    )

                    M3TextField(
                        value = uiState.filterHometown,
                        onValueChange = { viewModel.updateFilterHometown(it) },
                        placeholder = "Quê quán", width = 180.dp, icon = Icons.Default.Home
                    )

                    // --- 3. Update Date Field ---
                    M3TextField(
                        value = uiState.filterDob,
                        onValueChange = { viewModel.updateFilterDob(it) },
                        placeholder = "Ngày sinh",
                        width = 160.dp,
                        icon = Icons.Default.DateRange,
                        readOnly = true, // Không cho nhập tay để tránh sai định dạng
                        onIconClick = { showDatePicker = true } // Bấm icon để mở lịch
                    )

                    // Action Buttons
                    Button(
                        onClick = { viewModel.searchCitizens() },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorHeaderBg),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tìm kiếm")
                    }

                    FilledTonalButton(
                        onClick = { viewModel.clearFilters() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFCFD8DC), contentColor = ColorTextPrimary
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
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFEEEEEE))
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

                // Table Content
                LazyColumn {
                    items(uiState.citizens) { citizen ->
                        TableRowItem(
                            citizen = citizen,
                            onClick = { viewModel.showCitizenDetail(citizen) },
                            onEditClick = {
                                val isCardOwner = homeUiState.cardInfo != null &&
                                        homeUiState.cardInfo?.citizenId == citizen.citizenId

                                if (isCardOwner) {
                                    // 2. Gán dữ liệu vào HomeViewModel
                                    homeViewModel.prepareForEdit(citizen)
                                    // 3. Mở Dialog Edit của HomeViewModel
                                    homeViewModel.showEditInfoDialog(true)
                                } else {
                                    // (Tùy chọn) Hiện thông báo yêu cầu kết nối đúng thẻ
                                    homeViewModel.showNoticeDialog(true)
                                    // Bạn có thể update message thông báo trong ViewModel nếu muốn
                                }
                            },
                            onDeleteClick = {
                                val connectedCitizenId = homeUiState.cardInfo?.citizenId

                                if (connectedCitizenId != null && connectedCitizenId == citizen.citizenId) {
                                    homeViewModel.disconnectCard()
                                }

                                // 2. Tiến hành xóa khỏi Database
                                viewModel.deleteCitizen(citizen.citizenId)
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

// --- CẬP NHẬT CÁC COMPOSABLE CON ---

@Composable
fun M3TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    width: androidx.compose.ui.unit.Dp,
    icon: ImageVector? = null,
    readOnly: Boolean = false,
    onIconClick: (() -> Unit)? = null // Thêm callback click icon
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        leadingIcon = if (icon != null) {
            {
                // Nếu có onIconClick thì dùng IconButton, ngược lại dùng Icon thường
                if (onIconClick != null) {
                    IconButton(onClick = onIconClick) {
                        Icon(icon, contentDescription = null, tint = Color.Gray)
                    }
                } else {
                    Icon(icon, contentDescription = null, tint = Color.Gray)
                }
            }
        } else null,
        modifier = Modifier.width(width).height(50.dp),
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
fun M3DropdownGender(
    width: androidx.compose.ui.unit.Dp,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.width(width).height(50.dp)
    ) {
        OutlinedTextField(
            value = selected,
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
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TableRowItem(
    citizen: Citizen,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableTextCell(citizen.citizenId, 1.2f)
        TableTextCell(citizen.fullName, 2f, alignLeft = true)
        TableTextCell(citizen.birthDate, 1.3f)
        TableTextCell(citizen.gender, 1f)
        TableTextCell(citizen.hometown, 2f, alignLeft = true)

        // Actions Column
        Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                /*SmallFloatingActionButton(
                    onClick = onEditClick,
                    containerColor = Color.White,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = ColorOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }*/
                SmallFloatingActionButton(
                    onClick = onDeleteClick,
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

// Hàm tiện ích chuyển đổi ngày
private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}