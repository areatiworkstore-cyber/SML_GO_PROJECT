package org.smlpartners.smlgo.ui.dashboard

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
import org.smlpartners.smlgo.domain.model.Route
import org.smlpartners.smlgo.ui.shared.components.ErrorSnackbar
import org.smlpartners.smlgo.ui.shared.components.LoadingOverlay
import org.smlpartners.smlgo.ui.shared.components.SMLGoTopBar

@Composable
fun DashboardScreen(
    onNavigateToClients  : () -> Unit,
    onNavigateToRoutes   : () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateToProfile  : () -> Unit,
    onNavigateToRouteDetail: (Int) -> Unit
) {
    val viewModel: DashboardViewModel = koinViewModel()
    val uiState  by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    Scaffold(
        topBar = {
            SMLGoTopBar(title = "SML Go")
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    icon     = { Icon(Icons.Filled.Map, contentDescription = "Mapa") },
                    label    = { Text("Mapa") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1; onNavigateToRoutes() },
                    icon     = { Icon(Icons.Filled.Route, contentDescription = "Rutas") },
                    label    = { Text("Rutas") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick  = { selectedTab = 2; onNavigateToSchedules() },
                    icon     = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Calendario") },
                    label    = { Text("Agenda") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick  = { selectedTab = 3; onNavigateToClients() },
                    icon     = { Icon(Icons.Filled.People, contentDescription = "Clientes") },
                    label    = { Text("Clientes") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick  = { selectedTab = 4; onNavigateToProfile() },
                    icon     = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                    label    = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> LoadingOverlay()
                else -> DashboardContent(
                    uiState              = uiState,
                    onRouteClick         = onNavigateToRouteDetail
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
private fun DashboardContent(
    uiState     : DashboardUiState,
    onRouteClick: (Int) -> Unit
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Resumen ───────────────────────────────────────────────────
        item {
            SummaryCards(uiState)
        }

        // ── Rutas del día ─────────────────────────────────────────────
        item {
            Text(
                text  = "Rutas activas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (uiState.todayRoutes.isEmpty()) {
            item {
                Text(
                    text  = "No hay rutas activas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            items(uiState.todayRoutes) { route ->
                RouteCard(route = route, onClick = { onRouteClick(route.id) })
            }
        }
    }
}

@Composable
private fun SummaryCards(uiState: DashboardUiState) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            title    = "Clientes",
            value    = uiState.clientsOnMap.size.toString(),
            icon     = Icons.Filled.People
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            title    = "Rutas",
            value    = uiState.todayRoutes.size.toString(),
            icon     = Icons.Filled.Route
        )
    }
}

@Composable
private fun SummaryCard(
    modifier : Modifier,
    title    : String,
    value    : String,
    icon     : androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = title,
                tint               = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text  = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text  = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RouteCard(route: Route, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape   = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Text(
                    text  = route.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text  = route.scheduledDate.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { route.progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "${route.visitedCount} visitados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text  = "${route.pendingCount} pendientes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}