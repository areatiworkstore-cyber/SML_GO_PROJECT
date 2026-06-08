package org.smlpartners.smlgo.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import smlgo.shared.generated.resources.*
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.ui.shared.components.ErrorSnackbar
import org.smlpartners.smlgo.ui.shared.components.SMLGoButton
import org.smlpartners.smlgo.ui.shared.components.SMLGoTextField

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
) {
    val viewModel : LoginViewModel = koinViewModel()
    val uiState   by viewModel.uiState.collectAsState()

    var username        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(value = false) }

    // Navega cuando el login es exitoso
    LaunchedEffect(uiState.isLoggedIn) {
        println("[LoginScreen] isLoggedIn: ${uiState.isLoggedIn}")
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Logo / Título ─────────────────────────────────────────
            Spacer(Modifier.height(48.dp))
            Image(
                painter = painterResource(Res.drawable.smlgo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text  = "Gestión de rutas y visitas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(48.dp))

            // ── Campos ────────────────────────────────────────────────
            SMLGoTextField(
                value         = username,
                onValueChange = { username = it },
                label         = "Correo",
                error         = uiState.usernameError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                )
            )
            Spacer(Modifier.height(16.dp))
            SMLGoTextField(
                value         = password,
                onValueChange = { password = it },
                label         = "Contraseña",
                error         = uiState.passwordError,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Ocultar contraseña"
                            else
                                "Mostrar contraseña"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                )
            )
            Spacer(Modifier.height(32.dp))

            // ── Botón ─────────────────────────────────────────────────
            SMLGoButton(
                text      = "Iniciar sesión",
                onClick   = { viewModel.login(username, password) },
                isLoading = uiState.isLoading
            )
            Spacer(Modifier.height(48.dp))
        }

        // ── Snackbar de error ─────────────────────────────────────────
        Box(
            modifier        = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            ErrorSnackbar(
                message   = uiState.error,
                onDismiss = viewModel::clearError
            )
        }
    }
}