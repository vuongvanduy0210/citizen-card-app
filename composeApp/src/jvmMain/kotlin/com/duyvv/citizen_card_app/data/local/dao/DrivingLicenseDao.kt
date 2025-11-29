package com.duyvv.citizen_card_app.data.local.dao

import com.duyvv.citizen_card_app.data.local.entity.DrivingLicense
import com.duyvv.citizen_card_app.data.local.table.DrivingLicenseTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object DrivingLicenseDao {

    fun insert(d: DrivingLicense) = transaction {
        DrivingLicenseTable.insert {
            it[citizenId] = d.citizenId
            it[licenseId] = d.licenseId
            it[licenseLevel] = d.licenseLevel
            it[createdAt] = d.createdAt
            it[createPlace] = d.createPlace
            it[expiredAt] = d.expiredAt
            it[createdBy] = d.createdBy
        }
    }

    // --- BỔ SUNG ---
    fun update(d: DrivingLicense) = transaction {
        DrivingLicenseTable.update({ DrivingLicenseTable.licenseId eq d.licenseId }) {
            it[licenseLevel] = d.licenseLevel
            it[createdAt] = d.createdAt
            it[createPlace] = d.createPlace
            it[expiredAt] = d.expiredAt
            it[createdBy] = d.createdBy
        }
    }

    // --- BỔ SUNG ---
    fun deleteByCitizenId(cId: String) = transaction {
        DrivingLicenseTable.deleteWhere { citizenId eq cId }
    }

    fun getByCitizenId(id: String): List<DrivingLicense> = transaction {
        DrivingLicenseTable
            .selectAll().where { DrivingLicenseTable.citizenId eq id }
            .map { rowToDrivingLicense(it) }
    }

    private fun rowToDrivingLicense(it: ResultRow) = DrivingLicense(
        it[DrivingLicenseTable.citizenId],
        it[DrivingLicenseTable.licenseId],
        it[DrivingLicenseTable.licenseLevel],
        it[DrivingLicenseTable.createdAt],
        it[DrivingLicenseTable.createPlace],
        it[DrivingLicenseTable.expiredAt],
        it[DrivingLicenseTable.createdBy]
    )
}