package com.duyvv.citizen_card_app.data.local.table

import org.jetbrains.exposed.sql.Table

object CitizenTable : Table("citizen") {
    val citizenId = text("citizenId").uniqueIndex()
    val fullName = text("fullName")
    val gender = text("gender")
    val birthDate = text("birthDate")
    val address = text("address")
    val hometown = text("hometown")
    val nationality = text("nationality")
    val ethnicity = text("ethnicity")
    val religion = text("religion")
    val identification = text("identification")
    val avatar = blob("avatar").nullable()
    val publicKey = text("publicKey").nullable()

    override val primaryKey = PrimaryKey(citizenId)
}