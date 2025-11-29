package com.duyvv.citizen_card_app.data.local.entity

data class DrivingLicense(
    val citizenId: String,
    val licenseId: String,
    val licenseLevel: String?,
    val createdAt: String?,
    val createPlace: String?,
    val expiredAt: String?,
    val createdBy: String?
)
