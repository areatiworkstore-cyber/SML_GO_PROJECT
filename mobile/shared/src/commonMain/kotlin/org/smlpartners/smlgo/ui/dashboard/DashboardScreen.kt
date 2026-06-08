package org.smlpartners.smlgo.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.domain.model.Route
import org.smlpartners.smlgo.ui.shared.components.LoadingOverlay
import org.smlpartners.smlgo.ui.shared.components.SMLGoTopBar
import org.smlpartners.smlgo.ui.shared.theme.*
import org.smlpartners.smlgo.core.utils.today

@Composable
fun DashboardScreen(
    onNavigateToClients      : () -> Unit,
    onNavigateToRoutes       : () -> Unit,
    onNavigateToSchedules    : () -> Unit,
    onNavigateToProfile      : () -> Unit,
    onNavigateToRouteDetail  : (Int) -> Unit,
    onNavigateToMap          : () -> Unit
) {
    val viewModel : DashboardViewModel = koinViewModel()
    val uiState   by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    Scaffold(
        topBar = {
            SMLGoTopBar(title = "Dashboard")
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0; onNavigateToMap() },
                    icon     = {
                        Icon(Icons.Filled.Map, contentDescription = "Mapa")
                    },
                    label    = { Text("Mapa", fontSize = 11.sp) },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Primary,
                        selectedTextColor   = Primary,
                        indicatorColor      = Primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1; onNavigateToRoutes() },
                    icon     = { Icon(Icons.Filled.Route, contentDescription = "Rutas") },
                    label    = { Text("Rutas", fontSize = 11.sp) },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Primary,
                        selectedTextColor   = Primary,
                        indicatorColor      = Primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick  = { selectedTab = 2; onNavigateToSchedules() },
                    icon     = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Agenda") },
                    label    = { Text("Agenda", fontSize = 11.sp) },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Primary,
                        selectedTextColor   = Primary,
                        indicatorColor      = Primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick  = { selectedTab = 3; onNavigateToClients() },
                    icon     = { Icon(Icons.Filled.People, contentDescription = "Clientes") },
                    label    = { Text("Clientes", fontSize = 11.sp) },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Primary,
                        selectedTextColor   = Primary,
                        indicatorColor      = Primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick  = { selectedTab = 4; onNavigateToProfile() },
                    icon     = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                    label    = { Text("Perfil", fontSize = 11.sp) },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Primary,
                        selectedTextColor   = Primary,
                        indicatorColor      = Primary.copy(alpha = 0.12f)
                    )
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
                uiState.isLoading -> LoadingOverlay()
                else -> DashboardContent(
                    uiState              = uiState,
                    onRouteClick         = onNavigateToRouteDetail,
                    onNavigateToClients  = onNavigateToClients,
                    onNavigateToRoutes   = onNavigateToRoutes,
                    onNavigateToSchedules = onNavigateToSchedules,
                    onNavigateToMap      = onNavigateToMap
                )
            }
        }
    }
}

// ── Contenido principal ───────────────────────────────────────────────────

