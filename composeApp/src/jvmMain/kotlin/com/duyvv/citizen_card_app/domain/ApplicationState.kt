package com.duyvv.citizen_card_app.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.smartcardio.Card

object ApplicationState {
    private val _isCardInserted = MutableStateFlow(false)
    val isCardInserted = _isCardInserted.asStateFlow()

    private val _isCardVerified = MutableStateFlow(false)
    val isCardVerified = _isCardVerified.asStateFlow()

    fun setCardInserted(inserted: Boolean) {
        _isCardInserted.value = inserted
    }
    fun setCardVerified(verified: Boolean) {
        _isCardVerified.value = verified
    }

    var isAppLoggedIn: Boolean = false

    var cardNumber: String? = null
    var card: Card? = null

    fun reset() {
        _isCardInserted.value = false
        _isCardVerified.value = false
        isAppLoggedIn = false
        cardNumber = null
        card?.disconnect(false)
        card = null
    }
}