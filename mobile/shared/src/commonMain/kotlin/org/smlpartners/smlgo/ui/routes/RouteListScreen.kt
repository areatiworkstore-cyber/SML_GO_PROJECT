package org.smlpartners.smlgo.ui.routes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.domain.model.Route
import org.smlpartners.smlgo.ui.shared.components.LoadingOverlay
import org.smlpartners.smlgo.ui.shared.components.SMLGoTopBar
import org.smlpartners.smlgo.ui.shared.theme.*

@Composable
fun RouteListScreen(
    onNavigateToCreate : () -> Unit,
    onNavigateToDetail : (Int) -> Unit,
    onBack             : () -> Unit
) {
    val viewModel : RouteViewModel = koinViewModel()
    val uiState   by viewModel.listState.collectAsState()
    val listState = rememberLazyListState()

    val showFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    LaunchedEffect(Unit) {
        viewModel.resetList()
        viewModel.loadRoutes()
    }

    Scaffold(
        topBar = {
            SMLGoTopBar(title = "Rutas", onBack = onBack)
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick        = onNavigateToCreate,
                    containerColor = Primary,
                    contentColor   = Color.White,
                    icon           = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text           = { Text("Nueva ruta", fontWeight = FontWeight.Medium) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading        -> LoadingOverlay()
                uiState.routes.isEmpty() -> EmptyRoutes(onAdd = onNavigateToCreate)
                else -> LazyColumn(
                    state               = listState,
                    contentPadding      = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 12.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text     = "${uiState.routes.size} rutas registradas",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = TextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(
                        items = uiState.routes,
                        key   = { it.id }
                    ) { route ->
                        RouteItemCard(
                            route    = route,
                            onClick  = { onNavigateToDetail(route.id) },
                            onDelete = { viewModel.deleteRoute(route.id) {} }
                        )
                    }
                }
            }
        }
    }
}

// ── Card de ruta ──────────────────────────────────────────────────────────

@Composable
private fun RouteItemCard(
    route   : Route,
    onClick : () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Fila superior: icono + nombre + fecha + eliminar ──────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar con ícono de ruta
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Route,
                        contentDescription = null,
                        tint               = Primary,
                        modifier           = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = route.name,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            modifier           = Modifier.size(12.dp),
                            tint               = TextMuted
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = route.scheduledDate.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Badge activa/inactiva
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (route.active)
                        Success.copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text     = if (route.active) "Activa" else "Inactiva",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = if (route.active) Success
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Botón eliminar
                FilledIconButton(
                    onClick  = { showDeleteDialog = true },
                    modifier = Modifier.size(36.dp),
                    colors   = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        contentColor   = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Delete,
                        contentDescription = "Eliminar",
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Barra de progreso ─────────────────────────────────────
            LinearProgressIndicator(
                progress    = { route.progress },
                modifier    = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color       = Primary,
                trackColor  = Primary.copy(alpha = 0.12f)
            )

            Spacer(Modifier.height(10.dp))

            // ── Chips de estado en una sola fila ──────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RouteStatusChip(
                    modifier = Modifier.weight(1f),
                    count    = route.pendingCount,
                    label    = "Pendientes",
                    color    = TextSecondary,
                    bgColor  = MaterialTheme.colorScheme.surfaceVariant
                )
                RouteStatusChip(
                    modifier = Modifier.weight(1f),
                    count    = route.visitedCount,
                    label    = "Visitados",
                    color    = Success,
                    bgColor  = Success.copy(alpha = 0.1f)
                )
                RouteStatusChip(
                    modifier = Modifier.weight(1f),
                    count    = route.cancelledCount,
                    label    = "Cancelados",
                    color    = MaterialTheme.colorScheme.error,
                    bgColor  = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                )
            }

            // ── Total waypoints ───────────────────────────────────────
            if (route.totalCount > 0) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text  = "${(route.progress * 100).toInt()}% completado · ${route.totalCount} paradas",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }

    // ── Diálogo confirmar eliminación ─────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon             = {
                Icon(
                    imageVector        = Icons.Filled.Delete,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error
                )
            },
            title            = { Text("Eliminar ruta") },
            text             = {
                Text("¿Estás seguro de eliminar \"${route.name}\"? Esta acción no se puede deshacer.")
            },
            confirmButton    = {
                Button(
                    onClick = { onDelete(); showDeleteDialog = false },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton    = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ── Chip de estado de ruta ────────────────────────────────────────────────

@Composable
private fun RouteStatusChip(
    modifier : Modifier,
    count    : Int,
    label    : String,
    color    : Color,
    bgColor  : Color
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(10.dp),
        color    = bgColor
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = count.toString(),
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = color
            )
            Text(
                text     = label,
                style    = MaterialTheme.typography.labelSmall,
                color    = color.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────

@Composable
private fun EmptyRoutes(onAdd: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier         = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Filled.Route,
                contentDescription = null,
                modifier           = Modifier.size(48.dp),
                tint               = Primary
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "Sin rutas aún",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "Crea tu primera ruta\ntocando el botón de abajo",
            style     = MaterialTheme.typography.bodyMedium,
            color     = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAdd,
            colors  = ButtonDefaults.buttonColors(containerColor = Primary),
            shape   = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Crear ruta")
        }
    }
}