package org.smlpartners.smlgo.data.remote.api

import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import org.smlpartners.smlgo.data.remote.dto.*
import org.smlpartners.smlgo.core.network.HttpClientManager

class AuthApiService(private val manager: HttpClientManager) {
    private val client get() = manager.client

    suspend fun login(username: String, password: String): TokenResponseDto {
        val response = client.submitForm(
            url = "auth/login",
            formParameters = Parameters.build {
                append("username", username)
                append("password", password)
                append("grant_type", "password")
            }
        )
        val body = response.body<TokenResponseDto>()
        return body
    }

    suspend fun register(username: String, password: String): UserDto =
        client.post("users/") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(username, password))
        }.body()
    suspend fun getMe(token: String? = null): MyProfileDto =
        client.get("users/me") {
            token?.let {
                headers {
                    append("Authorization", "Bearer $it")
                }
            }
        }.body()

    suspend fun updateUser(id: Int, update: UserUpdateDto): UserDto =
        client.put("users/$id") {
            contentType(ContentType.Application.Json)
            setBody(update)
        }.body()
}