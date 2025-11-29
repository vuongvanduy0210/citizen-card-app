package com.duyvv.citizen_card_app.data.local.entity

data class HealthInsurance(
    val citizenId: String,
    val insuranceId: String,
    val address: String?,
    val createDate: String?,
    val expiredDate: String?,
    val examinationPlace: String?
)
