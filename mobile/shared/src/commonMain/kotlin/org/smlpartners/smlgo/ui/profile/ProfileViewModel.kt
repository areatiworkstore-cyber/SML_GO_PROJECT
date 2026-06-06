package org.smlpartners.smlgo.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.smlpartners.smlgo.core.security.SecureStorage
import org.smlpartners.smlgo.domain.usecase.auth.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userName    : String?  = null,
    val userEmail   : String?  = null,
    val isLoggedOut : Boolean  = false
)

class ProfileViewModel(
    private val logoutUseCase  : LogoutUseCase,
    private val secureStorage  : SecureStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                userName  = secureStorage.getUserName(),
                userEmail = secureStorage.getUserEmail()
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}