package org.smlpartners.smlgo.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.*
import org.smlpartners.smlgo.domain.usecase.client.*
import org.smlpartners.smlgo.domain.usecase.masterdata.GetClientFormMasterDataUseCase
import org.smlpartners.smlgo.domain.usecase.geography.*
import org.smlpartners.smlgo.domain.usecase.auth.GetActiveUsersUseCase
import org.smlpartners.smlgo.domain.usecase.auth.GetFullUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.smlpartners.smlgo.core.error.AppError
import org.smlpartners.smlgo.core.error.GlobalErrorHandler
import org.smlpartners.smlgo.core.error.toAppError

data class ClientListUiState(
    val isLoading : Boolean       = true,
    val clients   : List<Client>  = emptyList(),
    val isAdmin   : Boolean       = false,
    val employees : List<User>    = emptyList(),
    val selectedEmployee: User?   = null,
    val isLoadingEmployees: Boolean = false
)

data class ClientFormUiState(
    val isLoading      : Boolean          = true,
    val isLocating     : Boolean          = false,
    val isSaved        : Boolean          = false,
    val client         : Client?          = null,
    val clientCode     : NextCode?        = null,
    val documentTypes  : List<DocumentType> = emptyList(),
    val businessTypes  : List<BusinessType> = emptyList(),
    val clientGroups   : List<ClientGroup>  = emptyList(),
    val departments    : List<Department> = emptyList(),
    val provinces      : List<Province>   = emptyList(),
    val districts      : List<District>   = emptyList(),
    val error          : String?          = null
)

