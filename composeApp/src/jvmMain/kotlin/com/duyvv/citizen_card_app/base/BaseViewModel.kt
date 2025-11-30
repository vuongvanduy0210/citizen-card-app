package com.duyvv.citizen_card_app.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus

open class BaseViewModel<UiStateType : UiState>(
    initialUiState: UiStateType
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow(initialUiState)
    val uiStateFlow = _uiStateFlow.asStateFlow()

    val uiState: UiStateType
        get() = _uiStateFlow.value

    protected fun updateUiState(function: (state: UiStateType) -> UiStateType) {
        _uiStateFlow.update(function)
    }

    private fun handleException(throwable: Throwable) {
        println("ViewModelException: ${throwable.message}")
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleException(throwable)
    }

    val viewModelHandlerScope by lazy {
        viewModelScope + exceptionHandler
    }
}

interface UiState

