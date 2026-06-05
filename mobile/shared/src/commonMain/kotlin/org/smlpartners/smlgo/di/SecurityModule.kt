package org.smlpartners.smlgo.di

import org.smlpartners.smlgo.core.security.SecureStorage
import org.smlpartners.smlgo.core.security.createSettings
import org.koin.dsl.module

val securityModule = module {
    single { createSettings() }
    single { SecureStorage(get()) }
}