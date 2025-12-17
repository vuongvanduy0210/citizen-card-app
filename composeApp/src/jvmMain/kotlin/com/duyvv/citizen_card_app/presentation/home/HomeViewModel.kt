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
                            noticeMessage = "Nhập sai mã pin! Còn $pinAttemptsRemain lần thử!"
                        )
                    }
                } else {
                    updateUiState {
                        it.copy(
                            isShowNoticeDialog = true,
                            noticeMessage = "Thẻ đã bị khoá do quá số lần sai cho phép!"
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

    fun resetPinCode(pinCode: String, citizen: Citizen) {
        viewModelHandlerScope.launch {
            println("resetPinCode11111: ")
            cardRepository.resetPinCode(pinCode, citizen) { isSuccess, newCitizen, publicKey ->
                println("resetPinCode: $isSuccess, $newCitizen, $publicKey")
                if (isSuccess && newCitizen != null && publicKey != null) {
                    updateCitizenLocal(newCitizen, publicKey, true)
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

    fun lockCard(pinCode: String) {
        verifyPinCard(pinCode) {
            viewModelHandlerScope.launch {
                val isSuccess = cardRepository.lockCard()
                updateUiState {
                    it.copy(
                        isShowPinConfirmLockCardDialog = !isSuccess,
                        isShowNoticeDialog = true,
                        noticeMessage = if (isSuccess) "Khóa thẻ thành công!" else "Khóa thẻ thất bại!"
                    )
                }
            }
        }
    }

    fun unlockCard(pinCode: String) {
        viewModelHandlerScope.launch {
            val isSuccess = cardRepository.unlockCard()
            updateUiState {
                it.copy(
                    isShowPinConfirmUnlockCardDialog = !isSuccess,
                    isShowNoticeDialog = true,
                    noticeMessage = if (isSuccess) "Mở khóa thẻ thành công!" else "Mở khóa thẻ thất bại!"
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

    fun saveDrivingLicense(license: DrivingLicense) {
        viewModelHandlerScope.launch {
            val success = dataRepository.saveDrivingLicense(license)
            showNoticeResult(success, "Lưu giấy phép lái xe")

            // --- THÊM DÒNG NÀY ---
            if (success) {
                loadIntegratedDocuments(license.citizenId)
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
    val currentInsurance: HealthInsurance? = null
) : UiState {
    fun reset() = copy(
        isShowPinDialog = false,
        isShowErrorPinCodeDialog = false,
        isShowNoticeDialog = false,
        noticeMessage = "",
        isCreateInfoDialog = false,
        isShowChangePinDialog = false,
        cardInfo = null,
        errorMessage = "",
        isShowEditInfoDialog = false,
        isShowPinConfirmChangeInfoDialog = false,
        isShowPinConfirmLockCardDialog = false,
        isShowPinConfirmUnlockCardDialog = false,
        isShowIntegratedDocumentsDialog = false,
        isShowResetPinDialog = false,
    )
}