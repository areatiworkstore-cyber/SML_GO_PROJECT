package org.smlpartners.smlgo.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.ui.shared.components.SMLGoButton
import org.smlpartners.smlgo.ui.shared.components.SMLGoTopBar

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack  : () -> Unit
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val uiState  by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLogout()
    }

    Scaffold(
        topBar = { SMLGoTopBar(title = "Perfil", onBack = onBack) }
    ) { padding ->
        Column(
            modifier            = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Avatar ────────────────────────────────────────────────
            Surface(
                modifier = Modifier.size(96.dp).clip(CircleShape),
                color    = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Filled.Person,
                        contentDescription = null,
                        modifier           = Modifier.size(48.dp),
                        tint               = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Datos ─────────────────────────────────────────────────
            Text(
                text  = uiState.userName ?: "Usuario",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text  = uiState.userEmail ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(Modifier.weight(1f))

            // ── Cerrar sesión ─────────────────────────────────────────
            SMLGoButton(
                text    = "Cerrar sesión",
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}