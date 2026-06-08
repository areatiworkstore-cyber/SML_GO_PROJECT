package org.smlpartners.smlgo.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.smlpartners.smlgo.core.error.AppError
import org.smlpartners.smlgo.core.error.GlobalErrorHandler
import org.smlpartners.smlgo.ui.shared.components.GlobalErrorToast

@Composable
fun GlobalErrorWrapper(
    onSessionExpired : () -> Unit,
    content          : @Composable () -> Unit
) {
    var currentError by remember { mutableStateOf<AppError?>(null) }

    // Escucha el canal global de errores
    LaunchedEffect(Unit) {
        GlobalErrorHandler.errors.collect { error ->
            // Si requiere logout lo maneja automáticamente
            if (error.requiresLogout()) {
                onSessionExpired()
            } else {
                currentError = error
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        // Toast global en la parte inferior
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            GlobalErrorToast(
                error     = currentError,
                onDismiss = { currentError = null }
            )
        }
    }
}