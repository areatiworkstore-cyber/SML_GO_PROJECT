package org.smlpartners.smlgo.core.network

import com.smlpartners.smlgo.core.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.smlpartners.smlgo.core.utils.platformName

/**
 * Crea el HttpClient de Ktor para autenticación por cookie.
 *
 * El backend usa cookies HttpOnly: en cada request se inyecta el header
 * "Cookie: access_token=<jwt>" obtenido del tokenProvider(), simulando el
 * comportamiento del navegador. El plugin Auth (Bearer) ya no se usa.
 *
 * @param tokenProvider  lambda que devuelve el JWT guardado en SecureStorage
 * @param onTokenExpired callback para cerrar sesión cuando el backend retorna 401
 */
fun createHttpClient(
    tokenProvider: () -> String?,
    onTokenExpired: () -> Unit = {}
): HttpClient {
    return HttpClient(httpClientEngine()) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint        = true
                    isLenient          = true
                    ignoreUnknownKeys  = true
                }
            )
        }

        install(HttpTimeout) {
            connectTimeoutMillis = BuildConfig.CONNECT_TIMEOUT
            requestTimeoutMillis = BuildConfig.READ_TIMEOUT
            socketTimeoutMillis  = BuildConfig.WRITE_TIMEOUT
        }

        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            retryOnException(maxRetries = 3, retryOnTimeout = true)
            exponentialDelay(base = 2.0, maxDelayMs = 10_000)
        }

        // ── Cookie-based auth — el tokenProvider() se evalúa por cada request ──
        install(DefaultRequest) {
            val base = BuildConfig.BASE_URL
            url(if (base.endsWith("/")) base else "$base/")
            header("Accept", "application/json")
            header("X-Platform", platformName)
            // Inyectar cookie si hay sesión activa
            tokenProvider()?.takeIf { it.isNotBlank() }?.let { token ->
                header("Cookie", "access_token=$token")
            }
        }

        // ── Logging (solo en debug) ───────────────────────────────────────────
        if (BuildConfig.IS_DEBUG) {
            install(Logging) {
                level  = LogLevel.BODY
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[SMLGo-HTTP] $message")
                    }
                }
            }
        }
    }
}