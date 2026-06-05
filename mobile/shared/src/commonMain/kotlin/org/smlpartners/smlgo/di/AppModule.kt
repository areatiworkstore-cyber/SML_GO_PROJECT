package org.smlpartners.smlgo.di

import org.koin.core.context.startKoin

fun initKoin(extraModules: List<org.koin.core.module.Module> = emptyList()) {
    startKoin {
        modules(
            securityModule,
            networkModule,
            repositoryModule,
            *extraModules.toTypedArray()
        )
    }
}