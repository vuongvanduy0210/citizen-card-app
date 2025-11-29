package com.duyvv.citizen_card_app.data.local.dao

import com.duyvv.citizen_card_app.data.local.entity.HealthInsurance
import com.duyvv.citizen_card_app.data.local.table.HealthInsuranceTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object HealthInsuranceDao {

    fun insert(h: HealthInsurance) = transaction {
        HealthInsuranceTable.insert {
            it[citizenId] = h.citizenId
            it[insuranceId] = h.insuranceId
            it[address] = h.address
            it[createDate] = h.createDate
            it[expiredDate] = h.expiredDate
            it[examinationPlace] = h.examinationPlace
        }
    }

    // --- BỔ SUNG ---
    fun update(h: HealthInsurance) = transaction {
        HealthInsuranceTable.update({ HealthInsuranceTable.insuranceId eq h.insuranceId }) {
            it[address] = h.address
            it[createDate] = h.createDate
            it[expiredDate] = h.expiredDate
            it[examinationPlace] = h.examinationPlace
        }
    }

    // --- BỔ SUNG ---
    fun deleteByCitizenId(cId: String) = transaction {
        HealthInsuranceTable.deleteWhere { citizenId eq cId }
    }

    fun getByCitizenId(id: String): List<HealthInsurance> = transaction {
        HealthInsuranceTable
            .selectAll().where { HealthInsuranceTable.citizenId eq id }
            .map { rowToHealthInsurance(it) }
    }

    private fun rowToHealthInsurance(it: ResultRow) = HealthInsurance(
        it[HealthInsuranceTable.citizenId],
        it[HealthInsuranceTable.insuranceId],
        it[HealthInsuranceTable.address],
        it[HealthInsuranceTable.createDate],
        it[HealthInsuranceTable.expiredDate],
        it[HealthInsuranceTable.examinationPlace]
    )
}