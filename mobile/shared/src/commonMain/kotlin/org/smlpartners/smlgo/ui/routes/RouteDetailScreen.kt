package org.smlpartners.smlgo.ui.routes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.domain.model.Waypoint
import org.smlpartners.smlgo.domain.model.WaypointStatus
import org.smlpartners.smlgo.core.error.GlobalErrorHandler
import org.smlpartners.smlgo.core.network.ApiError
import org.smlpartners.smlgo.ui.routes.components.FullPhotoViewerModal
import org.smlpartners.smlgo.ui.routes.components.VisitAuditDialog
import org.smlpartners.smlgo.ui.shared.components.*
import org.smlpartners.smlgo.ui.shared.theme.Spacing
import org.smlpartners.smlgo.ui.shared.theme.Radius
import org.smlpartners.smlgo.ui.shared.theme.Success

@Composable
fun RouteDetailScreen(
    routeId       : Int,
    onBack        : () -> Unit,
    onGetLocation : (onResult: (Double?, Double?) -> Unit) -> Unit = { callback -> callback(null, null) }
) {
    val viewModel  : RouteViewModel = koinViewModel()
    val detailState by viewModel.detailState.collectAsState()

    LaunchedEffect(routeId) { viewModel.loadRouteDetail(routeId) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    detailState.isLoading     -> LoadingOverlay()
                    detailState.route == null -> EmptyRouteDetail()
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
                                    text       = "Paradas (${route.waypoints.size})",
                                    style      = MaterialTheme.typography.titleMedium,
                                    color      = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier   = Modifier.padding(vertical = Spacing.xs)
                                )
                            }

                            // ── Lista de waypoints ────────────────────────
                            itemsIndexed(
                                items = route.waypoints.sortedBy { it.orderSequence },
                                key   = { _, w -> w.id }
                            ) { index, waypoint ->
                                WaypointCard(
                                    waypoint          = waypoint,
                                    index             = index,
                                    isLast            = index == route.waypoints.size - 1,
                                    isSubmittingVisit = detailState.isSubmittingVisit,
                                    isLoadingPhoto    = detailState.loadingPhotoForWaypointId == waypoint.id,
                                    onVisitWithAudit  = { comment, photoBytes, filename ->
                                        onGetLocation { lat, lng ->
                                            if (lat != null && lng != null) {
                                                viewModel.submitWaypointVisitWithAudit(
                                                    routeId    = routeId,
                                                    waypointId = waypoint.id,
                                                    comment    = comment,
                                                    photoBytes = photoBytes,
                                                    filename   = filename,
                                                    latitude   = lat,
                                                    longitude  = lng
                                                )
                                            } else {
                                                GlobalErrorHandler.emit(
                                                    ApiError.UnknownError("No se pudo obtener la ubicación GPS actual del vendedor. Verifica que los servicios de ubicación estén activados.")
                                                )
                                            }
                                        }
                                    },
                                    onCancel  = { comment ->
                                        viewModel.cancelWaypoint(
                                            routeId    = routeId,
                                            waypointId = waypoint.id,
                                            comment    = comment
                                        )
                                    },
                                    onViewPhoto = {
                                        viewModel.loadWaypointPhotoUrl(waypoint.id)
                                    }
                                )
                            }

                            item { Spacer(Modifier.height(Spacing.xl)) }
                        }
                    }
                }
            }
        }

        // ── Overlay global "Subiendo foto..." ─────────────────────────────
        if (detailState.isUploadingPhoto) {
            PhotoUploadOverlay()
        }
    }

    // ── Visor de foto cargada desde el servidor ───────────────────────────
    val loadedUrl = detailState.loadedPhotoUrl
    val loadingForId = detailState.loadingPhotoForWaypointId
    if (loadedUrl != null) {
        val waypointForPhoto = detailState.route?.waypoints?.find {
            !it.urlPhoto.isNullOrBlank()
        }
        FullPhotoViewerModal(
            photoUrl   = loadedUrl,
            clientName = waypointForPhoto?.clientName,
            onDismiss  = { viewModel.clearLoadedPhotoUrl() }
        )
    }
}

// ── Overlay de subida de foto ─────────────────────────────────────────────

