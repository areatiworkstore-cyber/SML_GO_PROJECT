package org.smlpartners.smlgo.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module

// Función que devuelve todos los módulos para usar desde Android y iOS
fun allModules(): List<Module> = listOf(
    securityModule,
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)

// Para iOS que llama initKoin directamente
fun initKoin(extraModules: List<Module> = emptyList()) {
    startKoin {
        modules(allModules() + extraModules)
    }
}