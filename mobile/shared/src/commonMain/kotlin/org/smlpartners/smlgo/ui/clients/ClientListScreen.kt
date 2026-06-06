package org.smlpartners.smlgo.ui.clients

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

@Composable
fun ClientListScreen(
    onNavigateToForm: (Int?) -> Unit,
    onBack          : () -> Unit
) {
    val viewModel: ClientViewModel = koinViewModel()
    val uiState  by viewModel.listState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadClients() }

    Scaffold(
        topBar = {
            SMLGoTopBar(
                title  = "Clientes",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo cliente")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading          -> LoadingOverlay()
                uiState.clients.isEmpty()  -> EmptyClients()
                else -> ClientList(
                    clients = uiState.clients,
                    onEdit  = { onNavigateToForm(it.id) }
                )
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
}

@Composable
private fun ClientList(
    clients : List<Client>,
    onEdit  : (Client) -> Unit
) {
    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(clients, key = { it.id }) { client ->
            ClientCard(client = client, onEdit = { onEdit(client) })
        }
    }
}

@Composable
private fun ClientCard(client: Client, onEdit: () -> Unit) {
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
                    text  = client.name ?: "Sin nombre",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text  = client.address ?: "Sin dirección",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (client.hasLocation) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.LocationOn,
                            contentDescription = "Tiene ubicación",
                            tint               = MaterialTheme.colorScheme.secondary,
                            modifier           = Modifier.size(14.dp)
                        )
                        Text(
                            text  = "Ubicación registrada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar")
            }
        }
    }
}

@Composable
private fun EmptyClients() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = Icons.Filled.People,
                contentDescription = null,
                modifier           = Modifier.size(64.dp),
                tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text  = "No hay clientes registrados",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}