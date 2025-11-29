package com.duyvv.citizen_card_app.domain.repository

import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.data.local.entity.DrivingLicense
import com.duyvv.citizen_card_app.data.local.entity.HealthInsurance
import com.duyvv.citizen_card_app.data.local.entity.VehicleRegister

interface DataRepository {
    // --- Citizen ---
    suspend fun getAllCitizens(): List<Citizen>
    suspend fun getCitizenById(id: String): Citizen?
    suspend fun insertCitizen(citizen: Citizen): Boolean
    suspend fun updateCitizen(citizen: Citizen): Boolean
    suspend fun deleteCitizen(id: String): Boolean
    suspend fun filterCitizens(
        id: String?, name: String?, gender: String?, 
        dob: String?, hometown: String?
    ): List<Citizen>
    
    // --- Authentication ---
    suspend fun getPublicKeyById(citizenId: String): String?
    suspend fun updatePublicKey(citizenId: String, publicKey: String): Boolean
    suspend fun isCitizenIdExists(citizenId: String): Boolean
    suspend fun getLatestCitizenId(): String?

    // --- Driving License ---
    suspend fun getDrivingLicenseByCitizenId(citizenId: String): List<DrivingLicense>
    suspend fun insertDrivingLicense(license: DrivingLicense): Boolean
    suspend fun updateDrivingLicense(license: DrivingLicense): Boolean
    suspend fun deleteDrivingLicense(citizenId: String): Boolean // Xóa theo citizenId

    // --- Vehicle Register ---
    suspend fun getVehicleRegisterByCitizenId(citizenId: String): List<VehicleRegister>
    suspend fun insertVehicleRegister(vehicle: VehicleRegister): Boolean
    suspend fun updateVehicleRegister(vehicle: VehicleRegister): Boolean
    suspend fun deleteVehicleRegister(citizenId: String): Boolean

    // --- Health Insurance ---
    suspend fun getHealthInsuranceByCitizenId(citizenId: String): List<HealthInsurance>
    suspend fun insertHealthInsurance(insurance: HealthInsurance): Boolean
    suspend fun updateHealthInsurance(insurance: HealthInsurance): Boolean
    suspend fun deleteHealthInsurance(citizenId: String): Boolean
}