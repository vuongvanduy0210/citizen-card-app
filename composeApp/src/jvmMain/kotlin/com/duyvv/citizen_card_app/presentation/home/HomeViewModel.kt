package com.duyvv.citizen_card_app.presentation.home

import androidx.lifecycle.viewModelScope
import com.duyvv.citizen_card_app.base.BaseViewModel
import com.duyvv.citizen_card_app.base.UiState
import com.duyvv.citizen_card_app.domain.ApplicationState
import com.duyvv.citizen_card_app.domain.repository.DataRepository
import com.duyvv.citizen_card_app.domain.repository.JavaCardRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val cardRepository: JavaCardRepository,
    private val dataRepository: DataRepository,
) : BaseViewModel<HomeUIState>(HomeUIState()) {

    fun connectCard(pinCode: String) {
        viewModelScope.launch {
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
                            sendEvent("Thẻ không hợp lệ do sai định dạng!")
                            ApplicationState.setCardVerified(false)
                            return@launch
                        }
                    }
                    cardRepository.verifyCard(pinCode) { isVerified, pinAttemptsRemain ->
                        ApplicationState.setCardVerified(isVerified)
                        if (isVerified) {
                            updateUiState { it.copy(isShowPinDialog = false) }
                        } else {
                            println("Pin code is incorrect!: $pinAttemptsRemain")
                            if (pinAttemptsRemain > 0) {

                            }
                        }
                    }
                } else {
                    sendEvent("Thẻ đã bị khoá!")
                    ApplicationState.setCardInserted(false)
                    cardRepository.disconnectCard()
                }
            } else {
                sendEvent("Không thể kết nối thẻ!")
            }
        }
    }

    fun disconnectCard() {
        ApplicationState.reset()
    }

    fun showPinDialog(isShow: Boolean) {
        updateUiState { it.copy(isShowPinDialog = isShow) }
    }
}

data class HomeUIState(
    val isShowPinDialog: Boolean = false,
    val errorMessage: String = "",
) : UiState