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

data class ScheduleUiState(
    val isLoading         : Boolean                          = false,
    val schedulesByDay    : Map<LocalDate, List<ClientSchedule>> = emptyMap(),
    val currentWeekStart  : LocalDate?                       = null,
    val availableClients  : List<Client>                     = emptyList(),
    val isSaved           : Boolean                          = false,
    val error             : String?                          = null
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
            _uiState.update { it.copy(isLoading = true, error = null, currentWeekStart = referenceDate) }
            when (val result = getWeeklySchedulesUseCase.groupedByDay(referenceDate)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, schedulesByDay = result.data)
                }
                is ApiResult.Error   -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
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
                is ApiResult.Error   -> _uiState.update {
                    it.copy(error = result.exception.message)
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = createScheduleUseCase(clientId, day, startTime, observation)) {
                is ApiResult.Success -> {
                    // Refresca la semana actual
                    _uiState.value.currentWeekStart?.let { loadWeek(it) }
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                is ApiResult.Error   -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
            }
        }
    }

    fun deleteSchedule(id: Int) {
        viewModelScope.launch {
            when (val result = deleteScheduleUseCase(id)) {
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
                is ApiResult.Error   -> _uiState.update {
                    it.copy(error = result.exception.message)
                }
            }
        }
    }

    fun clearError()  = _uiState.update { it.copy(error = null) }
    fun clearSaved()  = _uiState.update { it.copy(isSaved = false) }
}