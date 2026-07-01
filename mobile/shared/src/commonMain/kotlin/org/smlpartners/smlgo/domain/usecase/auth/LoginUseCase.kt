package org.smlpartners.smlgo.domain.usecase.auth

import org.smlpartners.smlgo.core.network.ApiError
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Profile
import org.smlpartners.smlgo.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        username: String,
        password: String
    ): ApiResult<Profile> {
        if (username.isBlank()) return ApiResult.Error(
            ApiError.UnknownError("El usuario no puede estar vacío")
        )
        if (password.isBlank()) return ApiResult.Error(
            ApiError.UnknownError("La contraseña no puede estar vacía")
        )
        return repository.login(username.trim(), password)
    }
}

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}

class IsLoggedInUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Boolean = repository.isLoggedIn()
}

/**
 * Devuelve los roles del usuario desde SecureStorage, sin llamada a red.
 * Útil para guards de navegación y verificaciones de permisos en la UI.
 */
class GetCurrentRolesUseCase(private val repository: AuthRepository) {
    operator fun invoke(): List<String> = repository.getCurrentRoles()
}