package com.duyvv.citizen_card_app.data.local.dao

import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.data.local.table.CitizenTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction

object CitizenDao {
    fun insert(c: Citizen) = transaction {
        CitizenTable.insert {
            it[citizenId] = c.citizenId
            it[fullName] = c.fullName
            it[gender] = c.gender
            it[birthDate] = c.birthDate
            it[address] = c.address
            it[hometown] = c.hometown
            it[nationality] = c.nationality
            it[ethnicity] = c.ethnicity
            it[religion] = c.religion
            it[identification] = c.identification
            it[avatar] = c.avatar?.let { bytes -> ExposedBlob(bytes) }
        }
    }

    fun update(c: Citizen) = transaction {
        CitizenTable.update({ CitizenTable.citizenId eq c.citizenId }) {
            it[fullName] = c.fullName
            it[gender] = c.gender
            it[birthDate] = c.birthDate
            it[address] = c.address
            it[hometown] = c.hometown
            it[nationality] = c.nationality
            it[ethnicity] = c.ethnicity
            it[religion] = c.religion
            it[identification] = c.identification
            if (c.avatar != null) {
                it[avatar] = ExposedBlob(c.avatar!!)
            }
        }
    }

    fun delete(id: String) = transaction {
        CitizenTable.deleteWhere { citizenId eq id }
    }

    fun getById(id: String): Citizen? = transaction {
        CitizenTable.selectAll().where { CitizenTable.citizenId eq id }
            .singleOrNull()
            ?.let { rowToCitizen(it) }
    }

    fun getAll(): List<Citizen> = transaction {
        CitizenTable.selectAll().map { rowToCitizen(it) }
    }

    // --- Đổi từ private thành public để Repository dùng được ---
    fun rowToCitizen(row: ResultRow) = Citizen(
        citizenId = row[CitizenTable.citizenId],
        fullName = row[CitizenTable.fullName],
        gender = row[CitizenTable.gender],
        birthDate = row[CitizenTable.birthDate],
        address = row[CitizenTable.address],
        hometown = row[CitizenTable.hometown],
        nationality = row[CitizenTable.nationality],
        ethnicity = row[CitizenTable.ethnicity],
        religion = row[CitizenTable.religion],
        identification = row[CitizenTable.identification],
        avatar = row[CitizenTable.avatar]?.bytes
    )
}