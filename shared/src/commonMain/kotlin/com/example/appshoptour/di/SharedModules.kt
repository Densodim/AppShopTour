package com.example.appshoptour.di

import com.example.appshoptour.data.remote.UserApiClient
import com.example.appshoptour.data.remote.createHttpClient
import com.example.appshoptour.data.repository.UserRepositoryImpl
import com.example.appshoptour.domain.repository.UserRepository
import com.example.appshoptour.domain.usecase.GetUsersUseCase
import com.example.appshoptour.presentation.users.UsersViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Data слой: HTTP клиент, API clients, репозитории.
 * single = один экземпляр на всё время жизни приложения.
 * baseUrl передаётся снаружи (из BuildConfig на Android, из конфига на других платформах).
 */
fun dataModule(baseUrl: String) = module {
    single { createHttpClient() }
    single { UserApiClient(get(), baseUrl) }
    singleOf(::UserRepositoryImpl) bind UserRepository::class
}

/**
 * Domain слой: use cases.
 * factory = новый экземпляр при каждом запросе (use cases stateless).
 */
val domainModule = module {
    factoryOf(::GetUsersUseCase)
}

/**
 * Presentation слой: ViewModels.
 * factory = новый ViewModel при каждом запросе (жизненный цикл управляется экраном).
 */
val presentationModule = module {
    factoryOf(::UsersViewModel)
}

/**
 * Список всех общих модулей — передаётся в startKoin() на каждой платформе.
 * baseUrl приходит из платформенного кода (BuildConfig.BASE_URL на Android).
 */
fun sharedModules(baseUrl: String) = listOf(dataModule(baseUrl), domainModule, presentationModule)
