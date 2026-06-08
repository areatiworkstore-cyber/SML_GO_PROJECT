package org.smlpartners.smlgo

import android.app.Application
import org.smlpartners.smlgo.appContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.smlpartners.smlgo.di.allModules

class SMLGoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@SMLGoApplication)
            modules(allModules())
        }
    }
}