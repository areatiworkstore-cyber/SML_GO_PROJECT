package org.smlpartners.smlgo.ui.shared

import androidx.compose.runtime.*
import org.smlpartners.smlgo.ui.clients.ClientFormScreen
import org.smlpartners.smlgo.ui.clients.ClientListScreen
import org.smlpartners.smlgo.ui.dashboard.DashboardScreen
import org.smlpartners.smlgo.ui.auth.LoginScreen
import org.smlpartners.smlgo.ui.profile.ProfileScreen
import org.smlpartners.smlgo.ui.routes.RouteCreateScreen
import org.smlpartners.smlgo.ui.routes.RouteDetailScreen
import org.smlpartners.smlgo.ui.routes.RouteListScreen
import org.smlpartners.smlgo.ui.schedule.ScheduleScreen

sealed class Screen {
    object Login                                  : Screen()
    object Dashboard                              : Screen()
    object Clients                                : Screen()
    data class ClientForm(val clientId: Int?)     : Screen()
    object Routes                                 : Screen()
    object RouteCreate                            : Screen()
    data class RouteDetail(val routeId: Int)      : Screen()
    object Schedules                              : Screen()
    object Profile                                : Screen()
}

@Composable
fun AppNavigation(
    onGetLocation: (onResult: (Double, Double) -> Unit) -> Unit = { _ -> }
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    when (val screen = currentScreen) {
        Screen.Login         -> LoginScreen(
            onLoginSuccess = { currentScreen = Screen.Dashboard }
        )
        Screen.Dashboard     -> DashboardScreen(
            onNavigateToClients       = { currentScreen = Screen.Clients },
            onNavigateToRoutes        = { currentScreen = Screen.Routes },
            onNavigateToSchedules     = { currentScreen = Screen.Schedules },
            onNavigateToProfile       = { currentScreen = Screen.Profile },
            onNavigateToRouteDetail   = { currentScreen = Screen.RouteDetail(it) }
        )
        Screen.Clients       -> ClientListScreen(
            onNavigateToForm = { currentScreen = Screen.ClientForm(it) },
            onBack           = { currentScreen = Screen.Dashboard }
        )
        is Screen.ClientForm -> ClientFormScreen(
            clientId      = screen.clientId,
            onSaved       = { currentScreen = Screen.Clients },
            onBack        = { currentScreen = Screen.Clients },
            onGetLocation = onGetLocation
        )
        Screen.Routes        -> RouteListScreen(
            onNavigateToCreate = { currentScreen = Screen.RouteCreate },
            onNavigateToDetail = { currentScreen = Screen.RouteDetail(it) },
            onBack             = { currentScreen = Screen.Dashboard }
        )
        Screen.RouteCreate   -> RouteCreateScreen(
            onCreated = { currentScreen = Screen.Routes },
            onBack    = { currentScreen = Screen.Routes }
        )
        is Screen.RouteDetail -> RouteDetailScreen(
            routeId = screen.routeId,
            onBack  = { currentScreen = Screen.Routes }
        )
        Screen.Schedules     -> ScheduleScreen(
            onBack = { currentScreen = Screen.Dashboard }
        )
        Screen.Profile       -> ProfileScreen(
            onLogout = { currentScreen = Screen.Login },
            onBack   = { currentScreen = Screen.Dashboard }
        )
    }
}