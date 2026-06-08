package org.smlpartners.smlgo.core.network

import org.smlpartners.smlgo.core.security.SecureStorage

class HttpClientManager(
    private val secureStorage  : SecureStorage,
    private val onTokenExpired : () -> Unit
) {
    // El cliente se recrea cuando cambia el token
    var client = buildClient()
        private set

    fun recreate() {
        client  = buildClient()   // ← solo reemplaza
    }

    private fun buildClient() = createHttpClient(
        tokenProvider  = { secureStorage.getToken() },
        onTokenExpired = onTokenExpired
    )
}