@Composable
private fun PhotoUploadOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape  = RoundedCornerShape(Radius.lg),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier            = Modifier.padding(horizontal = 40.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(52.dp),
                    color       = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Text(
                    text       = "Subiendo imagen...",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = "Por favor espera, estamos guardando\nla evidencia en el servidor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                    text       = route.name,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(Spacing.xs))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = Color.Black
                    )
                    Text(
                        text       = route.scheduledDate.toString(),
                        style      = MaterialTheme.typography.bodySmall,
                        color      = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            // Badge estado activo
            Surface(
                shape = RoundedCornerShape(Radius.full),
                color = if (route.active)
                    Success.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            ) {
                Text(
                    text       = if (route.active) "Activa" else "Inactiva",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = if (route.active) Success else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
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
                    text       = "Progreso",
                    style      = MaterialTheme.typography.titleSmall,
                    color      = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text       = "${(route.progress * 100).toInt()}%",
                    style      = MaterialTheme.typography.titleSmall,
                    color      = MaterialTheme.colorScheme.primary,
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
                    color = Color.Black
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
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Waypoint Card ─────────────────────────────────────────────────────────

@Composable
private fun WaypointCard(
    waypoint          : Waypoint,
    index             : Int,
    isLast            : Boolean,
    isSubmittingVisit : Boolean,
    isLoadingPhoto    : Boolean,
    onVisitWithAudit  : (comment: String?, photoBytes: ByteArray?, filename: String?) -> Unit,
    onCancel          : (String?) -> Unit,
    onViewPhoto       : () -> Unit
) {
    var expanded          by remember { mutableStateOf(false) }
    var showVisitDialog   by remember { mutableStateOf(false) }
    var showCancelDialog  by remember { mutableStateOf(false) }

    // Color e ícono según estado
    val (statusColor, statusIcon) = when (waypoint.status) {
        WaypointStatus.PENDIENTE  -> Pair(
            Color.Black,
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
                    HorizontalDivider(
                        modifier = Modifier.fillMaxHeight().width(2.dp),
                        color    = MaterialTheme.colorScheme.outlineVariant
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
                    WaypointStatus.VISITA    -> Success.copy(alpha = 0.1f)
                    WaypointStatus.CANCELADA -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    else                     -> MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {

                // Número de orden + nombre del cliente + Badge
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
                                text       = "${index + 1}",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text       = waypoint.clientName ?: "Cliente ${waypoint.clientId}",
                        style      = MaterialTheme.typography.titleSmall,
                        color      = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.weight(1f)
                    )

                    if (waypoint.isVisited) {
                        Surface(
                            shape = RoundedCornerShape(Radius.full),
                            color = Success.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier          = Modifier.padding(horizontal = Spacing.xs, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.Verified,
                                    contentDescription = null,
                                    tint               = Success,
                                    modifier           = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text       = "Auditado",
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = Success,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (waypoint.isPending) {
                        Icon(
                            imageVector        = if (expanded)
                                Icons.Filled.ExpandLess
                            else
                                Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint               = Color.Black,
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
                        tint               = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text       = waypoint.address,
                        style      = MaterialTheme.typography.bodySmall,
                        color      = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Datos de Auditoría si ya fue visitado
                if (waypoint.isVisited) {
                    Spacer(Modifier.height(Spacing.xs))
                    if (waypoint.visitedAt != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Filled.AccessTime,
                                contentDescription = null,
                                modifier           = Modifier.size(14.dp),
                                tint               = Success
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text       = "Visitado: ${waypoint.visitedAt}",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Observación de auditoría
                    if (!waypoint.comment.isNullOrBlank()) {
                        Spacer(Modifier.height(Spacing.xs))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector        = Icons.Filled.Comment,
                                contentDescription = null,
                                modifier           = Modifier.size(14.dp).padding(top = 2.dp),
                                tint               = Color.Black
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text       = "Obs: ${waypoint.comment}",
                                style      = MaterialTheme.typography.bodySmall,
                                color      = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Evidencia fotográfica — botón para cargar foto del servidor
                    if (!waypoint.urlPhoto.isNullOrBlank()) {
                        Spacer(Modifier.height(Spacing.xs))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.sm))
                                .clickable(enabled = !isLoadingPhoto) { onViewPhoto() },
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier          = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isLoadingPhoto) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color       = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector        = Icons.Filled.CameraAlt,
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text       = if (isLoadingPhoto) "Cargando foto..." else "Ver foto de evidencia",
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
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
                                Text("Cancelar", color = Color.Red, fontWeight = FontWeight.Bold)
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
                                    modifier           = Modifier.size(16.dp),
                                    tint               = Color.White
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Visitar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Diálogo de Auditoría de Visita ─────────────────────────────────────
    if (showVisitDialog) {
        VisitAuditDialog(
            waypoint     = waypoint,
            isSubmitting = isSubmittingVisit,
            onConfirm    = { comment, photoBytes, filename ->
                onVisitWithAudit(comment, photoBytes, filename)
                showVisitDialog = false
            },
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
        title   = { Text(title, color = Color.Black, fontWeight = FontWeight.Bold) },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(message, color = Color.Black, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value         = comment,
                    onValueChange = { comment = it },
                    label         = { Text("Motivo / Comentario (opcional)", color = Color.Black, fontWeight = FontWeight.SemiBold) },
                    shape         = RoundedCornerShape(Radius.md),
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedTextColor    = Color.Black,
                        unfocusedTextColor  = Color.Black,
                        focusedLabelColor   = Color.Black,
                        unfocusedLabelColor = Color.Black
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(comment.ifBlank { null }) },
                colors  = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmText, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Black, fontWeight = FontWeight.Bold) }
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
                text       = "Ruta no encontrada",
                style      = MaterialTheme.typography.bodyLarge,
                color      = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}