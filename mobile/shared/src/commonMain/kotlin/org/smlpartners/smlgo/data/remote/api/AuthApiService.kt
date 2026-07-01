package org.smlpartners.smlgo.data.remote.api

import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import org.smlpartners.smlgo.data.remote.dto.*
import org.smlpartners.smlgo.core.network.HttpClientManager

class AuthApiService(private val manager: HttpClientManager) {
    private val client get() = manager.client

    /**
     * El backend responde con Set-Cookie: access_token=<jwt>; HttpOnly
     * Extraemos el JWT del header y lo devolvemos como String.
     * El body JSON (LoginResponseDto) se descarta — los roles se obtienen en getMe().
     */
    suspend fun login(username: String, password: String): String {
        val response = client.submitForm(
            url = "auth/login",
            formParameters = Parameters.build {
                append("username", username)
                append("password", password)
                append("grant_type", "password")
            }
        )
        // Set-Cookie puede venir como múltiples headers; buscamos el de access_token
        val setCookieHeaders = response.headers.getAll(HttpHeaders.SetCookie) ?: emptyList()
        return setCookieHeaders
            .firstOrNull { it.trimStart().startsWith("access_token=") }
            ?.substringAfter("access_token=")
            ?.substringBefore(";")
            ?.trim()
            ?: throw Exception("El servidor no devolvió la cookie de autenticación")
    }

    suspend fun register(username: String, password: String): UserDto =
        client.post("users/") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(username, password))
        }.body()

    /**
     * Sin parámetro token — el header Cookie: access_token=<jwt>
     * se inyecta automáticamente por HttpClientFactory en cada request.
     */
    suspend fun getMe(): MyProfileDto =
        client.get("users/me").body()

    suspend fun updateUser(id: Int, update: UserUpdateDto): UserDto =
        client.put("users/$id") {
            contentType(ContentType.Application.Json)
            setBody(update)
        }.body()

    suspend fun getActiveUsers(): List<UserDto> =
        client.get("users/active").body()
}