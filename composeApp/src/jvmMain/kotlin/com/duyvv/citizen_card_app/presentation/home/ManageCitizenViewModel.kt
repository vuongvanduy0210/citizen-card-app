package com.duyvv.citizen_card_app.presentation.home

import androidx.lifecycle.viewModelScope
import com.duyvv.citizen_card_app.base.BaseViewModel
import com.duyvv.citizen_card_app.base.UiState
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.domain.repository.DataRepository
import kotlinx.coroutines.launch

class ManageCitizenViewModel(
    private val repository: DataRepository
) : BaseViewModel<ManageCitizenUIState>(ManageCitizenUIState()) {

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelHandlerScope.launch {
            val data = repository.getAllCitizens()
            updateUiState { it.copy(citizens = data) }
        }
    }

    fun searchCitizens() {
        viewModelScope.launch {
            val data = repository.filterCitizens(
                id = uiState.filterId,
                name = uiState.filterName,
                gender = if (uiState.filterGender == "Giới tính") null else uiState.filterGender,
                hometown = uiState.filterHometown,
                dob = uiState.filterDob
            )
            updateUiState { it.copy(citizens = data) }
        }
    }

    fun clearFilters() {
        updateUiState { it.refresh() }
    }

    fun deleteCitizen(id: String) {
        viewModelScope.launch {
            val success = repository.deleteCitizen(id)
            if (success) refreshData()
        }
    }

    fun updateFilterId(value: String) {
        updateUiState { it.copy(filterId = value) }
    }

    fun updateFilterName(value: String) {
        updateUiState { it.copy(filterName = value) }
    }

    fun updateFilterGender(value: String) {
        updateUiState { it.copy(filterGender = value) }
    }

    fun updateFilterHometown(value: String) {
        updateUiState { it.copy(filterHometown = value) }
    }

    fun updateFilterDob(value: String) {
        updateUiState { it.copy(filterDob = value) }
    }
}

data class ManageCitizenUIState(
    val citizens: List<Citizen> = emptyList(),
    var filterId: String = "",
    var filterName: String = "",
    var filterGender: String = "",
    var filterHometown: String = "",
    var filterDob: String = "",
    val isShowDialogInfoCitizen: Boolean = false,
    val selectedCitizen: Citizen? = null,
) : UiState {
    fun refresh() = copy(
        filterId = "",
        filterName = "",
        filterGender = "",
        filterHometown = "",
        filterDob = ""
    )
}