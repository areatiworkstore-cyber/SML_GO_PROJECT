package org.smlpartners.smlgo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.domain.model.MapMarker
import org.smlpartners.smlgo.domain.model.toMapMarker
import org.smlpartners.smlgo.domain.usecase.client.GetClientsWithLocationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.smlpartners.smlgo.core.error.GlobalErrorHandler

data class MapUiState(
    val isLoading       : Boolean         = false,
    val markers         : List<MapMarker> = emptyList(),
    val selectedMarker  : MapMarker?      = null,
    val selectedClient  : Client?         = null,
    val error           : String?         = null
)

class MapViewModel(
    private val getClientsWithLocationUseCase: GetClientsWithLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // Cache de clientes para mostrar el detalle al seleccionar marker
    private var clientsCache: List<Client> = emptyList()

    fun loadMarkers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getClientsWithLocationUseCase()) {
                is ApiResult.Success -> {
                    clientsCache = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            markers   = result.data.map { c -> c.toMapMarker() }
                        )
                    }
                }
                is ApiResult.Error -> {
                    GlobalErrorHandler.emit(result.exception)
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onMarkerSelected(marker: MapMarker) {
        val client = clientsCache.firstOrNull { it.id == marker.id }
        _uiState.update {
            it.copy(
                selectedMarker = marker,
                selectedClient = client,
                markers        = it.markers.map { m ->
                    m.copy(isSelected = m.id == marker.id)
                }
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedMarker = null,
                selectedClient = null,
                markers        = it.markers.map { m -> m.copy(isSelected = false) }
            )
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}