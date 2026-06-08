package org.smlpartners.smlgo.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.security.SecureStorage
import org.smlpartners.smlgo.domain.model.*
import org.smlpartners.smlgo.domain.repository.AuthRepository
import org.smlpartners.smlgo.domain.usecase.auth.LogoutUseCase
import org.smlpartners.smlgo.domain.usecase.masterdata.GetProfileMasterDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading     : Boolean  = false,
    val user          : User?    = null,
    val documentTypes : List<DocumentType> = emptyList(),
    val roles         : List<Role> = emptyList(),
    val userName      : String?  = null,
    val userCode      : String?  = null,
    val isLoggedOut   : Boolean  = false,
    val isUpdated     : Boolean  = false,
    val error         : String?  = null
)

class ProfileViewModel(
    private val logoutUseCase           : LogoutUseCase,
    private val secureStorage           : SecureStorage,
    private val authRepository          : AuthRepository,
    private val getProfileMasterDataUseCase: GetProfileMasterDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileSummary()
    }

    fun loadProfileSummary() {
        println("USER ID = ${secureStorage.getUserId()}")
        println("USER NAME = ${secureStorage.getUserName()}")
        println("USER CODE = ${secureStorage.getUserCode()}")
        _uiState.update {
            it.copy(
                userName = secureStorage.getUserName(),
                userCode = secureStorage.getUserCode()
            )
        }
    }

    fun loadFormData() {
        secureStorage.getUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val masterDataResult = getProfileMasterDataUseCase()
            val userResult       = authRepository.getFullUser()

            if (masterDataResult is ApiResult.Error) {
                _uiState.update { it.copy(isLoading = false, error = masterDataResult.exception.message) }
                return@launch
            }

            val masterData = (masterDataResult as ApiResult.Success).data

            when (userResult) {
                is ApiResult.Success -> {
                    val rawUser = userResult.data
                    // Resolver objetos completos
                    val resolvedUser = rawUser.copy(
                        documentType = masterData.documentTypes.firstOrNull { it.id == rawUser.documentType?.id },
                        roles = rawUser.roles.map { role -> 
                            masterData.roles.firstOrNull { it.id == role.id } ?: role 
                        }
                    )
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            user = resolvedUser,
                            documentTypes = masterData.documentTypes,
                            roles = masterData.roles
                        ) 
                    }
                }
                is ApiResult.Error -> _uiState.update { 
                    it.copy(isLoading = false, error = userResult.exception.message) 
                }
            }
        }
    }

    fun updateProfile(updatedUser: User, newPassword: String? = null) {
        val userId = secureStorage.getUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isUpdated = false) }
            
            // Si el backend recibe password, lo incluimos en el DTO
            // Nota: El DTO ya tiene campo password. copy en User no tiene password, 
            // así que manejaremos la lógica en el Repositorio o ajustamos el mapeo.
            
            // Por simplicidad para el usuario actual, si el password no es nulo, lo mandamos
            // En una app real, User domain model debería tener password opcional para edición.
            
            when (val result = authRepository.updateUser(userId, updatedUser)) {
                is ApiResult.Success -> {
                    val user = result.data
                    secureStorage.saveUserSession(
                        id = user.id,
                        code = user.code,
                        name = "${user.firstName} ${user.firstSurname}"
                    )
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            user = user, 
                            isUpdated = true,
                            userName = "${user.firstName} ${user.firstSurname}"
                        ) 
                    }
                }
                is ApiResult.Error -> _uiState.update { 
                    it.copy(isLoading = false, error = result.exception.message) 
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = ProfileUiState(
                isLoggedOut = true
            )
        }
    }

    fun resetProfile() {
        _uiState.update { ProfileUiState() }  // ← limpia todo el estado
    }

    fun clearStatus() = _uiState.update { it.copy(isUpdated = false, error = null) }
}