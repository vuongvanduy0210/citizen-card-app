package com.duyvv.citizen_card_app.data.local.table

import org.jetbrains.exposed.sql.Table

object VehicleRegisterTable : Table("vehicle_register") {
    val citizenId = varchar("citizenId", 32)
    val vehicleRegisterId = integer("vehicleRegisterId").uniqueIndex()
    val vehicleBrand = varchar("vehicleBrand", 100).nullable()
    val vehicleModel = varchar("vehicleModel", 100).nullable()
    val vehicleColor = varchar("vehicleColor", 50).nullable()
    val vehiclePlate = varchar("vehiclePlate", 20).nullable()
    val vehicleFrame = varchar("vehicleFrame", 50).nullable()
    val vehicleEngine = varchar("vehicleEngine", 50).nullable()
    val vehicleRegisterDate = varchar("vehicleRegisterDate", 20).nullable()
    val vehicleExpiredDate = varchar("vehicleExpiredDate", 20).nullable()
    val vehicleRegisterPlace = varchar("vehicleRegisterPlace", 200).nullable()
    val vehicleCapacity = varchar("vehicleCapacity", 20).nullable()

    override val primaryKey = PrimaryKey(vehicleRegisterId)
}
