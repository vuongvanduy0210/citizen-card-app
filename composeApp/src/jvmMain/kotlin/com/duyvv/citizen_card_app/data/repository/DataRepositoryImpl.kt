package com.duyvv.citizen_card_app.data.repository

import com.duyvv.citizen_card_app.data.local.dao.CitizenDao
import com.duyvv.citizen_card_app.data.local.dao.DrivingLicenseDao
import com.duyvv.citizen_card_app.data.local.dao.HealthInsuranceDao
import com.duyvv.citizen_card_app.data.local.dao.VehicleRegisterDao
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.data.local.entity.DrivingLicense
import com.duyvv.citizen_card_app.data.local.entity.HealthInsurance
import com.duyvv.citizen_card_app.data.local.entity.VehicleRegister
import com.duyvv.citizen_card_app.data.local.table.CitizenTable
import com.duyvv.citizen_card_app.domain.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class DataRepositoryImpl : DataRepository {

    // Helper function để chạy query trên luồng IO an toàn
    private suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
        transaction { block() }
    }

    // ==================================================================================
    // 1. QUẢN LÝ CÔNG DÂN (CITIZEN)
    // ==================================================================================

    override suspend fun getAllCitizens(): List<Citizen> = dbQuery {
        CitizenDao.getAll()
    }

    override suspend fun getCitizenById(id: String): Citizen? = dbQuery {
        CitizenDao.getById(id)
    }

    override suspend fun insertCitizen(citizen: Citizen): Boolean = dbQuery {
        try {
            CitizenDao.insert(citizen)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun updateCitizen(citizen: Citizen): Boolean = dbQuery {
        try {
            CitizenDao.update(citizen)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteCitizen(id: String): Boolean = dbQuery {
        try {
            CitizenDao.delete(id) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Chức năng lọc nâng cao (Thay thế cho logic nối chuỗi SQL cũ)
    override suspend fun filterCitizens(
        id: String?, name: String?, gender: String?,
        dob: String?, hometown: String?
    ): List<Citizen> = dbQuery {
        val query = CitizenTable.selectAll()

        // Áp dụng các điều kiện lọc nếu tham số không null/empty
        id?.takeIf { it.isNotEmpty() }?.let {
            query.andWhere { CitizenTable.citizenId like "%$it%" }
        }
        name?.takeIf { it.isNotEmpty() }?.let {
            query.andWhere { CitizenTable.fullName like "%$it%" }
        }
        gender?.takeIf { it.isNotEmpty() }?.let {
            query.andWhere { CitizenTable.gender eq it }
        }
        dob?.takeIf { it.isNotEmpty() }?.let {
            query.andWhere { CitizenTable.birthDate eq it }
        }
        hometown?.takeIf { it.isNotEmpty() }?.let {
            query.andWhere { CitizenTable.hometown like "%$it%" }
        }

        // Mapping kết quả trả về list Citizen
        query.map { CitizenDao.rowToCitizen(it) }
    }

    // ==================================================================================
    // 2. XÁC THỰC & BẢO MẬT (AUTHENTICATION)
    // ==================================================================================

    override suspend fun getPublicKeyById(citizenId: String): String? = dbQuery {
        CitizenTable.select(CitizenTable.publicKey)
            .where { CitizenTable.citizenId eq citizenId }
            .singleOrNull()
            ?.get(CitizenTable.publicKey)
    }

    override suspend fun updatePublicKey(citizenId: String, publicKey: String): Boolean = dbQuery {
        try {
            CitizenTable.update({ CitizenTable.citizenId eq citizenId }) {
                it[CitizenTable.publicKey] = publicKey
            } > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun isCitizenIdExists(citizenId: String): Boolean = dbQuery {
        !CitizenTable.selectAll().where { CitizenTable.citizenId eq citizenId }.empty()
    }

    override suspend fun getLatestCitizenId(prefix: String): String? = dbQuery {
        CitizenTable.select(CitizenTable.citizenId)
            .where { CitizenTable.citizenId like "$prefix%" } // Quan trọng: like 'ddMMyy%'
            .orderBy(CitizenTable.citizenId, SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.get(CitizenTable.citizenId)
    }

    // ==================================================================================
    // 3. GIẤY PHÉP LÁI XE (DRIVING LICENSE)
    // ==================================================================================

    override suspend fun getDrivingLicenseByCitizenId(citizenId: String): List<DrivingLicense> = dbQuery {
        DrivingLicenseDao.getByCitizenId(citizenId)
    }

    override suspend fun insertDrivingLicense(license: DrivingLicense): Boolean = dbQuery {
        try {
            DrivingLicenseDao.insert(license)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun updateDrivingLicense(license: DrivingLicense): Boolean = dbQuery {
        try {
            DrivingLicenseDao.update(license)
            true // Hàm update của Dao không trả về kết quả nên mặc định true nếu không crash
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteDrivingLicense(citizenId: String): Boolean = dbQuery {
        try {
            DrivingLicenseDao.deleteByCitizenId(citizenId) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun saveDrivingLicense(license: DrivingLicense): Boolean = dbQuery {
        try {
            val list = DrivingLicenseDao.getByCitizenId(license.citizenId)
            val exists = list.any { it.licenseId == license.licenseId }
            if (exists) {
                DrivingLicenseDao.update(license)
            } else {
                DrivingLicenseDao.insert(license)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ==================================================================================
    // 4. ĐĂNG KÝ XE (VEHICLE REGISTER)
    // ==================================================================================

    override suspend fun getVehicleRegisterByCitizenId(citizenId: String): List<VehicleRegister> = dbQuery {
        VehicleRegisterDao.getByCitizenId(citizenId)
    }

    override suspend fun insertVehicleRegister(vehicle: VehicleRegister): Boolean = dbQuery {
        try {
            VehicleRegisterDao.insert(vehicle)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun updateVehicleRegister(vehicle: VehicleRegister): Boolean = dbQuery {
        try {
            VehicleRegisterDao.update(vehicle)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteVehicleRegister(citizenId: String): Boolean = dbQuery {
        try {
            VehicleRegisterDao.deleteByCitizenId(citizenId) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- LOGIC UPSERT (Save) ---
    override suspend fun saveVehicleRegister(vehicle: VehicleRegister): Boolean = dbQuery {
        try {
            val list = VehicleRegisterDao.getByCitizenId(vehicle.citizenId)

            // Logic mới: Kiểm tra trùng ID (Số đăng ký xe)
            val exists = list.any { it.vehicleRegisterId == vehicle.vehicleRegisterId }

            if (exists) {
                VehicleRegisterDao.update(vehicle)
            } else {
                VehicleRegisterDao.insert(vehicle)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ==================================================================================
    // 5. BẢO HIỂM Y TẾ (HEALTH INSURANCE)
    // ==================================================================================

    override suspend fun getHealthInsuranceByCitizenId(citizenId: String): List<HealthInsurance> = dbQuery {
        HealthInsuranceDao.getByCitizenId(citizenId)
    }

    override suspend fun insertHealthInsurance(insurance: HealthInsurance): Boolean = dbQuery {
        try {
            HealthInsuranceDao.insert(insurance)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun updateHealthInsurance(insurance: HealthInsurance): Boolean = dbQuery {
        try {
            HealthInsuranceDao.update(insurance)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteHealthInsurance(citizenId: String): Boolean = dbQuery {
        try {
            HealthInsuranceDao.deleteByCitizenId(citizenId) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- LOGIC UPSERT (Save) ---
    override suspend fun saveHealthInsurance(insurance: HealthInsurance): Boolean = dbQuery {
        try {
            val list = HealthInsuranceDao.getByCitizenId(insurance.citizenId)
            // Kiểm tra mã số thẻ bảo hiểm
            val exists = list.any { it.insuranceId == insurance.insuranceId }

            if (exists) {
                HealthInsuranceDao.update(insurance)
            } else {
                HealthInsuranceDao.insert(insurance)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}