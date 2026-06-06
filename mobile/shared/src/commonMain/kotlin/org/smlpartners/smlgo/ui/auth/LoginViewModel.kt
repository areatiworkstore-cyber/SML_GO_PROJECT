package org.smlpartners.smlgo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.User
import org.smlpartners.smlgo.domain.usecase.auth.LoginUseCase
import org.smlpartners.smlgo.domain.usecase.auth.LogoutUseCase
import org.smlpartners.smlgo.domain.usecase.auth.IsLoggedInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading       : Boolean = false,
    val isLoggedIn      : Boolean = false,
    val user            : User?   = null,
    val usernameError   : String? = null,
    val passwordError   : String? = null,
    val error           : String? = null
)

class LoginViewModel(
    private val loginUseCase    : LoginUseCase,
    private val logoutUseCase   : LogoutUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isLoggedIn = isLoggedInUseCase()) }
    }

    fun login(username: String, password: String) {
        // Validación local antes de llamar al use case
        var hasError = false
        if (username.isBlank()) {
            _uiState.update { it.copy(usernameError = "El usuario es obligatorio") }
            hasError = true
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "La contraseña es obligatoria") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, usernameError = null, passwordError = null) }
            when (val result = loginUseCase(username, password)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, isLoggedIn = true, user = result.data)
                }
                is ApiResult.Error   -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { LoginUiState() }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}