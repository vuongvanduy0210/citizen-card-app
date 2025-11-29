package com.duyvv.citizen_card_app.data.local.entity

data class VehicleRegister(
    val citizenId: String,
    val vehicleRegisterId: Int,
    val vehicleBrand: String?,
    val vehicleModel: String?,
    val vehicleColor: String?,
    val vehiclePlate: String?,
    val vehicleFrame: String?,
    val vehicleEngine: String?,
    val vehicleRegisterDate: String?,
    val vehicleExpiredDate: String?,
    val vehicleRegisterPlace: String?,
    val vehicleCapacity: String?
)
