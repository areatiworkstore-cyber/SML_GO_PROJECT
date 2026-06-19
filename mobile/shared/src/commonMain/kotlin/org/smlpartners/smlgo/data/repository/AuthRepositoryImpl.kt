package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.core.security.SecureStorage
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.mapper.toUpdateDto
import org.smlpartners.smlgo.data.remote.api.AuthApiService
import org.smlpartners.smlgo.domain.model.Profile
import org.smlpartners.smlgo.domain.model.User
import org.smlpartners.smlgo.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.smlpartners.smlgo.core.network.HttpClientManager
import org.smlpartners.smlgo.data.mapper.toDomainUser
import org.smlpartners.smlgo.domain.model.DocumentType

class AuthRepositoryImpl(
    private val api           : AuthApiService,
    private val secureStorage : SecureStorage,
    private val httpClientManager: HttpClientManager
) : AuthRepository {

    private val _isLoggedInFlow = MutableStateFlow(secureStorage.isLoggedIn())
    override val isLoggedInFlow: StateFlow<Boolean> = _isLoggedInFlow.asStateFlow()

    override suspend fun login(username: String, password: String): ApiResult<Profile> {
        val result = safeApiCall {
            val tokenDto = api.login(username, password)
            val token    = tokenDto.accessToken ?: ""
            if (token.isBlank()) throw Exception("Token inválido")

            // Guarda el token
            secureStorage.saveToken(token)

            // Obtiene el perfil con el token explícito — solo esta vez
            val myProfile = api.getMe(token)
            val firstName = myProfile.firstName ?: "Vendedor"
            val surname   = myProfile.firstSurname ?: ""
            
            println("[AuthRepo] getMe response: id=${myProfile.id}, name=$firstName")

            secureStorage.saveUserSession(
                id   = myProfile.id,
                code = myProfile.code,
                name = "$firstName $surname".trim()
            )

            _isLoggedInFlow.value = true

            Profile(
                id             = myProfile.id,
                code           = myProfile.code,
                firstName      = firstName,
                secondName     = myProfile.secondName ?: "",
                firstSurname   = surname,
                secondSurname  = myProfile.secondSurname ?: "",
                documentType   = myProfile.documentType?.toDomain(),
                documentNumber = myProfile.documentNumber ?: "",
                cellphone      = myProfile.cellphone ?: "",
                email          = myProfile.email ?: "",
                roles          = myProfile.roles.map { it.toDomain() }
            )
        }

        // ← FUERA del safeApiCall — el cliente viejo ya terminó su trabajo
        if (result is ApiResult.Success) {
            httpClientManager.recreate()
        }

        return result
    }

    override suspend fun logout() {
        secureStorage.clearSession()
        _isLoggedInFlow.value = false
        httpClientManager.recreate()
    }

    override suspend fun register(
        username: String,
        password: String
    ): ApiResult<User> = safeApiCall {
        api.register(username, password).toDomain()
    }

    override suspend fun updateUser(id: Int, user: User): ApiResult<User> = safeApiCall {
        api.updateUser(id, user.toUpdateDto()).toDomain()
    }

    override suspend fun getFullUser(): ApiResult<User> =
        safeApiCall {
            api.getMe().toDomainUser()   // ← users/me con campos completos
        }

    override fun isLoggedIn(): Boolean = secureStorage.isLoggedIn()

    override fun getCurrentUserId(): Int? = secureStorage.getUserId()
}