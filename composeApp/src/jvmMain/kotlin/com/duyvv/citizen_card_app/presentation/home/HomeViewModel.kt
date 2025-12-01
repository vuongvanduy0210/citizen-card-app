package com.duyvv.citizen_card_app.presentation.home

import com.duyvv.citizen_card_app.base.BaseViewModel
import com.duyvv.citizen_card_app.base.UiState
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.domain.ApplicationState
import com.duyvv.citizen_card_app.domain.repository.DataRepository
import com.duyvv.citizen_card_app.domain.repository.JavaCardRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val cardRepository: JavaCardRepository,
    private val dataRepository: DataRepository,
) : BaseViewModel<HomeUIState>(HomeUIState()) {

    var createCitizen: Citizen? = null

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

    fun verifyPinCard(pinCode: String) {
        viewModelHandlerScope.launch {
            val (isCardVerified, pinAttemptsRemain) = cardRepository.verifyCard(pinCode)
            if (isCardVerified) {
                println("Card connected successfully!")
                ApplicationState.setCardVerified(true)
                updateUiState {
                    it.copy(
                        isShowNoticeDialog = false,
                        isShowErrorPinCodeDialog = false,
                        isShowPinDialog = false
                    )
                }
                getCardInfo()
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
            val latestId = dataRepository.getLatestCitizenId()
            println("setupPinCode: $latestId")
            cardRepository.setupPinCode(pinCode, citizen, latestId) { isSuccess, newCitizen, publicKey ->
                if (isSuccess && newCitizen != null && publicKey != null) {
                    updateCitizenLocal(newCitizen, publicKey)
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
    }

    fun updateCitizenLocal(citizen: Citizen, publicKey: String) {
        viewModelHandlerScope.launch {
            val isInsertSuccess = dataRepository.insertCitizen(citizen)
            val isSetPublicKeySuccess = dataRepository.updatePublicKey(citizen.citizenId, publicKey)
            if (isInsertSuccess && isSetPublicKeySuccess) {
                updateUiState {
                    it.copy(
                        isShowNoticeDialog = true,
                        isShowSetupPinDialog = false,
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

    fun updateCardInfo() {

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
        updateUiState { it.copy(isShowPinConfirmDialog = isShow) }
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
    val isShowPinConfirmDialog: Boolean = false,
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
        isShowEditInfoDialog = false
    )
}