package org.smlpartners.smlgo.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.smlpartners.smlgo.ui.shared.components.SMLGoTopBar
import org.smlpartners.smlgo.ui.shared.theme.*

@Composable
fun ProfileScreen(
    viewModel       : ProfileViewModel,
    onLogout        : () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToView: () -> Unit,
    onBack          : () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // ← recarga el resumen al entrar siempre
    LaunchedEffect(Unit) {
        viewModel.loadProfileSummary()
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLogout()
    }

    Scaffold(
        topBar = { SMLGoTopBar(title = "Perfil", onBack = onBack) }
    ) { padding ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header con gradiente ──────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Primary.copy(alpha = 0.08f))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar
                    Box(
                        modifier         = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = uiState.userName
                            ?.firstOrNull()
                            ?.uppercaseChar()
                            ?.toString() ?: "U"
                        Text(
                            text       = initial,
                            fontSize   = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Primary
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text       = uiState.userName ?: "Usuario",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary
                    )

                    Spacer(Modifier.height(4.dp))

                    if (!uiState.userCode.isNullOrBlank()) {
                        Text(
                            text  = uiState.userCode ?: "Error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    if (!uiState.userCode.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(Radius.full),
                            color = Primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text     = "Código: ${uiState.userCode}",
                                style    = MaterialTheme.typography.labelMedium,
                                color    = Primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Opciones ──────────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileOptionCard(
                    icon    = Icons.Filled.Person,
                    title   = "Ver mis datos",
                    subtitle = "Consulta tu información personal",
                    color   = Primary,
                    onClick = onNavigateToView
                )

                ProfileOptionCard(
                    icon     = Icons.Filled.Edit,
                    title    = "Editar datos",
                    subtitle = "Actualiza tu información y contraseña",
                    color    = Success,
                    onClick  = onNavigateToEdit
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Cerrar sesión ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Button(
                    onClick        = viewModel::logout,
                    modifier       = Modifier.fillMaxWidth().height(52.dp),
                    shape          = RoundedCornerShape(Radius.md),
                    colors         = ButtonDefaults.buttonColors(
                        containerColor = Error,
                        contentColor   = Surface
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Logout,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "Cerrar sesión",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileOptionCard(
    icon    : ImageVector,
    title   : String,
    subtitle: String,
    color   : Color,
    onClick : () -> Unit
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
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary
                )
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector        = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint               = TextMuted,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}