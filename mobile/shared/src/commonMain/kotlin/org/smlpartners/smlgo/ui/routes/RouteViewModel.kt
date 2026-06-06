package org.smlpartners.smlgo.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.domain.model.Route
import org.smlpartners.smlgo.domain.model.Waypoint
import org.smlpartners.smlgo.domain.model.WaypointStatus
import org.smlpartners.smlgo.domain.usecase.client.GetClientsUseCase
import org.smlpartners.smlgo.domain.usecase.route.*
import org.smlpartners.smlgo.domain.usecase.waypoint.CreateWaypointUseCase
import org.smlpartners.smlgo.domain.usecase.waypoint.UpdateWaypointStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class RouteListUiState(
    val isLoading : Boolean      = false,
    val routes    : List<Route>  = emptyList(),
    val error     : String?      = null
)

data class RouteFormUiState(
    val isLoading         : Boolean        = false,
    val isSaved           : Boolean        = false,
    val route             : Route?         = null,
    val availableClients  : List<Client>   = emptyList(),
    val selectedClients   : List<Client>   = emptyList(),
    val error             : String?        = null
)

data class RouteDetailUiState(
    val isLoading : Boolean   = false,
    val route     : Route?    = null,
    val error     : String?   = null
)

class RouteViewModel(
    private val getRoutesUseCase                  : GetRoutesUseCase,
    private val getRouteByIdUseCase               : GetRouteByIdUseCase,
    private val createRouteWithWaypointsUseCase   : CreateRouteWithWaypointsUseCase,
    private val deleteRouteUseCase                : DeleteRouteUseCase,
    private val getClientsUseCase                 : GetClientsUseCase,
    private val createWaypointUseCase             : CreateWaypointUseCase,
    private val updateWaypointStatusUseCase       : UpdateWaypointStatusUseCase
) : ViewModel() {

    private val _listState   = MutableStateFlow(RouteListUiState())
    val listState: StateFlow<RouteListUiState> = _listState.asStateFlow()

    private val _formState   = MutableStateFlow(RouteFormUiState())
    val formState: StateFlow<RouteFormUiState> = _formState.asStateFlow()

    private val _detailState = MutableStateFlow(RouteDetailUiState())
    val detailState: StateFlow<RouteDetailUiState> = _detailState.asStateFlow()

    // ── Lista ─────────────────────────────────────────────────────────

    fun loadRoutes() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true, error = null) }
            when (val result = getRoutesUseCase()) {
                is ApiResult.Success -> _listState.update {
                    it.copy(isLoading = false, routes = result.data)
                }
                is ApiResult.Error   -> _listState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }

    // ── Detalle ───────────────────────────────────────────────────────

    fun loadRouteDetail(routeId: Int) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null) }
            when (val result = getRouteByIdUseCase(routeId)) {
                is ApiResult.Success -> _detailState.update {
                    it.copy(isLoading = false, route = result.data)
                }
                is ApiResult.Error   -> _detailState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }

    // ── Formulario crear ruta ─────────────────────────────────────────

    fun loadAvailableClients() {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, error = null) }
            when (val result = getClientsUseCase()) {
                is ApiResult.Success -> _formState.update {
                    // Solo clientes con ubicación pueden ser waypoints
                    it.copy(
                        isLoading        = false,
                        availableClients = result.data.filter { c -> c.hasLocation }
                    )
                }
                is ApiResult.Error   -> _formState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }

    fun toggleClientSelection(client: Client) {
        _formState.update { state ->
            val selected = state.selectedClients.toMutableList()
            if (selected.any { it.id == client.id }) {
                selected.removeAll { it.id == client.id }
            } else {
                selected.add(client)
            }
            state.copy(selectedClients = selected)
        }
    }

    fun createRoute(name: String, scheduledDate: LocalDate) {
        val selected = _formState.value.selectedClients
        if (selected.isEmpty()) {
            _formState.update { it.copy(error = "Selecciona al menos un cliente") }
            return
        }
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, error = null) }
            val waypoints = selected.mapIndexed { index, client ->
                WaypointInput(client = client, orderSequence = index + 1)
            }
            when (val result = createRouteWithWaypointsUseCase(name, scheduledDate, waypoints)) {
                is ApiResult.Success -> _formState.update {
                    it.copy(isLoading = false, isSaved = true, route = result.data)
                }
                is ApiResult.Error   -> _formState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }

    // ── Waypoints en campo ────────────────────────────────────────────

    fun markWaypointAsVisited(routeId: Int, waypointId: Int, comment: String? = null) {
        updateWaypointStatus(routeId, waypointId, WaypointStatus.VISITA, comment)
    }

    fun cancelWaypoint(routeId: Int, waypointId: Int, comment: String? = null) {
        updateWaypointStatus(routeId, waypointId, WaypointStatus.CANCELADA, comment)
    }

    private fun updateWaypointStatus(
        routeId    : Int,
        waypointId : Int,
        status     : WaypointStatus,
        comment    : String?
    ) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            when (val result = updateWaypointStatusUseCase(routeId, waypointId, status, comment)) {
                is ApiResult.Success -> {
                    // Actualiza el waypoint en el estado local sin recargar toda la ruta
                    _detailState.update { state ->
                        state.copy(
                            isLoading = false,
                            route     = state.route?.copy(
                                waypoints = state.route.waypoints.map { w ->
                                    if (w.id == waypointId) result.data else w
                                }
                            )
                        )
                    }
                }
                is ApiResult.Error   -> _detailState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }

    fun deleteRoute(routeId: Int, onDeleted: () -> Unit) {
        viewModelScope.launch {
            when (deleteRouteUseCase(routeId)) {
                is ApiResult.Success -> {
                    _listState.update { state ->
                        state.copy(routes = state.routes.filter { it.id != routeId })
                    }
                    onDeleted()
                }
                is ApiResult.Error   -> _listState.update {
                    it.copy(error = "No se pudo eliminar la ruta")
                }
            }
        }
    }

    fun clearError() {
        _listState.update   { it.copy(error = null) }
        _formState.update   { it.copy(error = null) }
        _detailState.update { it.copy(error = null) }
    }

    fun clearSaved() = _formState.update { it.copy(isSaved = false) }
}