package org.smlpartners.smlgo.ui.routes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.domain.model.Waypoint
import org.smlpartners.smlgo.domain.model.WaypointStatus
import org.smlpartners.smlgo.ui.shared.components.*
import org.smlpartners.smlgo.ui.shared.theme.Spacing
import org.smlpartners.smlgo.ui.shared.theme.Radius
import org.smlpartners.smlgo.ui.shared.theme.Success

@Composable
fun RouteDetailScreen(
    routeId : Int,
    onBack  : () -> Unit
) {
    val viewModel  : RouteViewModel = koinViewModel()
    val detailState by viewModel.detailState.collectAsState()

    LaunchedEffect(routeId) { viewModel.loadRouteDetail(routeId) }

    Scaffold(
        topBar = {
            SMLGoTopBar(
                title  = detailState.route?.name ?: "Detalle de ruta",
                onBack = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                detailState.isLoading        -> LoadingOverlay()
                detailState.route == null    -> EmptyRouteDetail()
                else -> {
                    val route = detailState.route!!
                    LazyColumn(
                        contentPadding      = PaddingValues(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        // ── Encabezado ────────────────────────────────
                        item {
                            RouteHeaderCard(route = route)
                        }

                        // ── Progreso ──────────────────────────────────
                        item {
                            RouteProgressCard(route = route)
                        }

                        // ── Título waypoints ──────────────────────────
                        item {
                            Text(
                                text     = "Paradas (${route.waypoints.size})",
                                style    = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = Spacing.xs)
                            )
                        }

                        // ── Lista de waypoints ────────────────────────
                        itemsIndexed(
                            items = route.waypoints.sortedBy { it.orderSequence },
                            key   = { _, w -> w.id }
                        ) { index, waypoint ->
                            WaypointCard(
                                waypoint  = waypoint,
                                index     = index,
                                isLast    = index == route.waypoints.size - 1,
                                onVisit   = { comment ->
                                    viewModel.markWaypointAsVisited(
                                        routeId    = routeId,
                                        waypointId = waypoint.id,
                                        comment    = comment
                                    )
                                },
                                onCancel  = { comment ->
                                    viewModel.cancelWaypoint(
                                        routeId    = routeId,
                                        waypointId = waypoint.id,
                                        comment    = comment
                                    )
                                }
                            )
                        }

                        item { Spacer(Modifier.height(Spacing.xl)) }
                    }
                }
            }

            // ── Snackbar ──────────────────────────────────────────────
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                ErrorSnackbar(
                    message   = detailState.error,
                    onDismiss = viewModel::clearError
                )
            }
        }
    }
}

// ── Encabezado ────────────────────────────────────────────────────────────

@Composable
private fun RouteHeaderCard(route: org.smlpartners.smlgo.domain.model.Route) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(Radius.md),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier          = Modifier.padding(Spacing.md).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = route.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(Spacing.xs))
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text  = route.scheduledDate.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            // Badge estado activo
            Surface(
                shape = RoundedCornerShape(Radius.full),
                color = if (route.active)
                    Success.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            ) {
                Text(
                    text     = if (route.active) "Activa" else "Inactiva",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = if (route.active) Success
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                )
            }
        }
    }
}

// ── Progreso ──────────────────────────────────────────────────────────────