class ClientViewModel(
    private val getClientsUseCase         : GetClientsUseCase,
    private val getClientByIdUseCase      : GetClientByIdUseCase,
    private val createClientUseCase       : CreateClientUseCase,
    private val updateClientUseCase       : UpdateClientUseCase,
    private val deleteClientUseCase       : DeleteClientUseCase,
    private val activateClientUseCase     : ActivateClientUseCase,
    private val getClientFormMasterData   : GetClientFormMasterDataUseCase,
    private val getNextClientCodeUseCase: GetNextClientCodeUseCase,
    private val getDepartmentsUseCase     : GetDepartmentsUseCase,
    private val getProvincesUseCase       : GetProvincesUseCase,
    private val getDistrictsUseCase       : GetDistrictsUseCase,
    private val getActiveUsersUseCase     : GetActiveUsersUseCase,
    private val getFullUserUseCase        : GetFullUserUseCase
) : ViewModel() {

    private val _listState = MutableStateFlow(ClientListUiState())
    val listState: StateFlow<ClientListUiState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(ClientFormUiState())
    val formState: StateFlow<ClientFormUiState> = _formState.asStateFlow()

    // ── Lista ─────────────────────────────────────────────────────────
    
    fun checkUserRoleAndLoadData() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true) }
            when (val userResult = getFullUserUseCase()) {
                is ApiResult.Success -> {
                    val user = userResult.data
                    val isAdmin = user.roles.any { it.role.uppercase() == "ADMIN" || it.role.uppercase() == "ADMINISTRADOR" }
                    _listState.update { it.copy(isAdmin = isAdmin) }
                    if (isAdmin) {
                        loadEmployees()
                    } else {
                        loadClients()
                    }
                }
                is ApiResult.Error -> {
                    // Fallback a flujo normal si falla la verificación de rol
                    loadClients()
                }
            }
        }
    }

    fun loadEmployees() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoadingEmployees = true, isLoading = false) }
            when (val result = getActiveUsersUseCase()) {
                is ApiResult.Success -> {
                    _listState.update {
                        it.copy(
                            isLoadingEmployees = false,
                            employees = result.data.filter { emp -> emp.active }
                        )
                    }
                }
                is ApiResult.Error -> {
                    GlobalErrorHandler.emit(result.exception)
                    _listState.update { it.copy(isLoadingEmployees = false) }
                }
            }
        }
    }

    fun selectEmployee(employee: User?) {
        _listState.update { it.copy(selectedEmployee = employee) }
        if (employee != null) {
            loadClients(employee.id)
        } else {
            _listState.update { it.copy(clients = emptyList()) }
        }
    }

    fun loadClients(userId: Int? = null) {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true) }
            val targetUserId = userId ?: _listState.value.selectedEmployee?.id
            when (val result = getClientsUseCase(targetUserId)) {
                is ApiResult.Success -> _listState.update {
                    it.copy(isLoading = false, clients = result.data)
                }
                is ApiResult.Error   ->  {
                    GlobalErrorHandler.emit(result.exception)
                    _listState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    // ── Formulario ────────────────────────────────────────────────────

    fun loadFormData(clientId: Int? = null) {
        viewModelScope.launch {
            _formState.update { ClientFormUiState(isLoading = true) }

            println("[ClientVM] loadFormData clientId=$clientId")

            val masterDataResult  = getClientFormMasterData()
            val codeResult        = if (clientId == null) getNextClientCodeUseCase() else null
            val clientResult      = clientId?.let { getClientByIdUseCase(it) }
            val departmentsResult = getDepartmentsUseCase()

            if (masterDataResult is ApiResult.Error) {
                GlobalErrorHandler.emit(masterDataResult.exception)
                _formState.update { it.copy(isLoading = false) }
                return@launch
            }
            if (codeResult is ApiResult.Error) {
                GlobalErrorHandler.emit(codeResult.exception)
                _formState.update { it.copy(isLoading = false) }
                return@launch
            }
            if (departmentsResult is ApiResult.Error) {
                GlobalErrorHandler.emit(departmentsResult.exception)
                _formState.update { it.copy(isLoading = false) }
                return@launch
            }

            val masterData = (masterDataResult as ApiResult.Success).data
            val nextCode   = (codeResult as? ApiResult.Success)?.data
            val rawClient  = (clientResult as? ApiResult.Success)?.data
            val depts      = (departmentsResult as ApiResult.Success).data

            println("[ClientVM] rawClient district=${rawClient?.district?.id} province=${rawClient?.province?.id} department=${rawClient?.department?.id}")

            val resolvedClient = rawClient?.copy(
                documentType = masterData.documentTypes
                    .firstOrNull { it.id == rawClient.documentType?.id },
                businessType = masterData.businessTypes
                    .firstOrNull { it.id == rawClient.businessType?.id },
                clientGroup  = masterData.clientGroups
                    .firstOrNull { it.id == rawClient.clientGroup?.id }
            )

            // ── Carga cascada ubigeo si el cliente tiene distrito ─────────
            var provinces  = emptyList<Province>()
            var districts  = emptyList<District>()

            val clientDistrict   = resolvedClient?.district
            val clientProvince   = resolvedClient?.province
            val clientDepartment = resolvedClient?.department

            if (clientDistrict != null && clientProvince != null) {
                // Carga provincias del departamento
                if (clientDepartment != null) {
                    when (val provResult = getProvincesUseCase(clientDepartment.id)) {
                        is ApiResult.Success -> {
                            provinces = provResult.data
                            println("[ClientVM] Provincias cargadas: ${provinces.size}")
                        }
                        is ApiResult.Error -> println("[ClientVM] Error cargando provincias: ${provResult.exception.message}")
                    }
                }

                // Carga distritos de la provincia
                when (val distResult = getDistrictsUseCase(clientProvince.id)) {
                    is ApiResult.Success -> {
                        districts = distResult.data
                        println("[ClientVM] Distritos cargados: ${districts.size}")
                    }
                    is ApiResult.Error -> println("[ClientVM] Error cargando distritos: ${distResult.exception.message}")
                }
            }

            _formState.update {
                it.copy(
                    isLoading     = false,
                    client        = resolvedClient,
                    clientCode    = resolvedClient?.code?.let { c -> NextCode(c) } ?: nextCode,
                    documentTypes = masterData.documentTypes,
                    businessTypes = masterData.businessTypes,
                    clientGroups  = masterData.clientGroups,
                    departments   = depts,
                    provinces     = provinces,
                    districts     = districts
                )
            }

            println("[ClientVM] formState actualizado: provinces=${provinces.size} districts=${districts.size}")
        }
    }

    fun loadProvinces(departmentId: Int) {
        viewModelScope.launch {
            _formState.update { it.copy(provinces = emptyList(), districts = emptyList()) }
            when (val result = getProvincesUseCase(departmentId)) {
                is ApiResult.Success -> _formState.update { it.copy(provinces = result.data) }
                is ApiResult.Error -> GlobalErrorHandler.emit(result.exception)
            }
        }
    }

    fun loadDistricts(provinceId: Int) {
        viewModelScope.launch {
            _formState.update { it.copy(districts = emptyList()) }
            when (val result = getDistrictsUseCase(provinceId)) {
                is ApiResult.Success -> _formState.update { it.copy(districts = result.data) }
                is ApiResult.Error -> GlobalErrorHandler.emit(result.exception)
            }
        }
    }

    fun resetForm() {
        _formState.update { ClientFormUiState() }  // resetea a estado vacío
    }

    fun resetList() {
        _listState.update { ClientListUiState() }  // isLoading = true por defecto
    }

    fun saveClient(client: Client) {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, error = null) }
            val result = if (client.id == 0) {
                val clientWithUser = if (_listState.value.isAdmin && _listState.value.selectedEmployee != null) {
                    client.copy(userId = _listState.value.selectedEmployee?.id)
                } else {
                    client
                }
                println("[ClientVM] Creando nuevo cliente con user_id=${clientWithUser.userId}")
                createClientUseCase(clientWithUser)
            } else {
                println("[ClientVM] Actualizando cliente id=${client.id}")
                updateClientUseCase(client.id, client)
            }
            when (result) {
                is ApiResult.Success -> {
                    println("[ClientVM] Cliente guardado id=${result.data.id}")
                    _formState.update { it.copy(isLoading = false, isSaved = true, client = result.data) }
                }
                is ApiResult.Error -> {
                    println("[ClientVM] Error al guardar: ${result.exception.message}")
                    when (val appError = result.exception.toAppError()) {
                        is AppError.ValidationError -> _formState.update {
                            it.copy(isLoading = false, error = appError.toUserMessage())
                        }
                        else -> {
                            GlobalErrorHandler.emit(appError)
                            _formState.update { it.copy(isLoading = false) }
                        }
                    }
                }
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _formState.update { state ->
            val currentClient = state.client ?: Client()
            state.copy(
                isLocating = false,
                client = currentClient.copy(
                    latitude  = latitude,
                    longitude = longitude
                )
            )
        }
    }

    fun toggleClientActive(client: Client) {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true) }

            // Evaluamos si el cliente está activo para decidir el endpoint
            val result = if (client.active) {
                deleteClientUseCase(client.id)
            } else {
                activateClientUseCase(client.id)
            }
            when (result) {
                is ApiResult.Success -> {
                    loadClients()
                }
                is ApiResult.Error -> {
                    _listState.update { it.copy(isLoading = false) }
                    GlobalErrorHandler.emit(result.exception)
                }

            }
        }
    }

    fun startLocating() {
        _formState.update { it.copy(isLocating = true) }
    }

    fun stopLocating() {
        _formState.update { it.copy(isLocating = false) }
    }

    fun clearError()  = _formState.update { it.copy(error = null) }
    fun clearSaved()  = _formState.update { it.copy(isSaved = false) }
}