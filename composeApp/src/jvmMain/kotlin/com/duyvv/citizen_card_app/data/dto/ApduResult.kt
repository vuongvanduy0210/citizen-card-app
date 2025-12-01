package com.duyvv.citizen_card_app.data.dto

sealed class ApduResult {
    class Success(val response: ByteArray?): ApduResult()
    class Failed(val message: String, val response: ByteArray? = null): ApduResult()
}