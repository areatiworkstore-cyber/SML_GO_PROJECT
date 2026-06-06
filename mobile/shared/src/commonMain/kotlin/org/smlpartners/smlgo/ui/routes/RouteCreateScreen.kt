package org.smlpartners.smlgo.ui.routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.ui.shared.components.*
import org.smlpartners.smlgo.ui.shared.theme.Radius
import org.smlpartners.smlgo.ui.shared.theme.Spacing
import org.smlpartners.smlgo.ui.shared.theme.Success
import org.smlpartners.smlgo.core.utils.today

@Composable
fun RouteCreateScreen(
    onCreated : () -> Unit,
    onBack    : () -> Unit
) {
    val viewModel  : RouteViewModel = koinViewModel()
    val formState  by viewModel.formState.collectAsState()

    var routeName      by remember { mutableStateOf("") }
    var scheduledDate  by remember { mutableStateOf(today()) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadAvailableClients() }

    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) {
            viewModel.clearSaved()
            onCreated()
        }
    }

    Scaffold(
        topBar = {
            SMLGoTopBar(title = "Nueva ruta", onBack = onBack)
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                contentPadding      = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // ── Nombre de la ruta ─────────────────────────────────
                item {
                    SMLGoTextField(
                        value         = routeName,
                        onValueChange = { routeName = it },
                        label         = "Nombre de la ruta *"
                    )
                }

                // ── Fecha programada ──────────────────────────────────
                item {
                    OutlinedButton(
                        onClick  = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(Radius.md)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Fecha: $scheduledDate")
                    }
                }

                // ── Clientes seleccionados ────────────────────────────
                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Clientes disponibles",
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (formState.selectedClients.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(Radius.full),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text     = "${formState.selectedClients.size} seleccionados",
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        horizontal = Spacing.sm,
                                        vertical   = Spacing.xs
                                    )
                                )
                            }
                        }
                    }
                }

                // ── Lista de clientes ─────────────────────────────────
                if (formState.isLoading) {
                    item { LoadingOverlay() }
                } else if (formState.availableClients.isEmpty()) {
                    item {
                        Text(
                            text  = "No hay clientes con ubicación registrada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    items(
                        items = formState.availableClients,
                        key   = { it.id }
                    ) { client ->
                        ClientSelectionCard(
                            client     = client,
                            isSelected = formState.selectedClients.any { it.id == client.id },
                            onToggle   = { viewModel.toggleClientSelection(client) }
                        )
                    }
                }

                // ── Botón crear ───────────────────────────────────────
                item {
                    Spacer(Modifier.height(Spacing.sm))
                    SMLGoButton(
                        text      = "Crear ruta",
                        onClick   = { viewModel.createRoute(routeName, scheduledDate) },
                        isLoading = formState.isLoading,
                        enabled   = routeName.isNotBlank() &&
                                formState.selectedClients.isNotEmpty()
                    )
                }
            }

            // ── Snackbar ──────────────────────────────────────────────
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                ErrorSnackbar(
                    message   = formState.error,
                    onDismiss = viewModel::clearError
                )
            }
        }
    }
}

@Composable
private fun ClientSelectionCard(
    client     : Client,
    isSelected : Boolean,
    onToggle   : () -> Unit
) {
    Card(
        onClick  = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(Radius.md),
        colors   = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border   = if (isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Row(
            modifier          = Modifier.padding(Spacing.md).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked         = isSelected,
                onCheckedChange = { onToggle() }
            )
            Spacer(Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = client.name ?: "Sin nombre",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text  = client.address ?: "Sin dirección",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            if (client.hasLocation) {
                Icon(
                    imageVector        = Icons.Filled.LocationOn,
                    contentDescription = "Tiene ubicación",
                    tint               = Success,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}