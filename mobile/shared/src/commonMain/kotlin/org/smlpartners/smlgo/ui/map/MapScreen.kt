package org.smlpartners.smlgo.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.ui.dashboard.MapViewModel
import org.smlpartners.smlgo.ui.shared.components.ErrorSnackbar
import org.smlpartners.smlgo.ui.shared.components.LoadingOverlay
import org.smlpartners.smlgo.ui.shared.components.SMLGoTopBar
import org.smlpartners.smlgo.ui.shared.theme.Radius
import org.smlpartners.smlgo.ui.shared.theme.Spacing

@Composable
fun MapScreen(
    onNavigateToClientDetail : (Int) -> Unit,
    onBack                   : () -> Unit
) {
    val viewModel : MapViewModel = koinViewModel()
    val uiState   by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMarkers() }

    Scaffold(
        topBar = {
            SMLGoTopBar(
                title   = "Mapa de clientes",
                onBack  = onBack,
                actions = {
                    IconButton(onClick = { viewModel.loadMarkers() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            // ── Mapa nativo ───────────────────────────────────────────
            if (uiState.isLoading) {
                LoadingOverlay()
            } else {
                PlatformMapView(
                    markers          = uiState.markers,
                    onMarkerSelected = { viewModel.onMarkerSelected(it) },
                    modifier         = Modifier.fillMaxSize()
                )
            }

            // ── Contador de clientes ──────────────────────────────────
            if (uiState.markers.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.md),
                    shape  = RoundedCornerShape(Radius.full),
                    color  = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier          = Modifier.padding(
                            horizontal = Spacing.md,
                            vertical   = Spacing.xs
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.People,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp),
                            tint               = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(Spacing.xs))
                        Text(
                            text  = "${uiState.markers.size} clientes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // ── Bottom sheet al seleccionar marker ────────────────────
            uiState.selectedClient?.let { client ->
                ClientMarkerBottomCard(
                    client              = client,
                    onNavigateToDetail  = { onNavigateToClientDetail(client.id) },
                    onDismiss           = { viewModel.clearSelection() },
                    modifier            = Modifier.align(Alignment.BottomCenter)
                )
            }

            // ── Snackbar ──────────────────────────────────────────────
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
}

// ── Card que aparece al tocar un marker ───────────────────────────────────

@Composable
private fun ClientMarkerBottomCard(
    client             : Client,
    onNavigateToDetail : () -> Unit,
    onDismiss          : () -> Unit,
    modifier           : Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        shape   = RoundedCornerShape(Radius.lg),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = client.name ?: "Sin nombre",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text  = "Código: ${client.code ?: "Sin código"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    if (!client.address.isNullOrBlank()) {
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
                                text  = client.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            // Coordenadas
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CoordinateChip(
                    label = "Lat",
                    value = client.latitude?.toString()?.take(9) ?: "-"
                )
                CoordinateChip(
                    label = "Lng",
                    value = client.longitude?.toString()?.take(9) ?: "-"
                )
                if (!client.cellphone.isNullOrBlank()) {
                    CoordinateChip(label = "Tel", value = client.cellphone)
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // Botón ver detalle
            Button(
                onClick  = onNavigateToDetail,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(Radius.md)
            ) {
                Icon(
                    imageVector        = Icons.Filled.Person,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text("Ver detalle del cliente")
            }
        }
    }
}

@Composable
private fun CoordinateChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(Radius.sm),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}