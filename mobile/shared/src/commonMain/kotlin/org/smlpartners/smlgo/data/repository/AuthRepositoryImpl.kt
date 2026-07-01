package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.core.security.SecureStorage
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.mapper.toUpdateDto
import org.smlpartners.smlgo.data.mapper.toDomainUser
import org.smlpartners.smlgo.data.remote.api.AuthApiService
import org.smlpartners.smlgo.domain.model.Profile
import org.smlpartners.smlgo.domain.model.Role
import org.smlpartners.smlgo.domain.model.User
import org.smlpartners.smlgo.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.smlpartners.smlgo.core.network.HttpClientManager

class AuthRepositoryImpl(
    private val api              : AuthApiService,
    private val secureStorage    : SecureStorage,
    private val httpClientManager: HttpClientManager
) : AuthRepository {

    private val _isLoggedInFlow = MutableStateFlow(secureStorage.isLoggedIn())
    override val isLoggedInFlow: StateFlow<Boolean> = _isLoggedInFlow.asStateFlow()

    override suspend fun login(username: String, password: String): ApiResult<Profile> {
        val result = safeApiCall {
            // 1. Login → extrae JWT del header Set-Cookie
            val token = api.login(username, password)
            if (token.isBlank()) throw Exception("Token inválido recibido del servidor")

            // 2. Guarda el token antes de llamar a /users/me
            secureStorage.saveToken(token)

            // 3. Recrea el cliente para que DefaultRequest ya inyecte la cookie
            httpClientManager.recreate()

            // 4. Obtiene el perfil completo con el cliente actualizado
            val myProfile = api.getMe()
            val firstName = myProfile.firstName ?: "Usuario"
            val surname   = myProfile.firstSurname ?: ""

            println("[AuthRepo] getMe → id=${myProfile.id}, name=$firstName $surname")

            // 5. Persiste datos de sesión + roles
            secureStorage.saveUserSession(
                id   = myProfile.id,
                code = myProfile.code,
                name = "$firstName $surname".trim()
            )
            secureStorage.saveRoles(myProfile.roles)

            _isLoggedInFlow.value = true

            // 6. Construye el Profile del dominio (roles como List<String> → List<Role>)
            Profile(
                id             = myProfile.id,
                code           = myProfile.code,
                firstName      = firstName,
                secondName     = myProfile.secondName     ?: "",
                firstSurname   = surname,
                secondSurname  = myProfile.secondSurname  ?: "",
                documentType   = myProfile.documentType.toDomain(),
                documentNumber = myProfile.documentNumber ?: "",
                cellphone      = myProfile.cellphone      ?: "",
                email          = myProfile.email          ?: "",
                roles          = myProfile.roles.map { Role(role = it) }
            )
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

    override suspend fun getFullUser(): ApiResult<User> = safeApiCall {
        api.getMe().toDomainUser()
    }

    override suspend fun getActiveUsers(): ApiResult<List<User>> =
        safeApiCall {
            api.getActiveUsers().map { it.toDomain() }
        }

    override fun isLoggedIn(): Boolean = secureStorage.isLoggedIn()

    override fun getCurrentUserId(): Int? = secureStorage.getUserId()

    override fun getCurrentRoles(): List<String> = secureStorage.getUserRoles()
}