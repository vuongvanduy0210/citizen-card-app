package com.duyvv.citizen_card_app.presentation.home

import com.duyvv.citizen_card_app.base.BaseViewModel
import com.duyvv.citizen_card_app.base.UiState
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.data.local.entity.DrivingLicense
import com.duyvv.citizen_card_app.data.local.entity.HealthInsurance
import com.duyvv.citizen_card_app.data.local.entity.VehicleRegister
import com.duyvv.citizen_card_app.domain.ApplicationState
import com.duyvv.citizen_card_app.domain.repository.DataRepository
import com.duyvv.citizen_card_app.domain.repository.JavaCardRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val cardRepository: JavaCardRepository,
    private val dataRepository: DataRepository,
) : BaseViewModel<HomeUIState>(HomeUIState()) {

    var citizen: Citizen? = null

    fun connectCard(pinCode: String) {
        viewModelHandlerScope.launch {
            val isConnected = cardRepository.connectCard()
            if (isConnected) {
                if (cardRepository.isCardActive()) {
                    println("Card is active")
                    val cardId = cardRepository.getCardId()
                    if (cardId != null) {
                        val isCardVerified = cardRepository.challengeCard(
                            cardId,
                            dataRepository.getPublicKeyById(cardId)
                        )
                        println("Challenge result : $isCardVerified")
                        if (!isCardVerified) {
                            updateUiState {
                                it.copy(
                                    isShowNoticeDialog = true,
                                    noticeMessage = "Thẻ không hợp lệ do sai định dạng!"
                                )
                            }
                            ApplicationState.setCardVerified(false)
                            return@launch
                        }
                    }
                    val (isVerified, pinAttemptsRemain) = cardRepository.verifyCard(pinCode)
                    ApplicationState.setCardVerified(isVerified)
                    if (isVerified) {
                        updateUiState {
                            it.copy(
                                isShowNoticeDialog = false,
                                isShowErrorPinCodeDialog = false,
                                isShowPinDialog = false
                            )
                        }
                        getCardInfo()
                    } else {
                        println("Pin code is incorrect!: $pinAttemptsRemain")
                        if (pinAttemptsRemain > 0) {
                            updateUiState { it.copy(isShowPinDialog = false, isShowErrorPinCodeDialog = true) }
                        }
                    }
                } else {
                    updateUiState { it.copy(isShowNoticeDialog = true, noticeMessage = "Thẻ đã bị khoá!") }
                    ApplicationState.setCardInserted(false)
                    cardRepository.disconnectCard()
                }
            } else {
                updateUiState { it.copy(isShowNoticeDialog = true, noticeMessage = "Không thể kết nối thẻ!") }
            }
        }
    }

    fun disconnectCard() {
        ApplicationState.reset()
        updateUiState { it.reset() }
    }

    fun verifyPinCode(pinCode: String) {
        verifyPinCard(pinCode) {
            println("Card connected successfully!")
            ApplicationState.setCardVerified(true)
            updateUiState {
                it.copy(
                    isShowNoticeDialog = false,
                    isShowErrorPinCodeDialog = false,
                    isShowPinDialog = false
                )
            }
            viewModelHandlerScope.launch {
                getCardInfo()
            }
        }
    }

    fun verifyPinCard(pinCode: String, onSuccess: () -> Unit) {
        viewModelHandlerScope.launch {
            val (isCardVerified, pinAttemptsRemain) = cardRepository.verifyCard(pinCode)
            if (isCardVerified) {
                onSuccess.invoke()
            } else {
                if (pinAttemptsRemain > 0) {
                    updateUiState {
                        it.copy(
                            isShowNoticeDialog = true,
                            isCardActive = true,
                            noticeMessage = "Nhập sai mã pin! Còn $pinAttemptsRemain lần thử!"
                        )
                    }
                } else {
                    updateUiState {
                        it.copy(
                            isShowNoticeDialog = true,
                            isCardActive = false,
                            noticeMessage = "Thẻ đã bị khoá vui lòng thử lại sau!"
                        )
                    }
                }
            }
        }
    }

    suspend fun getCardInfo() {
        val cardInfo = cardRepository.getCardInfo()
        if (cardInfo == null) {
            updateUiState { it.copy(isCreateInfoDialog = true, cardInfo = null) }
        } else {
            updateUiState { it.copy(cardInfo = cardInfo) }
        }
    }

    fun setupPinCode(pinCode: String, citizen: Citizen) {
        viewModelHandlerScope.launch {
            println("setupPinCode11111: ")
            val latestId = dataRepository.getLatestCitizenId(SimpleDateFormat("ddMMyy").format(Date()))
            println("setupPinCode: $latestId")
            cardRepository.setupPinCode(pinCode, citizen, latestId) { isSuccess, newCitizen, publicKey ->
                if (isSuccess && newCitizen != null && publicKey != null) {
                    updateCitizenLocal(newCitizen, publicKey)
                } else {
                    updateUiState {
                        it.copy(
                            isShowNoticeDialog = true,
                            noticeMessage = "Thiết lập thông tin thất bại, vui lòng thử lại!"
                        )
                    }
                }
            }
        }
    }

    fun resetPinCode(adminPin: String, pinCode: String) {
        viewModelHandlerScope.launch {
            println("resetPinCode11111: ")
            val isSuccess = cardRepository.resetPinCode(adminPin, pinCode)
            if (isSuccess) {
                updateUiState {
                    it.copy(
                        isShowNoticeDialog = true,
                        isShowSetupPinDialog = false,
                        isShowResetPinDialog = false,
                        noticeMessage = "Reset PIN thành công! (Mặc định PUK: 12345678)"
                    )
                }
                getCardInfo()
            } else {
                updateUiState {
                    it.copy(
                        isShowNoticeDialog = true,
                        noticeMessage = "Reset PIN thất bại! Vui lòng kiểm tra PUK."
                    )
                }
            }
        }
    }

    fun updateCitizenLocal(citizen: Citizen, publicKey: String, isUpdate: Boolean = false) {
        viewModelHandlerScope.launch {
            val isInsertSuccess = if (isUpdate) {
                dataRepository.updateCitizen(citizen)
            } else {
                dataRepository.insertCitizen(citizen)
            }
            val isSetPublicKeySuccess = dataRepository.updatePublicKey(citizen.citizenId, publicKey)
            if (isInsertSuccess && isSetPublicKeySuccess) {
                updateUiState {
                    it.copy(
                        isShowNoticeDialog = true,
                        isShowSetupPinDialog = false,
                        isShowResetPinDialog = false,
                        noticeMessage = "Thiết lập mã PIN thành công!"
                    )
                }
                getCardInfo()
            } else {
                updateUiState {
                    it.copy(
                        isShowNoticeDialog = true,
                        noticeMessage = "Thiết lập mã PIN thất bại, vui lòng thử lại!"
                    )
                }
            }
        }
    }

    fun changePin(oldPin: String, newPin: String) {
        verifyPinCard(oldPin) {
            viewModelHandlerScope.launch {
                val isSuccess = cardRepository.changePin(oldPin, newPin)
                if (isSuccess) {
                    updateUiState {
                        it.copy(
                            isShowNoticeDialog = true,
                            isShowChangePinDialog = false,
                            noticeMessage = "Thay đổi mã pin thành công!"
                        )
                    }
                } else {
                    updateUiState {
                        it.copy(
                            isShowNoticeDialog = true,
                            noticeMessage = "Thay đổi mã pin thất bại, vui lòng thử lại!"
                        )
                    }
                }
            }
        }
    }

    fun updateCardInfo(pinCode: String) {
        verifyPinCard(pinCode) {
            if (citizen?.avatar == null) {
                updateUiState { it.copy(isShowNoticeDialog = true, noticeMessage = "Ảnh không hợp lệ!") }
            } else {
                viewModelHandlerScope.launch {
                    val isSendSuccess = cardRepository.sendAvatar(citizen?.avatar!!)
                    if (!isSendSuccess) {
                        updateUiState { it.copy(isShowNoticeDialog = true, noticeMessage = "Cập nhật ảnh thất bại!") }
                    } else {
                        val isUpdated = cardRepository.updateCardInfo(citizen!!)
                        updateUiState {
                            it.copy(
                                isShowNoticeDialog = true,
                                noticeMessage = "Cập nhật thông tin xuống thẻ thành công!"
                            )
                        }
                        if (isUpdated) {
                            val isSuccess = dataRepository.updateCitizen(citizen!!)
                            if (isSuccess) {
                                updateUiState {
                                    it.copy(
                                        isShowNoticeDialog = true,
                                        isShowPinConfirmChangeInfoDialog = false,
                                        isShowEditInfoDialog = false,
                                        noticeMessage = "Cập nhật thông tin thành công!"
                                    )
                                }
                                getCardInfo()
                            } else {
                                updateUiState {
                                    it.copy(
                                        isShowNoticeDialog = true,
                                        noticeMessage = "Cập nhật thông tin thất bại!"
                                    )
                                }
                            }
                        } else {
                            updateUiState {
                                it.copy(
                                    isShowNoticeDialog = true,
                                    noticeMessage = "Cập nhật thông tin thất bại!"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun lockCard(pinCode: String, isFromAdmin: Boolean = false) {
        if (isFromAdmin) {
            viewModelHandlerScope.launch {
                lock(isFromAdmin)
            }
        } else {
            verifyPinCard(pinCode) {
                viewModelHandlerScope.launch {
                    lock(isFromAdmin)
                }
            }
        }
    }

    suspend fun lock(isFromAdmin: Boolean) {
        val isSuccess = cardRepository.lockCard()
        updateUiState {
            it.copy(
                isShowPinConfirmLockCardDialog = if (isFromAdmin) false else !isSuccess,
                isShowNoticeDialog = true,
                noticeMessage = if (isSuccess) {
                    updateUiState { it.copy(isCardActive = false) }
                    "Khóa thẻ thành công!"
                } else {
                    "Khóa thẻ thất bại!"
                }
            )
        }
    }

    fun unlockCard(pinCode: String, isFromAdmin: Boolean = false) {
        if (isFromAdmin) {
            unlock(true)
        } else {
            verifyPinCard(pinCode) {
                unlock(isFromAdmin)
            }
        }
    }

    fun unlock(isFromAdmin: Boolean) {
        viewModelHandlerScope.launch {
            val isSuccess = cardRepository.unlockCard()
            updateUiState {
                it.copy(
                    isShowPinConfirmUnlockCardDialog = if (isFromAdmin) false else !isSuccess,
                    isShowNoticeDialog = true,
                    noticeMessage = if (isSuccess) {
                        updateUiState { it.copy(isCardActive = true) }
                        "Mở khóa thẻ thành công!"
                    } else {
                        "Mở khóa thẻ thất bại!"
                    }
                )
            }
        }
    }

    fun saveVehicle(vehicle: VehicleRegister) {
        viewModelHandlerScope.launch {
            val success = dataRepository.saveVehicleRegister(vehicle)
            showNoticeResult(success, "Lưu giấy đăng ký xe")

            // --- THÊM DÒNG NÀY ---
            if (success) {
                loadIntegratedDocuments(vehicle.citizenId)
            }
        }
    }

    // --- SỬA LẠI HÀM LƯU BẰNG LÁI (saveDrivingLicense) ---
    // (Tìm hàm cũ và thay bằng nội dung này)
    fun saveDrivingLicense(license: DrivingLicense) {
        viewModelHandlerScope.launch {
            // B1: Lưu vào DB Local
            val success = dataRepository.saveDrivingLicense(license)

            if (success) {
                // B2: Tải lại dữ liệu cho dialog
                loadIntegratedDocuments(license.citizenId)

                // B3: Nếu thẻ đang kết nối -> Đồng bộ toàn bộ danh sách xuống thẻ
                if (uiStateFlow.value.isCardActive) {
                    syncAllLicensesToCard(license.citizenId)
                } else {
                    updateUiState {
                        it.copy(
                            isShowNoticeDialog = true,
                            noticeMessage = "Đã lưu vào máy! (Chưa đồng bộ thẻ do chưa kết nối)"
                        )
                    }
                }
            } else {
                updateUiState { it.copy(isShowNoticeDialog = true, noticeMessage = "Lưu thất bại!") }
            }
        }
    }

    // 2. THÊM MỚI hàm này (để hàm trên gọi được)
    // Nhiệm vụ: Lấy hết bằng lái từ DB -> Gộp chuỗi -> Gửi xuống thẻ
    private suspend fun syncAllLicensesToCard(citizenId: String) {
        // Lấy danh sách mới nhất từ DB
        val licenses = dataRepository.getDrivingLicenseByCitizenId(citizenId)

        // Tạo chuỗi gộp: "ID1|A1|Date # ID2|B2|Date"
        val joinedString = licenses.joinToString("#") {
            "${it.licenseId}|${it.licenseLevel}|${it.expiredAt}"
        }

        // Gửi lệnh Setup (Sẽ reset điểm về 12 cho danh sách mới)
        // Lưu ý: Đảm bảo Repository đã có hàm setupMultiLicenses
        val success = cardRepository.setupMultiLicenses(joinedString, licenses.size, keepOldScores = true)

        if (success) {
            updateUiState {
                it.copy(
                    isShowNoticeDialog = true,
                    noticeMessage = "Đã lưu DB và đồng bộ danh sách xuống thẻ!"
                )
            }
            // Load lại từ thẻ để hiển thị lên giao diện
            loadLicensesFromCard()
        } else {
            updateUiState {
                it.copy(
                    isShowNoticeDialog = true,
                    noticeMessage = "Lưu DB thành công nhưng LỖI đồng bộ thẻ!"
                )
            }
        }
    }

    fun saveHealthInsurance(insurance: HealthInsurance) {
        viewModelHandlerScope.launch {
            val success = dataRepository.saveHealthInsurance(insurance)
            showNoticeResult(success, "Lưu bảo hiểm y tế")

            // --- THÊM DÒNG NÀY ---
            if (success) {
                loadIntegratedDocuments(insurance.citizenId)
            }
        }
    }

    private fun showNoticeResult(isSuccess: Boolean, title: String) {
        updateUiState {
            it.copy(
                isShowNoticeDialog = true,
                noticeMessage = if (isSuccess) "$title thành công!" else "$title thất bại!"
            )
        }
    }

    fun prepareForEdit(citizen: Citizen) {
        updateUiState {
            it.copy(
                cardInfo = citizen
            )
        }
    }

    fun showPinDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowPinDialog = isShow) }
    }

    fun showErrorPinCodeDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowErrorPinCodeDialog = isShow) }
    }

    fun showNoticeDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowNoticeDialog = isShow) }
    }

    fun showCreateInfoDialog(isShow: Boolean) {
        updateUiState { it.copy(isCreateInfoDialog = isShow) }
    }

    fun showSetupPinDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowSetupPinDialog = isShow) }
    }

    fun showChangePinDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowChangePinDialog = isShow) }
    }

    fun showEditInfoDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowEditInfoDialog = isShow) }
    }

    fun isShowPinConfirmDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowPinConfirmChangeInfoDialog = isShow) }
    }

    fun isShowPinConfirmLockCardDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowPinConfirmLockCardDialog = isShow) }
    }

    fun isShowPinConfirmUnlockCardDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowPinConfirmUnlockCardDialog = isShow) }
    }

    fun isShowResetPinDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowResetPinDialog = isShow) }
    }

    fun showIntegratedDocumentsDialog(isShow: Boolean) {
        if (isShow) {
            val currentCitizenId = uiStateFlow.value.cardInfo?.citizenId
            if (currentCitizenId != null) {
                // Hiện dialog trước
                updateUiState { it.copy(isShowIntegratedDocumentsDialog = true) }
                // Sau đó tải dữ liệu
                loadIntegratedDocuments(currentCitizenId)
            }
        } else {
            // Đóng dialog và reset dữ liệu
            updateUiState {
                it.copy(
                    isShowIntegratedDocumentsDialog = false,
                    currentVehicle = null,
                    currentLicense = null,
                    currentInsurance = null
                )
            }
        }
    }

    private fun loadIntegratedDocuments(citizenId: String) {
        println("Loading integrated documents ...")
        viewModelHandlerScope.launch {
            val vehicles = dataRepository.getVehicleRegisterByCitizenId(citizenId)
            val licenses = dataRepository.getDrivingLicenseByCitizenId(citizenId)
            val insurances = dataRepository.getHealthInsuranceByCitizenId(citizenId)

            updateUiState {
                it.copy(
                    // Cập nhật lại dữ liệu mới nhất từ DB vào State
                    currentVehicle = vehicles.firstOrNull(),
                    currentLicense = licenses.firstOrNull(),
                    currentInsurance = insurances.firstOrNull()
                )
            }
        }
    }

    fun loadLicensesFromCard() {
        viewModelHandlerScope.launch {
            val result = cardRepository.getAllLicensesFromCard()

            if (result != null) {
                val uiItems = result.mapIndexed { index, (text, score, isRevoked) ->
                    // --- FIX LOGIC: Nếu 0 điểm thì coi như KHÓA luôn ---
                    val finalIsRevoked = isRevoked || score == 0
                    LicenseUiItem(index, text, score, finalIsRevoked)
                }

                // Cập nhật State
                updateUiState { currentState ->
                    // --- ĐỒNG BỘ DATA CHO DIALOG ---
                    // Nếu đang mở Dialog, phải lấy dữ liệu mới nhất đắp vào selectedLicense
                    var updatedSelected = currentState.selectedLicense
                    if (updatedSelected != null) {
                        // Tìm item có cùng index trong danh sách mới tải về
                        val freshItem = uiItems.find { it.index == updatedSelected?.index }
                        if (freshItem != null) {
                            updatedSelected = freshItem
                        }
                    }

                    currentState.copy(
                        cardLicenses = uiItems,
                        selectedLicense = updatedSelected // Cập nhật biến này thì UI Dialog mới đổi
                    )
                }
            } else {
                updateUiState { it.copy(cardLicenses = emptyList()) }
            }
        }
    }

    // 2. Trừ điểm (Gửi lệnh phạt -> Load lại ngay lập tức)
    fun deductPoints(points: Int) {
        val currentItem = uiStateFlow.value.selectedLicense ?: return

        viewModelHandlerScope.launch {
            // Gửi lệnh xuống thẻ (P1=Điểm, P2=Index)
            val result = cardRepository.penalizeLicenseByIndex(points, currentItem.index)

            if (result != null) {
                val (newScore, isRevoked) = result

                // Show thông báo tạm
                val isRevokedSafe = isRevoked || newScore == 0

                val msg = if (isRevokedSafe) "Đã tước bằng lái!" else "Trừ thành công. Còn $newScore điểm."
                updateUiState { it.copy(isShowNoticeDialog = true, noticeMessage = msg) }

                // QUAN TRỌNG: Gọi hàm load lại để UI (List và Dialog) nhận dữ liệu mới nhất từ thẻ
                loadLicensesFromCard()
            } else {
                updateUiState {
                    it.copy(
                        isShowNoticeDialog = true,
                        noticeMessage = "Lỗi kết nối thẻ hoặc sai vị trí bằng lái!"
                    )
                }
            }
        }
    }

    // 3. Hàm Setup/Reset (Sửa lỗi 0 điểm do sai P2)
    // Hàm này thay thế cho setupSampleLicense cũ
    fun resetScoreForTest() {
        viewModelHandlerScope.launch {
            val cid = uiStateFlow.value.cardInfo?.citizenId
            if (cid == null) {
                // Nếu chưa có info, thử tạo dữ liệu mẫu
                val sampleData = "123456|A1|20/10/2030"
                // Gửi setup với Count = 1 (P2 = 1) để thẻ biết khởi tạo 12 điểm cho 1 bằng
                if (cardRepository.setupMultiLicenses(sampleData, 1)) {
                    updateUiState {
                        it.copy(
                            isShowNoticeDialog = true,
                            noticeMessage = "Đã nạp dữ liệu mẫu (12 điểm)!"
                        )
                    }
                    loadLicensesFromCard()
                }
                return@launch
            }

            // Nếu có info, lấy từ DB nạp xuống
            val dbLicenses = dataRepository.getDrivingLicenseByCitizenId(cid)
            if (dbLicenses.isNotEmpty()) {
                val joinedStr = dbLicenses.joinToString("#") { "${it.licenseId}|${it.licenseLevel}|${it.expiredAt}" }
                // Gửi đúng số lượng (dbLicenses.size) vào P2
                if (cardRepository.setupMultiLicenses(joinedStr, dbLicenses.size)) {
                    updateUiState {
                        it.copy(
                            isShowNoticeDialog = true,
                            noticeMessage = "Đã Reset thẻ theo dữ liệu DB!"
                        )
                    }
                    loadLicensesFromCard()
                }
            } else {
                // DB trống => Nạp mẫu
                val sampleData = "SAMPLE|A1|2030"
                if (cardRepository.setupMultiLicenses(sampleData, 1)) {
                    loadLicensesFromCard()
                }
            }
        }
    }

    fun resetCurrentLicense() {
        val currentItem = uiStateFlow.value.selectedLicense ?: return

        viewModelHandlerScope.launch {
            val success = cardRepository.resetLicenseByIndex(currentItem.index)
            if (success) {
                updateUiState {
                    it.copy(
                        isShowNoticeDialog = true,
                        noticeMessage = "Đã khôi phục 12 điểm cho bằng lái này!"
                    )
                }
                loadLicensesFromCard() // Load lại để cập nhật UI
            } else {
                updateUiState { it.copy(isShowNoticeDialog = true, noticeMessage = "Lỗi khi reset!") }
            }
        }
    }

    // Các hàm điều khiển Dialog
    fun selectLicenseAndShowDetail(item: LicenseUiItem) {
        updateUiState { it.copy(selectedLicense = item, isShowLicenseDetailDialog = true) }
    }

    fun dismissLicenseDetail() {
        updateUiState { it.copy(isShowLicenseDetailDialog = false, selectedLicense = null) }
    }
}

