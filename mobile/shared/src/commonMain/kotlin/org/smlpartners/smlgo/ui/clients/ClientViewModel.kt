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

data class ClientListUiState(
    val isLoading : Boolean       = false,
    val clients   : List<Client>  = emptyList(),
    val error     : String?       = null
)

data class ClientFormUiState(
    val isLoading      : Boolean          = false,
    val isSaved        : Boolean          = false,
    val client         : Client?          = null,
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
    private val getClientFormMasterData   : GetClientFormMasterDataUseCase
) : ViewModel() {

    private val _listState = MutableStateFlow(ClientListUiState())
    val listState: StateFlow<ClientListUiState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(ClientFormUiState())
    val formState: StateFlow<ClientFormUiState> = _formState.asStateFlow()

    // ── Lista ─────────────────────────────────────────────────────────

    fun loadClients() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true, error = null) }
            when (val result = getClientsUseCase()) {
                is ApiResult.Success -> _listState.update {
                    it.copy(isLoading = false, clients = result.data)
                }
                is ApiResult.Error   -> _listState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }

    // ── Formulario ────────────────────────────────────────────────────

    fun loadFormData(clientId: Int? = null) {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, error = null) }

            // Carga catálogos y cliente en paralelo
            val masterDataResult = getClientFormMasterData()
            val clientResult = clientId?.let { getClientByIdUseCase(it) }

            if (masterDataResult is ApiResult.Error) {
                _formState.update {
                    it.copy(isLoading = false, error = masterDataResult.exception.message)
                }
                return@launch
            }

            val masterData = (masterDataResult as ApiResult.Success).data
            val client     = (clientResult as? ApiResult.Success)?.data

            _formState.update {
                it.copy(
                    isLoading     = false,
                    client        = client,
                    documentTypes = masterData.documentTypes,
                    businessTypes = masterData.businessTypes,
                    clientGroups  = masterData.clientGroups,
                    suppliers     = masterData.suppliers
                )
            }
        }
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
                is ApiResult.Error   -> _formState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _formState.update { state ->
            state.copy(
                client = state.client?.copy(
                    latitude  = latitude,
                    longitude = longitude
                )
            )
        }
    }

    fun clearError()  = _formState.update { it.copy(error = null) }
    fun clearSaved()  = _formState.update { it.copy(isSaved = false) }
}