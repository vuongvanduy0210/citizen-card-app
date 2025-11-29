package com.duyvv.citizen_card_app.di

import com.duyvv.citizen_card_app.data.repository.JavaCardRepository
import com.duyvv.citizen_card_app.data.repository.JavaCardRepositoryImpl
import com.duyvv.citizen_card_app.presentation.home.HomeViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::JavaCardRepositoryImpl).bind<JavaCardRepository>()
}

val viewModelModule = module {
    factory { HomeViewModel(get()) }
}

val appModule = listOf(
    dataModule,
    viewModelModule
)