data class HomeUIState(
    val isShowPinDialog: Boolean = false,
    val isShowErrorPinCodeDialog: Boolean = false,
    val isShowNoticeDialog: Boolean = false,
    val noticeMessage: String = "",
    val isCreateInfoDialog: Boolean = false,
    val isShowSetupPinDialog: Boolean = false,
    val isShowChangePinDialog: Boolean = false,
    val cardInfo: Citizen? = null,
    val errorMessage: String = "",
    val isShowEditInfoDialog: Boolean = false,
    val isShowPinConfirmChangeInfoDialog: Boolean = false,
    val isShowPinConfirmLockCardDialog: Boolean = false,
    val isShowPinConfirmUnlockCardDialog: Boolean = false,
    val isShowIntegratedDocumentsDialog: Boolean = false,
    val isShowResetPinDialog: Boolean = false,
    val currentVehicle: VehicleRegister? = null,
    val currentLicense: DrivingLicense? = null,
    val currentInsurance: HealthInsurance? = null,
    val isCardActive: Boolean = true,
    val cardLicenses: List<LicenseUiItem> = emptyList(), // Danh sách hiển thị
    val selectedLicense: LicenseUiItem? = null,          // Item đang chọn
    val isShowLicenseDetailDialog: Boolean = false,      // Cờ hiện dialog
    val dbLicenses: List<DrivingLicense> = emptyList()
) : UiState {
    fun reset() = HomeUIState()
}

// --- THÊM MỚI ---
data class LicenseUiItem(
    val index: Int,
    val rawData: String,
    val score: Int,
    val isRevoked: Boolean,
    val licenseId: String = rawData.split("|").getOrElse(0) { "" },
    val rank: String = rawData.split("|").getOrElse(1) { "" },
    val expiration: String = rawData.split("|").getOrElse(2) { "" }
)