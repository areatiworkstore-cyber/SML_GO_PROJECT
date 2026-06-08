package org.smlpartners.smlgo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.smlpartners.smlgo.core.error.AppError
import org.smlpartners.smlgo.core.error.GlobalErrorHandler
import org.smlpartners.smlgo.core.error.toAppError
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Profile
import org.smlpartners.smlgo.domain.repository.AuthRepository
import org.smlpartners.smlgo.domain.usecase.auth.IsLoggedInUseCase
import org.smlpartners.smlgo.domain.usecase.auth.LoginUseCase
import org.smlpartners.smlgo.domain.usecase.auth.LogoutUseCase

data class LoginUiState(
    val isLoading       : Boolean = false,
    val isLoggedIn      : Boolean = false,
    val user            : Profile?   = null,
    val usernameError   : String? = null,
    val passwordError   : String? = null,
    val error           : String? = null
)

class LoginViewModel(
    private val loginUseCase    : LoginUseCase,
    private val logoutUseCase   : LogoutUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val authRepository  : AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isLoggedInFlow.collectLatest { loggedIn ->
                println("[LoginViewModel] Flow update isLoggedIn: $loggedIn")
                _uiState.update { it.copy(isLoggedIn = loggedIn) }
            }
        }
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
                is ApiResult.Error -> {
                    val appError = result.exception.toAppError()
                    when (appError) {
                        // Credenciales incorrectas → error local en pantalla
                        is AppError.Unauthorized -> _uiState.update {
                            it.copy(isLoading = false, error = appError.toUserMessage())
                        }
                        // Problema técnico → toast global
                        else -> {
                            GlobalErrorHandler.emit(appError)
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = LoginUiState(isLoggedIn = false)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}