@Composable
private fun RouteProgressCard(route: org.smlpartners.smlgo.domain.model.Route) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(Radius.md)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "Progreso",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text  = "${(route.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            LinearProgressIndicator(
                progress    = { route.progress },
                modifier    = Modifier.fillMaxWidth().height(8.dp),
                trackColor  = MaterialTheme.colorScheme.surfaceVariant,
                color       = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(Spacing.sm))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WaypointStatusStat(
                    count = route.pendingCount,
                    label = "Pendientes",
                    color = MaterialTheme.colorScheme.outline
                )
                WaypointStatusStat(
                    count = route.visitedCount,
                    label = "Visitados",
                    color = Success
                )
                WaypointStatusStat(
                    count = route.cancelledCount,
                    label = "Cancelados",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun WaypointStatusStat(
    count : Int,
    label : String,
    color : androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = count.toString(),
            style      = MaterialTheme.typography.headlineSmall,
            color      = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// ── Waypoint Card ─────────────────────────────────────────────────────────

@Composable
private fun WaypointCard(
    waypoint : Waypoint,
    index    : Int,
    isLast   : Boolean,
    onVisit  : (String?) -> Unit,
    onCancel : (String?) -> Unit
) {
    var expanded        by remember { mutableStateOf(false) }
    var showVisitDialog  by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // Color e ícono según estado
    val (statusColor, statusIcon) = when (waypoint.status) {
        WaypointStatus.PENDIENTE  -> Pair(
            MaterialTheme.colorScheme.outline,
            Icons.Filled.RadioButtonUnchecked
        )
        WaypointStatus.VISITA     -> Pair(
            Success,
            Icons.Filled.CheckCircle
        )
        WaypointStatus.CANCELADA  -> Pair(
            MaterialTheme.colorScheme.error,
            Icons.Filled.Cancel
        )
    }

    Row(modifier = Modifier.fillMaxWidth()) {

        // ── Línea de tiempo ───────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.width(40.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape    = CircleShape,
                color    = statusColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = statusIcon,
                        contentDescription = waypoint.status.name,
                        tint               = statusColor,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(Spacing.lg)
                        .padding(vertical = 2.dp)
                ) {
                    Divider(
                        modifier  = Modifier.fillMaxHeight().width(2.dp),
                        color     = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }

        Spacer(Modifier.width(Spacing.sm))

        // ── Contenido del waypoint ────────────────────────────────────
        Card(
            onClick  = { if (waypoint.isPending) expanded = !expanded },
            modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else Spacing.xs),
            shape    = RoundedCornerShape(Radius.md),
            colors   = CardDefaults.cardColors(
                containerColor = when (waypoint.status) {
                    WaypointStatus.VISITA    -> Success.copy(alpha = 0.05f)
                    WaypointStatus.CANCELADA -> MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
                    else                     -> MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {

                // Número de orden + nombre del cliente
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text  = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text     = waypoint.clientName ?: "Cliente ${waypoint.clientId}",
                        style    = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    // Chevron solo si está pendiente
                    if (waypoint.isPending) {
                        Icon(
                            imageVector        = if (expanded)
                                Icons.Filled.ExpandLess
                            else
                                Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                }

                // Dirección
                Spacer(Modifier.height(Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = waypoint.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Hora de visita si fue visitado
                if (waypoint.isVisited && waypoint.visitedAt != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.AccessTime,
                            contentDescription = null,
                            modifier           = Modifier.size(14.dp),
                            tint               = Success
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = "Visitado: ${waypoint.visitedAt}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Success
                        )
                    }
                }

                // Comentario
                if (!waypoint.comment.isNullOrBlank()) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text  = waypoint.comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // ── Acciones expandibles (solo PENDIENTE) ─────────────
                AnimatedVisibility(visible = expanded && waypoint.isPending) {
                    Column {
                        Spacer(Modifier.height(Spacing.sm))
                        HorizontalDivider()
                        Spacer(Modifier.height(Spacing.sm))
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            OutlinedButton(
                                onClick  = { showCancelDialog = true },
                                modifier = Modifier.weight(1f),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.Close,
                                    contentDescription = null,
                                    modifier           = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Cancelar")
                            }
                            Button(
                                onClick  = { showVisitDialog = true },
                                modifier = Modifier.weight(1f),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = Success
                                )
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier           = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Visitar")
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Diálogo confirmar visita ──────────────────────────────────────────
    if (showVisitDialog) {
        WaypointActionDialog(
            title        = "Confirmar visita",
            message      = "¿Marcar a ${waypoint.clientName ?: "este cliente"} como visitado?",
            confirmText  = "Confirmar visita",
            confirmColor = Success,
            onConfirm    = { comment -> onVisit(comment); showVisitDialog = false },
            onDismiss    = { showVisitDialog = false }
        )
    }

    // ── Diálogo cancelar waypoint ─────────────────────────────────────────
    if (showCancelDialog) {
        WaypointActionDialog(
            title        = "Cancelar parada",
            message      = "¿Cancelar la visita a ${waypoint.clientName ?: "este cliente"}?",
            confirmText  = "Cancelar parada",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm    = { comment -> onCancel(comment); showCancelDialog = false },
            onDismiss    = { showCancelDialog = false }
        )
    }
}

// ── Diálogo de acción con comentario opcional ─────────────────────────────

@Composable
private fun WaypointActionDialog(
    title        : String,
    message      : String,
    confirmText  : String,
    confirmColor : androidx.compose.ui.graphics.Color,
    onConfirm    : (String?) -> Unit,
    onDismiss    : () -> Unit
) {
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(title) },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(message)
                OutlinedTextField(
                    value         = comment,
                    onValueChange = { comment = it },
                    label         = { Text("Comentario (opcional)") },
                    shape         = RoundedCornerShape(Radius.md),
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(comment.ifBlank { null }) },
                colors  = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ── Empty state ───────────────────────────────────────────────────────────

@Composable
private fun EmptyRouteDetail() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = Icons.Filled.Route,
                contentDescription = null,
                modifier           = Modifier.size(64.dp),
                tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(Spacing.md))
            Text(
                text  = "Ruta no encontrada",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}