package org.smlpartners.smlgo.di

import org.koin.core.context.startKoin

// Función que Swift puede llamar directamente
fun doInitKoin() {
    startKoin {
        modules(allModules())
    }
}