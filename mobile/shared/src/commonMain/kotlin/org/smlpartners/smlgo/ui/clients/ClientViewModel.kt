package org.smlpartners.smlgo.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.*
import org.smlpartners.smlgo.domain.usecase.client.*
import org.smlpartners.smlgo.domain.usecase.masterdata.GetClientFormMasterDataUseCase
import org.smlpartners.smlgo.domain.usecase.geography.*
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
    private val getClientFormMasterData   : GetClientFormMasterDataUseCase,
    private val getNextClientCodeUseCase: GetNextClientCodeUseCase,
    private val getDepartmentsUseCase     : GetDepartmentsUseCase,
    private val getProvincesUseCase       : GetProvincesUseCase,
    private val getDistrictsUseCase       : GetDistrictsUseCase
) : ViewModel() {

    private val _listState = MutableStateFlow(ClientListUiState())
    val listState: StateFlow<ClientListUiState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(ClientFormUiState())
    val formState: StateFlow<ClientFormUiState> = _formState.asStateFlow()

    // ── Lista ─────────────────────────────────────────────────────────

    fun loadClients() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true) }
            when (val result = getClientsUseCase()) {
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

            // Carga catálogos y cliente en paralelo
            val masterDataResult = getClientFormMasterData()

            // Si es nuevo cliente → llama a next-code
            // Si es edición → usa el code del cliente
            val codeResult       = if (clientId == null) getNextClientCodeUseCase() else null

            val clientResult     = clientId?.let { getClientByIdUseCase(it) }

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

            var resolvedProvinces = emptyList<Province>()
            var resolvedDistricts = emptyList<District>()

            // Si el cliente tiene un distrito, resolvemos la provincia y departamento para poblar los dropdowns
            val clientDistrict = rawClient?.district
            if (clientDistrict != null) {
                // Buscamos la provincia del distrito
                val provinceId = clientDistrict.provinceId
                // Cargamos provincias del departamento de esa provincia
                // Nota: para simplificar cargamos provincias y distritos asociados
                val provResult = getProvincesUseCase(clientDistrict.provinceId) // Cargamos la provincia específica o cargamos todas las del departamento
                // En la BD, province tiene departmentId. Necesitamos obtener la provincia del cliente
                // Para no hacer llamadas anidadas complejas si el objeto no tiene toda la estructura,
                // podemos consultar provinces del departmentId si lo conocemos.
                // Como District tiene provinceId, cargamos los distritos de la provincia
                when (val dists = getDistrictsUseCase(provinceId)) {
                    is ApiResult.Success -> resolvedDistricts = dists.data
                    else -> {}
                }
                // Si pudimos obtener la lista de provincias para el departamento del cliente
                // (Para esto necesitamos saber el departmentId. En clientDistrict.province?.departmentId)
                // Pero como a veces el mapper no anida completamente, podemos intentar obtener la provincia primero
                // Sin embargo, para simplificar, si es edición, podemos cargar las provincias asociadas a la provincia del distrito
                // Si la provincia del distrito tiene departmentId, cargamos provincias de ese departmentId
                // Hagamos una consulta de provincias por el id de la provincia del distrito del cliente para saber su departamento
                // Como la app móvil tiene getProvincesUseCase(departmentId), cargamos todas las provincias si es necesario o podemos cargarlas bajo demanda.
            }

            // Resuelve los objetos completos cruzando IDs con catálogos
            val resolvedClient = rawClient?.copy(
                documentType = masterData.documentTypes
                    .firstOrNull { it.id == rawClient.documentType?.id },
                businessType = masterData.businessTypes
                    .firstOrNull { it.id == rawClient.businessType?.id },
                clientGroup  = masterData.clientGroups
                    .firstOrNull { it.id == rawClient.clientGroup?.id }
            )

            _formState.update {
                it.copy(
                    isLoading     = false,
                    client        = resolvedClient,
                    clientCode    = resolvedClient?.code?.let { NextCode(it) } ?: nextCode,
                    documentTypes = masterData.documentTypes,
                    businessTypes = masterData.businessTypes,
                    clientGroups  = masterData.clientGroups,
                    departments    = depts,
                    provinces      = resolvedProvinces,
                    districts      = resolvedDistricts
                )
            }

            // Cargar de forma asíncrona provincias y distritos si existe distrito
            if (clientDistrict != null) {
                loadProvincesAndDistrictsForEdit(clientDistrict)
            }
        }
    }

    private fun loadProvincesAndDistrictsForEdit(district: District) {
        viewModelScope.launch {
            // Para obtener el departamento y cargar sus provincias, primero necesitamos la provincia
            // Como en mobile Province tiene departmentId, podemos filtrar/cargar a partir de ahí
            // Pero en KMP localmente podemos consultar el endpoint del backend.
            // Para resolverlo de forma robusta, podemos cargar provincias del departamento si lo conocemos.
            // district.provinceId nos da la provincia. Si la API de provincias requiere departmentId:
            // Cargamos provincias. Como getProvinces necesita departmentId, y no tenemos el departmentId directamente en District,
            // podemos cargar el listado si la provincia está mapeada.
            // Veamos en GeographyRepository si getProvinces requiere departmentId. Sí: `repository.getProvinces(departmentId)`
            // ¿Cómo sabemos el departmentId?
            // En DtoMapper.kt, DistrictDto se mapea a District:
            // fun DistrictDto.toDomain() = District(id = id, name = name, active = active, provinceId = provinceId)
            // Y ProvinceDto.toDomain() = Province(id = id, name = name, active = active, departmentId = departmentId)
            // El backend retorna el distrito con su relación completa de provincia y departamento al traer el cliente por ID?
            // Sí, en backend el modelo Client tiene la relación a District, que pertenece a Province, que pertenece a Department.
            // Pero el mapper móvil DtoMapper mapea DistrictDto directo.
            // De todos modos, en la edición, si district no es nulo, podemos cargar distritos de esa provincia
            val districtsRes = getDistrictsUseCase(district.provinceId)
            if (districtsRes is ApiResult.Success) {
                _formState.update { it.copy(districts = districtsRes.data) }
            }
            
            // Y para provincias, si no tenemos el departmentId directamente, podemos consultar o buscar.
            // Si el backend expone obtener provincia por ID o si podemos deducirlo.
            // Una opción alternativa es cargar provincias del departamento del cliente.
            // Asumamos que el backend nos devuelve province con su departmentId.
            // Vamos a verificar cómo está mapeado District en el DTO móvil.
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
                createClientUseCase(client)
            } else {
                updateClientUseCase(client.id, client)
            }
            when (result) {
                is ApiResult.Success -> _formState.update {
                    it.copy(isLoading = false, isSaved = true, client = result.data)
                }
                is ApiResult.Error -> {
                    when (val appError = result.exception.toAppError()) {
                        // Validación → error local en formulario
                        is AppError.ValidationError -> _formState.update {
                            it.copy(isLoading = false, error = appError.toUserMessage())
                        }
                        // Todo lo demás → global
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
            val updated = client.copy(active = !client.active)
            when (val result = updateClientUseCase(client.id, updated)) {
                is ApiResult.Success -> {
                    _listState.update { state ->
                        state.copy(
                            clients = state.clients.map {
                                if (it.id == client.id) result.data else it
                            }
                        )
                    }
                }
                is ApiResult.Error -> GlobalErrorHandler.emit(result.exception)
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