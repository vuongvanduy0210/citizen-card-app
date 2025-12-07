package com.duyvv.citizen_card_app.data.local.dao

import com.duyvv.citizen_card_app.data.local.entity.VehicleRegister
import com.duyvv.citizen_card_app.data.local.table.VehicleRegisterTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object VehicleRegisterDao {

    fun insert(v: VehicleRegister) = transaction {
        VehicleRegisterTable.insert {
            it[citizenId] = v.citizenId
            // vehicleRegisterId là tự tăng, thường không insert thủ công trừ khi cần thiết
            it[vehicleRegisterId] = v.vehicleRegisterId
            it[vehicleBrand] = v.vehicleBrand
            it[vehicleModel] = v.vehicleModel
            it[vehicleColor] = v.vehicleColor
            it[vehiclePlate] = v.vehiclePlate
            it[vehicleFrame] = v.vehicleFrame
            it[vehicleEngine] = v.vehicleEngine
            it[vehicleRegisterDate] = v.vehicleRegisterDate
            it[vehicleExpiredDate] = v.vehicleExpiredDate
            it[vehicleRegisterPlace] = v.vehicleRegisterPlace
            it[vehicleCapacity] = v.vehicleCapacity
        }
    }

    // --- BỔ SUNG ---
    fun update(v: VehicleRegister) = transaction {
        VehicleRegisterTable.update({ VehicleRegisterTable.vehicleRegisterId eq v.vehicleRegisterId }) {
            it[vehicleBrand] = v.vehicleBrand
            it[vehicleModel] = v.vehicleModel
            it[vehicleColor] = v.vehicleColor
            it[vehiclePlate] = v.vehiclePlate
            it[vehicleFrame] = v.vehicleFrame
            it[vehicleEngine] = v.vehicleEngine
            it[vehicleRegisterDate] = v.vehicleRegisterDate
            it[vehicleExpiredDate] = v.vehicleExpiredDate
            it[vehicleRegisterPlace] = v.vehicleRegisterPlace
            it[vehicleCapacity] = v.vehicleCapacity
        }
    }

    // --- BỔ SUNG ---
    fun deleteByCitizenId(cId: String) = transaction {
        VehicleRegisterTable.deleteWhere { citizenId eq cId }
    }

    fun getByCitizenId(id: String): List<VehicleRegister> = transaction {
        VehicleRegisterTable
            .selectAll().where { VehicleRegisterTable.citizenId eq id }
            .map { rowToVehicle(it) }
    }

    private fun rowToVehicle(it: ResultRow) = VehicleRegister(
        it[VehicleRegisterTable.citizenId],
        it[VehicleRegisterTable.vehicleRegisterId],
        it[VehicleRegisterTable.vehicleBrand],
        it[VehicleRegisterTable.vehicleModel],
        it[VehicleRegisterTable.vehicleColor],
        it[VehicleRegisterTable.vehiclePlate],
        it[VehicleRegisterTable.vehicleFrame],
        it[VehicleRegisterTable.vehicleEngine],
        it[VehicleRegisterTable.vehicleRegisterDate],
        it[VehicleRegisterTable.vehicleExpiredDate],
        it[VehicleRegisterTable.vehicleRegisterPlace],
        it[VehicleRegisterTable.vehicleCapacity]
    )
}