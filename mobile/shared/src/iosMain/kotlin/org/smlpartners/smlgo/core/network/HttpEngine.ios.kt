package org.smlpartners.smlgo.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun httpClientEngine(): HttpClientEngine = Darwin.create {
    configureRequest {
        setTimeoutInterval(30.0)
        setAllowsCellularAccess(true)
    }
}