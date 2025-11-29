package com.duyvv.citizen_card_app.data.local.entity

data class Citizen(
    val citizenId: String,
    val fullName: String,
    val gender: String,
    val birthDate: String,
    val address: String,
    val hometown: String,
    val nationality: String,
    val ethnicity: String,
    val religion: String,
    val identification: String,
    val avatar: ByteArray? = null
) {
    fun toCardInfo(): String =
        listOf(
            citizenId, fullName, gender, birthDate, address,
            hometown, nationality, ethnicity, religion, identification
        ).joinToString("$")

    companion object {
        fun fromCardInfo(data: String, avatar: ByteArray? = null): Citizen {
            val parts = data.split("$")
            return Citizen(
                citizenId = parts[0],
                fullName = parts[1],
                gender = parts[2],
                birthDate = parts[3],
                address = parts[4],
                hometown = parts[5],
                nationality = parts[6],
                ethnicity = parts[7],
                religion = parts[8],
                identification = parts[9],
                avatar = avatar
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Citizen

        if (citizenId != other.citizenId) return false
        if (fullName != other.fullName) return false
        if (gender != other.gender) return false
        if (birthDate != other.birthDate) return false
        if (address != other.address) return false
        if (hometown != other.hometown) return false
        if (nationality != other.nationality) return false
        if (ethnicity != other.ethnicity) return false
        if (religion != other.religion) return false
        if (identification != other.identification) return false
        if (!avatar.contentEquals(other.avatar)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = citizenId.hashCode()
        result = 31 * result + fullName.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + birthDate.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + hometown.hashCode()
        result = 31 * result + nationality.hashCode()
        result = 31 * result + ethnicity.hashCode()
        result = 31 * result + religion.hashCode()
        result = 31 * result + identification.hashCode()
        result = 31 * result + (avatar?.contentHashCode() ?: 0)
        return result
    }
}
