package org.smlpartners.smlgo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.domain.model.Route
import org.smlpartners.smlgo.domain.usecase.client.GetClientsWithLocationUseCase
import org.smlpartners.smlgo.domain.usecase.route.GetRoutesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.smlpartners.smlgo.core.error.GlobalErrorHandler

data class DashboardUiState(
    val isLoading         : Boolean       = false,
    val clientsOnMap      : List<Client>  = emptyList(),
    val todayRoutes       : List<Route>   = emptyList(),
)

class DashboardViewModel(
    private val getClientsWithLocationUseCase : GetClientsWithLocationUseCase,
    private val getRoutesUseCase              : GetRoutesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val clientsResult = getClientsWithLocationUseCase()
            val routesResult  = getRoutesUseCase()

            val clients = (clientsResult as? ApiResult.Success)?.data ?: emptyList()
            val routes  = (routesResult  as? ApiResult.Success)?.data ?: emptyList()

            // Emite errores técnicos al global
            if (clientsResult is ApiResult.Error)
                GlobalErrorHandler.emit(clientsResult.exception)
            if (routesResult is ApiResult.Error)
                GlobalErrorHandler.emit(routesResult.exception)

            _uiState.update {
                it.copy(
                    isLoading    = false,
                    clientsOnMap = clients,
                    todayRoutes  = routes,
                )
            }
        }
    }
}