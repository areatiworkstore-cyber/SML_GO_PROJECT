package org.smlpartners.smlgo.ui.shared.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.smlpartners.smlgo.core.error.AppError
import org.smlpartners.smlgo.ui.shared.theme.Radius
import org.smlpartners.smlgo.ui.shared.theme.Spacing


// ── Pantalla de error completa ────────────────────────────────────────────

@Composable
fun ErrorScreen(
    error    : AppError,
    onRetry  : (() -> Unit)? = null,
    onLogout : (() -> Unit)? = null,
    modifier : Modifier      = Modifier
) {
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl)
        ) {
            // ── Ícono ─────────────────────────────────────────────────
            Surface(
                shape  = RoundedCornerShape(Radius.full),
                color  = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = error.toIcon().toImageVector(),
                        contentDescription = null,
                        modifier           = Modifier.size(44.dp),
                        tint               = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            // ── Título ────────────────────────────────────────────────
            Text(
                text      = error.toTitle(),
                style     = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(Spacing.sm))

            // ── Mensaje ───────────────────────────────────────────────
            Text(
                text      = error.toUserMessage(),
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(Spacing.xl))

            // ── Botón reintentar ──────────────────────────────────────
            if (error.isRetryable() && onRetry != null) {
                Button(
                    onClick  = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(Radius.md)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text("Reintentar")
                }
                Spacer(Modifier.height(Spacing.sm))
            }

            // ── Botón cerrar sesión ───────────────────────────────────
            if (error.requiresLogout() && onLogout != null) {
                OutlinedButton(
                    onClick  = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(Radius.md)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Logout,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

// ── Toast de error global (overlay no bloqueante) ─────────────────────────

@Composable
fun GlobalErrorToast(
    error    : AppError?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = error != null,
        enter   = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit    = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        error?.let {
            Snackbar(
                modifier = Modifier.padding(Spacing.md),
                action   = {
                    TextButton(onClick = onDismiss) {
                        Text("OK", color = MaterialTheme.colorScheme.inversePrimary)
                    }
                },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor   = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = it.toIcon().toImageVector(),
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text  = it.toUserMessage(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ── Error inline para listas vacías con error ─────────────────────────────

@Composable
fun InlineError(
    error   : AppError,
    onRetry : (() -> Unit)? = null,
    modifier: Modifier      = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(Radius.md),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier          = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = error.toIcon().toImageVector(),
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.error,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text     = error.toUserMessage(),
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            if (error.isRetryable() && onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text("Reintentar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// ── Helpers de extensión ──────────────────────────────────────────────────

private fun AppError.toTitle(): String = when (this) {
    is AppError.NoInternet        -> "Sin conexión"
    is AppError.Timeout           -> "Tiempo de espera agotado"
    is AppError.ServerUnavailable -> "Servidor no disponible"
    is AppError.Unauthorized      -> "Acceso denegado"
    is AppError.SessionExpired    -> "Sesión expirada"
    is AppError.NotFound          -> "No encontrado"
    is AppError.Forbidden         -> "Sin permisos"
    is AppError.ServerError       -> "Error del servidor"
    is AppError.ValidationError   -> "Datos inválidos"
    is AppError.SerializationError-> "Error de datos"
    is AppError.Unknown           -> "Error inesperado"
}

private fun AppError.ErrorIcon.toImageVector(): ImageVector = when (this) {
    AppError.ErrorIcon.NO_INTERNET  -> Icons.Filled.WifiOff
    AppError.ErrorIcon.TIMEOUT      -> Icons.Filled.HourglassEmpty
    AppError.ErrorIcon.SERVER       -> Icons.Filled.CloudOff
    AppError.ErrorIcon.AUTH         -> Icons.Filled.Lock
    AppError.ErrorIcon.NOT_FOUND    -> Icons.Filled.SearchOff
    AppError.ErrorIcon.FORBIDDEN    -> Icons.Filled.Block
    AppError.ErrorIcon.VALIDATION   -> Icons.Filled.Warning
    AppError.ErrorIcon.UNKNOWN      -> Icons.Filled.ErrorOutline
}