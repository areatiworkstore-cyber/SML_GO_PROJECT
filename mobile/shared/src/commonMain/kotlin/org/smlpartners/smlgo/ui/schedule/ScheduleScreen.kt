package org.smlpartners.smlgo.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.domain.model.ClientSchedule
import org.smlpartners.smlgo.ui.shared.components.*
import kotlinx.datetime.Clock

@Composable
fun ScheduleScreen(onBack: () -> Unit) {
    val viewModel: ScheduleViewModel = koinViewModel()
    val uiState  by viewModel.uiState.collectAsState()

    val today        = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var selectedDate by remember { mutableStateOf(today) }

    LaunchedEffect(Unit) {
        viewModel.loadWeek(today)
        viewModel.loadClients()
    }

    Scaffold(
        topBar = { SMLGoTopBar(title = "Agenda", onBack = onBack) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // ── Navegación semanal ────────────────────────────────────
            WeekNavigator(
                currentDate  = selectedDate,
                onPrevWeek   = {
                    selectedDate = selectedDate.minus(DatePeriod(days = 7))
                    viewModel.loadWeek(selectedDate)
                },
                onNextWeek   = {
                    selectedDate = selectedDate.plus(DatePeriod(days = 7))
                    viewModel.loadWeek(selectedDate)
                }
            )

            // ── Días de la semana ─────────────────────────────────────
            WeekDaySelector(
                currentDate     = selectedDate,
                schedulesByDay  = uiState.schedulesByDay,
                onSelectDate    = { selectedDate = it }
            )

            HorizontalDivider()

            // ── Lista de schedules del día seleccionado ───────────────
            val daySchedules = uiState.schedulesByDay[selectedDate] ?: emptyList()
            if (daySchedules.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "Sin visitas programadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daySchedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onDelete = { viewModel.deleteSchedule(schedule.id) }
                        )
                    }
                }
            }
        }

        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            ErrorSnackbar(
                message   = uiState.error,
                onDismiss = viewModel::clearError
            )
        }
    }
}

@Composable
private fun WeekNavigator(
    currentDate : LocalDate,
    onPrevWeek  : () -> Unit,
    onNextWeek  : () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevWeek) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Semana anterior")
        }
        Text(
            text  = "Semana del ${currentDate.minus(DatePeriod(days = currentDate.dayOfWeek.ordinal))}",
            style = MaterialTheme.typography.titleSmall
        )
        IconButton(onClick = onNextWeek) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Semana siguiente")
        }
    }
}

@Composable
private fun WeekDaySelector(
    currentDate    : LocalDate,
    schedulesByDay : Map<LocalDate, List<ClientSchedule>>,
    onSelectDate   : (LocalDate) -> Unit
) {
    val monday      = currentDate.minus(DatePeriod(days = currentDate.dayOfWeek.ordinal))
    val dayNames    = listOf("L", "M", "M", "J", "V", "S", "D")

    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (0..6).forEach { offset ->
            val date       = monday.plus(DatePeriod(days = offset))
            val isSelected = date == currentDate
            val hasItems   = schedulesByDay[date]?.isNotEmpty() == true

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectDate(date) }
                    .padding(8.dp)
            ) {
                Text(
                    text  = dayNames[offset],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else            MaterialTheme.colorScheme.surface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text      = date.dayOfMonth.toString(),
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else            MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                if (hasItems) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule : ClientSchedule,
    onDelete : () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = schedule.client.name ?: "Cliente ${schedule.client.id}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text  = schedule.startTime.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!schedule.observation.isNullOrBlank()) {
                    Text(
                        text  = schedule.observation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector        = Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint               = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}