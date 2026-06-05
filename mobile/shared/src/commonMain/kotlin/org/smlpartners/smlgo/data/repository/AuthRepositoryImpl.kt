package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.core.security.SecureStorage
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.remote.api.AuthApiService
import org.smlpartners.smlgo.domain.model.User
import org.smlpartners.smlgo.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api           : AuthApiService,
    private val secureStorage : SecureStorage
) : AuthRepository {

    override suspend fun login(username: String, password: String): ApiResult<User> =
        safeApiCall {
            // 1. Obtiene el token
            val tokenDto = api.login(username, password)
            secureStorage.saveToken(tokenDto.accessToken)

            // 2. Obtiene los datos del usuario
            val userDto = api.getMe()
            val user    = userDto.toDomain()

            // 3. Guarda sesión básica
            secureStorage.saveUserSession(
                id    = user.id,
                email = user.email,
                name  = "${user.firstName} ${user.firstSurname}"
            )
            user
        }

    override suspend fun logout() {
        secureStorage.clearSession()
    }

    override suspend fun register(
        username: String,
        password: String
    ): ApiResult<User> = safeApiCall {
        api.register(username, password).toDomain()
    }

    override fun isLoggedIn(): Boolean = secureStorage.isLoggedIn()

    override fun getCurrentUserId(): Int? = secureStorage.getUserId()
}