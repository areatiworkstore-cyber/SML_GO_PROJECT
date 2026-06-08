package org.smlpartners.smlgo.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.domain.model.BusinessType
import org.smlpartners.smlgo.domain.model.ClientGroup
import org.smlpartners.smlgo.domain.model.DocumentType
import org.smlpartners.smlgo.domain.model.Supplier
import org.smlpartners.smlgo.domain.usecase.client.*
import org.smlpartners.smlgo.domain.usecase.masterdata.GetClientFormMasterDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.smlpartners.smlgo.core.error.AppError
import org.smlpartners.smlgo.core.error.GlobalErrorHandler
import org.smlpartners.smlgo.core.error.toAppError
import org.smlpartners.smlgo.domain.model.NextCode

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
    val suppliers      : List<Supplier>     = emptyList(),
    val error          : String?          = null
)

class ClientViewModel(
    private val getClientsUseCase         : GetClientsUseCase,
    private val getClientByIdUseCase      : GetClientByIdUseCase,
    private val createClientUseCase       : CreateClientUseCase,
    private val updateClientUseCase       : UpdateClientUseCase,
    private val getClientFormMasterData   : GetClientFormMasterDataUseCase,
    private val getNextClientCodeUseCase: GetNextClientCodeUseCase
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

            val masterData = (masterDataResult as ApiResult.Success).data
            val nextCode   = (codeResult as? ApiResult.Success)?.data
            val rawClient  = (clientResult as? ApiResult.Success)?.data

            // Resuelve los objetos completos cruzando IDs con catálogos
            val resolvedClient = rawClient?.copy(
                documentType = masterData.documentTypes
                    .firstOrNull { it.id == rawClient.documentType?.id },
                businessType = masterData.businessTypes
                    .firstOrNull { it.id == rawClient.businessType?.id },
                clientGroup  = masterData.clientGroups
                    .firstOrNull { it.id == rawClient.clientGroup?.id },
                supplier     = masterData.suppliers
                    .firstOrNull { it.id == rawClient.supplier?.id }
            )

            _formState.update {
                it.copy(
                    isLoading     = false,
                    client        = resolvedClient,
                    clientCode    = resolvedClient?.code?.let { NextCode(it) } ?: nextCode,
                    documentTypes = masterData.documentTypes,
                    businessTypes = masterData.businessTypes,
                    clientGroups  = masterData.clientGroups,
                    suppliers     = masterData.suppliers
                )
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