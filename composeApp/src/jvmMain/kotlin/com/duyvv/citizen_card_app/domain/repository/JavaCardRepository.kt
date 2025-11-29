package com.duyvv.citizen_card_app.domain.repository

interface JavaCardRepository {
    suspend fun connectCard(): Boolean
    suspend fun isCardActive(): Boolean
    suspend fun getCardId(): String?
    suspend fun challengeCard(citizenId: String, storedPublicKey: String?): Boolean
    suspend fun verifyCard(pinCode: String, onResult: (Boolean, Int) -> Unit)
    fun disconnectCard()
}