package org.smlpartners.smlgo.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.domain.model.ClientSchedule
import org.smlpartners.smlgo.domain.usecase.client.GetClientsUseCase
import org.smlpartners.smlgo.domain.usecase.schedule.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.smlpartners.smlgo.core.error.GlobalErrorHandler
import org.smlpartners.smlgo.core.network.ApiError

data class ScheduleUiState(
    val isLoading         : Boolean                          = true,
    val schedulesByDay    : Map<LocalDate, List<ClientSchedule>> = emptyMap(),
    val currentWeekStart  : LocalDate?                       = null,
    val availableClients  : List<Client>                     = emptyList(),
    val isSaved           : Boolean                          = false,
)

class ScheduleViewModel(
    private val getWeeklySchedulesUseCase : GetWeeklySchedulesUseCase,
    private val createScheduleUseCase     : CreateScheduleUseCase,
    private val deleteScheduleUseCase     : DeleteScheduleUseCase,
    private val getClientsUseCase         : GetClientsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    fun loadWeek(referenceDate: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentWeekStart = referenceDate) }
            when (val result = getWeeklySchedulesUseCase.groupedByDay(referenceDate)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, schedulesByDay = result.data)
                }
                is ApiResult.Error -> {
                    GlobalErrorHandler.emit(result.exception)
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun loadClients() {
        viewModelScope.launch {
            when (val result = getClientsUseCase()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(availableClients = result.data)
                }
                is ApiResult.Error   ->  {
                    GlobalErrorHandler.emit(result.exception)
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun createSchedule(
        clientId    : Int,
        day         : LocalDate,
        startTime   : String,
        observation : String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = createScheduleUseCase(clientId, day, startTime, observation)) {
                is ApiResult.Success -> {
                    // Refresca la semana actual
                    _uiState.value.currentWeekStart?.let { loadWeek(it) }
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                is ApiResult.Error -> {
                    GlobalErrorHandler.emit(result.exception)
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun deleteSchedule(id: Int) {
        viewModelScope.launch {
            when (deleteScheduleUseCase(id)) {
                is ApiResult.Success -> {
                    // Elimina localmente sin recargar
                    _uiState.update { state ->
                        state.copy(
                            schedulesByDay = state.schedulesByDay.mapValues { (_, schedules) ->
                                schedules.filter { it.id != id }
                            }.filter { it.value.isNotEmpty() }
                        )
                    }
                }
                is ApiResult.Error   -> {
                    GlobalErrorHandler.emit(
                        ApiError.UnknownError(
                            "No se pudo eliminar el horario"
                        )
                    )
                }
            }
        }
    }

    fun clearSaved()  = _uiState.update { it.copy(isSaved = false) }
    fun resetSchedules() {
        _uiState.update { ScheduleUiState() }  // isLoading = true por defecto
    }
}