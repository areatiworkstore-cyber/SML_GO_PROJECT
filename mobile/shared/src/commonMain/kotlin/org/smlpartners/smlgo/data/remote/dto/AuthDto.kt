package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

/**
 * El backend ahora responde con una cookie HttpOnly y este body JSON.
 * El access_token viaja en el header Set-Cookie, NO en el body.
 */
@Serializable
data class LoginResponseDto(
    @SerialName("message") val message: String = "",
    @SerialName("roles")   val roles: List<String> = emptyList()
)