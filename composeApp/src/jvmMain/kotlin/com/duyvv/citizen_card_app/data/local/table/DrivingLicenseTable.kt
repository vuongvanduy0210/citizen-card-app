package com.duyvv.citizen_card_app.data.local.table

import org.jetbrains.exposed.sql.Table

object DrivingLicenseTable : Table("driving_license") {
    val citizenId = varchar("citizenId", 32)
    val licenseId = varchar("licenseId", 32).uniqueIndex()
    val licenseLevel = varchar("licenseLevel", 10).nullable()
    val createdAt = varchar("createdAt", 20).nullable()
    val createPlace = varchar("createPlace", 100).nullable()
    val expiredAt = varchar("expiredAt", 20).nullable()
    val createdBy = varchar("createdBy", 50).nullable()

    override val primaryKey = PrimaryKey(licenseId)
}