@Composable
private fun DashboardContent(
    uiState               : DashboardUiState,
    onRouteClick          : (Int) -> Unit,
    onNavigateToClients   : () -> Unit,
    onNavigateToRoutes    : () -> Unit,
    onNavigateToSchedules : () -> Unit,
    onNavigateToMap       : () -> Unit
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Header de bienvenida ──────────────────────────────────────
        item { DashboardHeader() }

        // ── Cards de resumen ──────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier    = Modifier.weight(1f),
                        icon        = Icons.Filled.People,
                        value       = uiState.clientsOnMap.size.toString(),
                        label       = "Clientes",
                        color       = Primary,
                        onClick     = onNavigateToClients
                    )
                    StatCard(
                        modifier    = Modifier.weight(1f),
                        icon        = Icons.Filled.Route,
                        value       = uiState.todayRoutes.size.toString(),
                        label       = "Rutas",
                        color       = Success,
                        onClick     = onNavigateToRoutes
                    )
                }
            }
        }

        // ── Accesos rápidos ───────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text       = "Accesos rápidos",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Filled.Map,
                        label    = "Ver mapa",
                        color    = Color(0xFF3B82F6),
                        onClick  = onNavigateToMap
                    )
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Filled.CalendarMonth,
                        label    = "Agenda",
                        color    = Color(0xFF8B5CF6),
                        onClick  = onNavigateToSchedules
                    )
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Filled.AddCircle,
                        label    = "Nueva ruta",
                        color    = Primary,
                        onClick  = onNavigateToRoutes
                    )
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Filled.PersonAdd,
                        label    = "Cliente",
                        color    = Success,
                        onClick  = onNavigateToClients
                    )
                }
            }
        }

        // ── Rutas activas ─────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = "Rutas activas",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary
                    )
                    if (uiState.todayRoutes.isNotEmpty()) {
                        TextButton(onClick = onNavigateToRoutes) {
                            Text(
                                text  = "Ver todas",
                                color = Primary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (uiState.todayRoutes.isEmpty()) {
            item {
                EmptyRoutesCard(onClick = onNavigateToRoutes)
            }
        } else {
            items(
                items = uiState.todayRoutes,
                key   = { it.id }
            ) { route ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    DashboardRouteCard(
                        route   = route,
                        onClick = { onRouteClick(route.id) }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Header ────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary.copy(alpha = 0.08f))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "Buenos días 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text       = today().toString(),
                    style      = MaterialTheme.typography.bodySmall,
                    color      = TextMuted
                )
            }
            // Badge de fecha
            Surface(
                shape = RoundedCornerShape(Radius.md),
                color = Primary.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = Primary
                    )
                    Text(
                        text  = "Hoy",
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Stat Card ─────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    modifier : Modifier,
    icon     : ImageVector,
    value    : String,
    label    : String,
    color    : Color,
    onClick  : () -> Unit
) {
    Card(
        onClick   = onClick,
        modifier  = modifier,
        shape     = RoundedCornerShape(Radius.md),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = color,
                    modifier           = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text       = value,
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = color
                )
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

// ── Quick Access Card ─────────────────────────────────────────────────────

@Composable
private fun QuickAccessCard(
    modifier : Modifier,
    icon     : ImageVector,
    label    : String,
    color    : Color,
    onClick  : () -> Unit
) {
    Card(
        onClick   = onClick,
        modifier  = modifier,
        shape     = RoundedCornerShape(Radius.md),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = color,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelSmall,
                color     = TextSecondary,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Route Card en Dashboard ───────────────────────────────────────────────

@Composable
private fun DashboardRouteCard(
    route  : Route,
    onClick: () -> Unit
) {
    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(Radius.md),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Nombre + fecha ────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Route,
                        contentDescription = null,
                        tint               = Primary,
                        modifier           = Modifier.size(20.dp)
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
                    Text(
                        text  = route.scheduledDate.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Icon(
                    imageVector        = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint               = TextMuted,
                    modifier           = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Barra de progreso ─────────────────────────────────────
            LinearProgressIndicator(
                progress   = { route.progress },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color      = Primary,
                trackColor = Primary.copy(alpha = 0.12f)
            )

            Spacer(Modifier.height(10.dp))

            // ── Stats en una fila ─────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RouteStatChip(
                    modifier = Modifier.weight(1f),
                    count    = route.pendingCount,
                    label    = "Pendientes",
                    color    = TextSecondary,
                    bgColor  = MaterialTheme.colorScheme.surfaceVariant
                )
                RouteStatChip(
                    modifier = Modifier.weight(1f),
                    count    = route.visitedCount,
                    label    = "Visitados",
                    color    = Success,
                    bgColor  = Success.copy(alpha = 0.1f)
                )
                RouteStatChip(
                    modifier = Modifier.weight(1f),
                    count    = route.cancelledCount,
                    label    = "Cancelados",
                    color    = MaterialTheme.colorScheme.error,
                    bgColor  = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                )
            }

            // ── Progreso % ────────────────────────────────────────────
            if (route.totalCount > 0) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text  = "${(route.progress * 100).toInt()}% · ${route.totalCount} paradas",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteStatChip(
    modifier : Modifier,
    count    : Int,
    label    : String,
    color    : Color,
    bgColor  : Color
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(Radius.sm),
        color    = bgColor
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
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

// ── Empty state de rutas ──────────────────────────────────────────────────

@Composable
private fun EmptyRoutesCard(onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(Radius.md),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.AddCircle,
                    contentDescription = null,
                    tint               = Primary,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Sin rutas activas",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color      = TextPrimary
                )
                Text(
                    text  = "Toca para crear una nueva ruta",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector        = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint               = TextMuted,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}