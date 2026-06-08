package org.smlpartners.smlgo.ui.clients

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
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.ui.shared.components.LoadingOverlay
import org.smlpartners.smlgo.ui.shared.components.SMLGoTopBar
import org.smlpartners.smlgo.ui.shared.theme.*

@Composable
fun ClientListScreen(
    onNavigateToForm : (Int?) -> Unit,
    onBack           : () -> Unit
) {
    val viewModel  : ClientViewModel = koinViewModel()
    val uiState    by viewModel.listState.collectAsState()
    val listState  = rememberLazyListState()

    // Oculta el FAB cuando el usuario hace scroll hacia abajo
    val showFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    LaunchedEffect(Unit) {
        viewModel.resetList()
        viewModel.loadClients()
    }

    Scaffold(
        topBar = {
            SMLGoTopBar(title = "Clientes", onBack = onBack)
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick            = { onNavigateToForm(null) },
                    containerColor     = Primary,
                    contentColor       = Color.White,
                    icon               = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text               = { Text("Nuevo cliente", fontWeight = FontWeight.Medium) }
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
                uiState.isLoading         -> LoadingOverlay()
                uiState.clients.isEmpty() -> EmptyClients(
                    onAdd = { onNavigateToForm(null) }
                )
                else -> {
                    LazyColumn(
                        state               = listState,
                        contentPadding      = PaddingValues(
                            start  = 16.dp,
                            end    = 16.dp,
                            top    = 12.dp,
                            bottom = 88.dp   // ← espacio para el FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ── Contador ──────────────────────────────────
                        item {
                            Text(
                                text  = "${uiState.clients.size} clientes registrados",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        items(
                            items = uiState.clients,
                            key   = { it.id }
                        ) { client ->
                            ClientCard(
                                client   = client,
                                onEdit   = { onNavigateToForm(client.id) },
                                onToggle = { viewModel.toggleClientActive(client) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Card de cliente ───────────────────────────────────────────────────────

@Composable
private fun ClientCard(
    client   : Client,
    onEdit   : () -> Unit,
    onToggle : () -> Unit
) {
    var showToggleDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (client.active)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Avatar con inicial ────────────────────────────────────
            ClientAvatar(
                name     = client.name,
                isActive = client.active
            )

            Spacer(Modifier.width(12.dp))

            // ── Datos del cliente ─────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text       = client.name ?: "Sin nombre",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (client.active) TextPrimary
                        else TextSecondary,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    // Badge inactivo
                    if (!client.active) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text     = "Inactivo",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (!client.address.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier           = Modifier.size(12.dp),
                            tint               = TextMuted
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text     = client.address,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!client.cellphone.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.Phone,
                            contentDescription = null,
                            modifier           = Modifier.size(12.dp),
                            tint               = TextMuted
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text  = client.cellphone,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // ── Chips de info ─────────────────────────────────────
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (client.hasLocation) {
                        InfoChip(
                            icon  = Icons.Filled.MyLocation,
                            label = "GPS",
                            color = Success
                        )
                    }
                    if (!client.code.isNullOrBlank()) {
                        InfoChip(
                            icon  = Icons.Filled.Tag,
                            label = client.code,
                            color = Primary
                        )
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            // ── Acciones ──────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Botón editar
                FilledIconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp),
                    colors   = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Primary.copy(alpha = 0.12f),
                        contentColor   = Primary
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Edit,
                        contentDescription = "Editar",
                        modifier           = Modifier.size(16.dp)
                    )
                }

                // Botón activar/desactivar
                FilledIconButton(
                    onClick  = { showToggleDialog = true },
                    modifier = Modifier.size(36.dp),
                    colors   = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (client.active)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        else
                            Success.copy(alpha = 0.12f),
                        contentColor   = if (client.active)
                            MaterialTheme.colorScheme.error
                        else
                            Success
                    )
                ) {
                    Icon(
                        imageVector        = if (client.active)
                            Icons.Filled.PersonOff
                        else
                            Icons.Filled.PersonAdd,
                        contentDescription = if (client.active) "Desactivar" else "Activar",
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    // ── Diálogo confirmar toggle ──────────────────────────────────────────
    if (showToggleDialog) {
        AlertDialog(
            onDismissRequest = { showToggleDialog = false },
            icon             = {
                Icon(
                    imageVector = if (client.active) Icons.Filled.PersonOff
                    else Icons.Filled.PersonAdd,
                    contentDescription = null,
                    tint = if (client.active) MaterialTheme.colorScheme.error
                    else Success
                )
            },
            title = {
                Text(if (client.active) "Desactivar cliente" else "Activar cliente")
            },
            text  = {
                Text(
                    if (client.active)
                        "¿Desactivar a ${client.name ?: "este cliente"}? No aparecerá en rutas nuevas."
                    else
                        "¿Activar a ${client.name ?: "este cliente"}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = { onToggle(); showToggleDialog = false },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = if (client.active)
                            MaterialTheme.colorScheme.error
                        else
                            Success
                    )
                ) {
                    Text(if (client.active) "Desactivar" else "Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showToggleDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ── Avatar circular con inicial ───────────────────────────────────────────

@Composable
private fun ClientAvatar(name: String?, isActive: Boolean) {
    val initial = name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val bgColor = if (isActive) Primary.copy(alpha = 0.15f)
    else MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier         = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = initial,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = if (isActive) Primary else TextMuted
        )
    }
}

// ── Chip de información ───────────────────────────────────────────────────

@Composable
private fun InfoChip(
    icon  : androidx.compose.ui.graphics.vector.ImageVector,
    label : String,
    color : Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                modifier           = Modifier.size(10.dp),
                tint               = color
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontSize = 10.sp
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────

@Composable
private fun EmptyClients(onAdd: () -> Unit) {
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
                imageVector        = Icons.Filled.People,
                contentDescription = null,
                modifier           = Modifier.size(48.dp),
                tint               = Primary
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "Sin clientes aún",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "Registra tu primer cliente\ntocando el botón de abajo",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick        = onAdd,
            colors = ButtonDefaults.buttonColors(
                contentColor = Primary,
                containerColor = TextInverse
            ),
            shape          = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Agregar cliente")
        }
    }
}