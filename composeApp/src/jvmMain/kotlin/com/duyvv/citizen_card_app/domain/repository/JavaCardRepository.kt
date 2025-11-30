package com.duyvv.citizen_card_app.domain.repository

import com.duyvv.citizen_card_app.data.local.entity.Citizen

interface JavaCardRepository {
    suspend fun connectCard(): Boolean
    suspend fun isCardActive(): Boolean
    suspend fun getCardId(): String?
    suspend fun challengeCard(citizenId: String, storedPublicKey: String?): Boolean
    suspend fun verifyCard(pinCode: String): Pair<Boolean, Int>
    suspend fun getCardInfo(): Citizen?
    suspend fun setupPinCode(
        pinCode: String,
        citizen: Citizen,
        latestId: String?,
        onResult: (Boolean, Citizen?, String?) -> Unit
    )

    suspend fun sendAvatar(avatar: ByteArray?): Boolean
    fun disconnectCard()
}