package com.duyvv.citizen_card_app.data.local.table

import org.jetbrains.exposed.sql.Table

object HealthInsuranceTable : Table("health_insurance") {
    val citizenId = varchar("citizenId", 32)
    val insuranceId = varchar("insuranceId", 32).uniqueIndex()
    val address = varchar("address", 200).nullable()
    val createDate = varchar("createDate", 20).nullable()
    val expiredDate = varchar("expiredDate", 20).nullable()
    val examinationPlace = varchar("examinationPlace", 200).nullable()

    override val primaryKey = PrimaryKey(insuranceId)
}
