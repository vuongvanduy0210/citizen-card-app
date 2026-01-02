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

    suspend fun resetPinCode(
        pinCode: String,
        citizen: Citizen,
        onResult: (Boolean, Citizen?, String?) -> Unit
    )

    suspend fun sendAvatar(avatar: ByteArray?): Boolean
    fun disconnectCard()
    suspend fun changePin(oldPin: String, newPin: String): Boolean
    suspend fun updateCardInfo(citizen: Citizen): Boolean
    suspend fun lockCard(): Boolean
    suspend fun unlockCard(): Boolean

    suspend fun setupMultiLicenses(dataStr: String, count: Int): Boolean
    suspend fun penalizeLicenseByIndex(points: Int, index: Int): Pair<Int, Boolean>?
    suspend fun getAllLicensesFromCard(): List<Triple<String, Int, Boolean>>?
    suspend fun resetLicenseByIndex(index: Int): Boolean
}