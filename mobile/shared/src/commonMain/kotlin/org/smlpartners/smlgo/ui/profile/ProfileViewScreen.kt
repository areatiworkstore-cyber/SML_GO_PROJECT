package org.smlpartners.smlgo.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.smlpartners.smlgo.ui.shared.components.LoadingOverlay
import org.smlpartners.smlgo.ui.shared.components.SMLGoTopBar
import org.smlpartners.smlgo.ui.shared.theme.Primary
import org.smlpartners.smlgo.ui.shared.theme.Radius
import org.smlpartners.smlgo.ui.shared.theme.TextPrimary
import org.smlpartners.smlgo.ui.shared.theme.TextSecondary

@Composable
fun ProfileViewScreen(
    viewModel: ProfileViewModel,
    onBack   : () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFormData()
    }

    Scaffold(
        topBar = { SMLGoTopBar(title = "Mis datos", onBack = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.user == null) {
                LoadingOverlay()
            } else {
                val user = uiState.user
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Sección identidad ─────────────────────────────
                    ProfileSectionTitle(
                        icon  = Icons.Filled.Badge,
                        title = "Identidad"
                    )
                    ProfileInfoCard {
                        ProfileInfoRow(
                            label = "Código",
                            value = user?.code ?: "-"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            label = "Tipo de documento",
                            value = user?.documentType?.description ?: "-"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            label = "Número de documento",
                            value = user?.documentNumber ?: "-"
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // ── Sección nombre ────────────────────────────────
                    ProfileSectionTitle(
                        icon  = Icons.Filled.Person,
                        title = "Nombre completo"
                    )
                    ProfileInfoCard {
                        ProfileInfoRow(
                            label = "Primer nombre",
                            value = user?.firstName ?: "-"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            label = "Segundo nombre",
                            value = user?.secondName ?: "-"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            label = "Primer apellido",
                            value = user?.firstSurname ?: "-"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            label = "Segundo apellido",
                            value = user?.secondSurname ?: "-"
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // ── Sección contacto ──────────────────────────────
                    ProfileSectionTitle(
                        icon  = Icons.Filled.ContactPhone,
                        title = "Contacto"
                    )
                    ProfileInfoCard {
                        ProfileInfoRow(
                            label = "Celular",
                            value = user?.cellphone ?: "-"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            label = "Correo",
                            value = user?.email ?: "-"
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // ── Sección roles ─────────────────────────────────
                    if (!user?.roles.isNullOrEmpty()) {
                        ProfileSectionTitle(
                            icon  = Icons.Filled.AdminPanelSettings,
                            title = "Roles asignados"
                        )
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            user?.roles?.forEach { role ->
                                Surface(
                                    shape = RoundedCornerShape(Radius.full),
                                    color = Primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text     = role.role,
                                        style    = MaterialTheme.typography.labelMedium,
                                        color    = Primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical   = 6.dp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionTitle(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = Primary,
            modifier           = Modifier.size(18.dp)
        )
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color      = Primary
        )
    }
}

@Composable
private fun ProfileInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(Radius.md),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content  = content
        )
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color      = TextPrimary,
            modifier   = Modifier.weight(0.55f)
        )
    }
}