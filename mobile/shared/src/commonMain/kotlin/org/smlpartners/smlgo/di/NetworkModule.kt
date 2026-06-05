package org.smlpartners.smlgo.di

import org.smlpartners.smlgo.core.network.createHttpClient
import org.smlpartners.smlgo.core.security.SecureStorage
import org.koin.dsl.module

val networkModule = module {
    single {
        val secureStorage = get<SecureStorage>()
        createHttpClient(
            tokenProvider = { secureStorage.getToken() },
            onTokenExpired = { secureStorage.clearSession() }
        )
    }
}