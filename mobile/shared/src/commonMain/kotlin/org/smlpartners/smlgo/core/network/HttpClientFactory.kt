package org.smlpartners.smlgo.core.network

import com.smlpartners.smlgo.core.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.smlpartners.smlgo.core.utils.platformName

fun createHttpClient(
    tokenProvider: () -> String?,
    onTokenExpired: () -> Unit = {}   // ← callback para cerrar sesión si expira
): HttpClient {
    return HttpClient(httpClientEngine()) {
        install(ContentNegotiation) {
            // ── Serialización ────────────────────────────────────────────
            json(
                Json {
                    prettyPrint = true // tolera campos nuevos del backend
                    isLenient = true // acepta JSON malformado levemente
                    ignoreUnknownKeys = true
                }
            )
        }

        // ── Timeouts ────────────────────────────────────────────
        install(HttpTimeout) {
            connectTimeoutMillis = BuildConfig.CONNECT_TIMEOUT
            requestTimeoutMillis = BuildConfig.READ_TIMEOUT
            socketTimeoutMillis = BuildConfig.WRITE_TIMEOUT
        }

        // ── Retry automático ─────────────────────────────────────────
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            retryOnException(maxRetries = 3, retryOnTimeout = true)
            exponentialDelay(base = 2.0, maxDelayMs = 10_000)
        }

        // ── JWT Bearer token ─────────────────────────────────────────
        install(Auth) {
            bearer {
                loadTokens {
                    val token = tokenProvider() ?: return@loadTokens null
                    BearerTokens(accessToken = token, refreshToken = "")
                }
                // Si el token expira (401), limpiamos sesión
                refreshTokens { null }
                // Solo adjunta el header en rutas que no sean login
                sendWithoutRequest { request ->
                    request.url.host == BuildConfig.BASE_URL
                        .removePrefix("https://")
                        .removePrefix("http://")
                        .substringBefore("/")
                }
            }
        }

        install(DefaultRequest) {
            val base = BuildConfig.BASE_URL
            url(if (base.endsWith("/")) base else "$base/")
            header("Accept", "application/json")
            header("X-Platform", platformName)
        }

        // ── Logging (solo debug) ──────────────────────────────────────
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