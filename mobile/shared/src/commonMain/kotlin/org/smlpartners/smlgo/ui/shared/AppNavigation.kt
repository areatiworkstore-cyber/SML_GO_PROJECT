package org.smlpartners.smlgo.ui.shared

import androidx.compose.runtime.*
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.ui.shared.utils.SMLGoBackHandler
import org.smlpartners.smlgo.ui.clients.ClientFormScreen
import org.smlpartners.smlgo.ui.clients.ClientListScreen
import org.smlpartners.smlgo.ui.dashboard.DashboardScreen
import org.smlpartners.smlgo.ui.auth.LoginScreen
import org.smlpartners.smlgo.ui.map.MapScreen
import org.smlpartners.smlgo.ui.profile.ProfileEditScreen
import org.smlpartners.smlgo.ui.profile.ProfileScreen
import org.smlpartners.smlgo.ui.profile.ProfileViewModel
import org.smlpartners.smlgo.ui.profile.ProfileViewScreen
import org.smlpartners.smlgo.ui.routes.RouteCreateScreen
import org.smlpartners.smlgo.ui.routes.RouteDetailScreen
import org.smlpartners.smlgo.ui.routes.RouteListScreen
import org.smlpartners.smlgo.ui.schedule.ScheduleScreen

sealed class Screen {
    object Login                                  : Screen()
    object Dashboard                              : Screen()
    object Map                                    : Screen()
    object Clients                                : Screen()
    data class ClientForm(val clientId: Int?)     : Screen()
    object Routes                                 : Screen()
    object RouteCreate                            : Screen()
    data class RouteDetail(val routeId: Int)      : Screen()
    object Schedules                              : Screen()
    object Profile                                : Screen()
    object ProfileEdit                            : Screen()
    object ProfileView : Screen()
}

@Composable
fun AppNavigation(
    onGetLocation: (onResult: (Double, Double) -> Unit) -> Unit = { _ -> },
) {
    val navigationStack = remember { mutableStateListOf<Screen>(Screen.Login) }
    val currentScreen   = navigationStack.lastOrNull() ?: Screen.Login

    // Compartimos el ViewModel del perfil para ambas pantallas
    val profileViewModel: ProfileViewModel = koinViewModel()

    fun navigateTo(screen: Screen, clearStack: Boolean = false) {
        if (clearStack) navigationStack.clear()
        navigationStack.add(screen)
    }

    fun navigateBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
        }
    }

    // Intercepta el botón físico de Android
    SMLGoBackHandler(enabled = navigationStack.size > 1) {
        navigateBack()
    }

    when (val screen = currentScreen) {
        Screen.Login         -> LoginScreen(
            onLoginSuccess = {
                profileViewModel.resetProfile()
                navigateTo(Screen.Dashboard, clearStack = true)
            }
        )
        Screen.Dashboard     -> DashboardScreen(
            onNavigateToClients       = { navigateTo(Screen.Clients) },
            onNavigateToRoutes        = { navigateTo(Screen.Routes) },
            onNavigateToSchedules     = { navigateTo(Screen.Schedules) },
            onNavigateToProfile       = { navigateTo(Screen.Profile) },
            onNavigateToRouteDetail   = { navigateTo(Screen.RouteDetail(it)) },
            onNavigateToMap           = { navigateTo(Screen.Map) }
        )
        Screen.Map -> MapScreen(
            onNavigateToClientDetail = { navigateTo(Screen.ClientForm(it)) },
            onBack                   = { navigateBack() }
        )
        Screen.Clients       -> ClientListScreen(
            onNavigateToForm = { navigateTo(Screen.ClientForm(it)) },
            onBack           = { navigateBack() }
        )
        is Screen.ClientForm -> ClientFormScreen(
            clientId      = screen.clientId,
            onSaved       = { navigateBack() },
            onBack        = { navigateBack() },
            onGetLocation = onGetLocation
        )
        Screen.Routes        -> RouteListScreen(
            onNavigateToCreate = { navigateTo(Screen.RouteCreate) },
            onNavigateToDetail = { navigateTo(Screen.RouteDetail(it)) },
            onBack             = { navigateBack() }
        )
        Screen.RouteCreate   -> RouteCreateScreen(
            onCreated = { navigateBack() },
            onBack    = { navigateBack() }
        )
        is Screen.RouteDetail -> RouteDetailScreen(
            routeId = screen.routeId,
            onBack  = { navigateBack() }
        )
        Screen.Schedules     -> ScheduleScreen(
            onBack = { navigateBack() }
        )
        Screen.Profile       -> ProfileScreen(
            viewModel = profileViewModel,
            onLogout = {
                navigateTo(Screen.Login, clearStack = true)
            },
            onNavigateToEdit = { navigateTo(Screen.ProfileEdit) },
            onNavigateToView = { navigateTo(Screen.ProfileView) },
            onBack   = { navigateBack() }
        )
        Screen.ProfileEdit -> ProfileEditScreen(
            viewModel = profileViewModel,
            onBack = { navigateBack() }
        )
        Screen.ProfileView -> ProfileViewScreen(    // ← nueva
            viewModel = profileViewModel,
            onBack    = { navigateBack() }
        )
    }
}