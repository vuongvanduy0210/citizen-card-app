package com.duyvv.citizen_card_app.di

import com.duyvv.citizen_card_app.data.repository.DataRepositoryImpl
import com.duyvv.citizen_card_app.data.repository.JavaCardRepositoryImpl
import com.duyvv.citizen_card_app.domain.repository.DataRepository
import com.duyvv.citizen_card_app.domain.repository.JavaCardRepository
import com.duyvv.citizen_card_app.presentation.home.HomeViewModel
import com.duyvv.citizen_card_app.presentation.home.ManageCitizenViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::JavaCardRepositoryImpl).bind<JavaCardRepository>()
    singleOf(::DataRepositoryImpl).bind<DataRepository>()
}

val viewModelModule = module {
    single { HomeViewModel(get(), get()) }
    viewModel { ManageCitizenViewModel(get()) }
}

val appModule = listOf(
    dataModule,
    viewModelModule
)