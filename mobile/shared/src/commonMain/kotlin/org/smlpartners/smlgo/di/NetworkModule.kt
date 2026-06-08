package org.smlpartners.smlgo.di

import org.smlpartners.smlgo.core.security.SecureStorage
import org.koin.dsl.module
import org.smlpartners.smlgo.core.network.HttpClientManager

val networkModule = module {
    single {
        HttpClientManager(
            secureStorage = get(),
            onTokenExpired = { get<SecureStorage>().clearSession() }
        )
    }
}