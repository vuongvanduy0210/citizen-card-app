package com.duyvv.citizen_card_app.presentation.home

import androidx.lifecycle.viewModelScope
import com.duyvv.citizen_card_app.base.BaseViewModel
import com.duyvv.citizen_card_app.base.UiState
import com.duyvv.citizen_card_app.data.repository.JavaCardRepository
import com.duyvv.citizen_card_app.domain.ApplicationState
import kotlinx.coroutines.launch

class HomeViewModel(
    private val cardRepository: JavaCardRepository
) : BaseViewModel<HomeUIState>(HomeUIState()) {

    fun connectCard() {
        viewModelScope.launch {
            val isConnected = cardRepository.connectCard()
            if (isConnected) {
                if (cardRepository.isCardActive()) {
                    println("Card is active")
                    val cardId = cardRepository.getCardId()
                    if (cardId != null) {
                        val isCardVerified: Boolean = cardRepository.challengeCard(cardId)
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
}

data class HomeUIState(
    val errorMessage: String = "",
) : UiState