package org.smlpartners.smlgo.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import org.smlpartners.smlgo.data.remote.dto.LoginRequestDto
import org.smlpartners.smlgo.data.remote.dto.TokenResponseDto
import org.smlpartners.smlgo.data.remote.dto.UserDto

class AuthApiService(private val client: HttpClient) {

    suspend fun login(username: String, password: String): TokenResponseDto =
        client.submitForm(
            url = "/auth/login",
            formParameters = Parameters.build {
                append("username", username)
                append("password", password)
                append("grant_type", "password")
            }
        ).body()

    suspend fun register(username: String, password: String): UserDto =
        client.post("/users/") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(username, password))
        }.body()

    suspend fun getMe(): UserDto =
        client.get("/auth/me").body()
}