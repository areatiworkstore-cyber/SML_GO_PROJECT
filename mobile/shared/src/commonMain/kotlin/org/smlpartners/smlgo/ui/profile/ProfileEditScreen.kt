package org.smlpartners.smlgo.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.smlpartners.smlgo.domain.model.DocumentType
import org.smlpartners.smlgo.domain.model.Role
import org.smlpartners.smlgo.ui.clients.SMLGoDropdown
import org.smlpartners.smlgo.ui.shared.components.*
import org.smlpartners.smlgo.ui.shared.theme.*

@Composable
fun ProfileEditScreen(
    viewModel: ProfileViewModel,
    onBack   : () -> Unit
) {
    val uiState     by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { viewModel.loadFormData() }

    LaunchedEffect(uiState.isUpdated) {
        if (uiState.isUpdated) {
            viewModel.clearStatus()
            onBack()
        }
    }

    var firstName      by remember { mutableStateOf("") }
    var secondName     by remember { mutableStateOf("") }
    var firstSurname   by remember { mutableStateOf("") }
    var secondSurname  by remember { mutableStateOf("") }
    var cellphone: String? by remember { mutableStateOf("") }
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }
    var selectedDocType by remember { mutableStateOf<DocumentType?>(null) }
    var selectedRoles  by remember { mutableStateOf<List<Role>>(emptyList()) }

    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            firstName      = user.firstName
            secondName     = user.secondName
            firstSurname   = user.firstSurname
            secondSurname  = user.secondSurname
            cellphone      = user.cellphone
            email          = user.email
            documentNumber = user.documentNumber
            selectedDocType = user.documentType
            selectedRoles  = user.roles
        }
    }

    Scaffold(
        topBar = { SMLGoTopBar(title = "Editar datos", onBack = onBack) }
    ) { padding ->
        if (uiState.isLoading && uiState.user == null) {
            LoadingOverlay()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Sección identidad ─────────────────────────────────────
            EditSectionTitle(
                icon  = Icons.Filled.Badge,
                title = "Identidad"
            )

            SMLGoTextField(
                value         = uiState.user?.code ?: "",
                onValueChange = {},
                label         = "Código de usuario",
                enabled       = false
            )

            if (uiState.documentTypes.isNotEmpty()) {
                SMLGoDropdown(
                    label    = "Tipo de documento",
                    options  = uiState.documentTypes,
                    selected = selectedDocType,
                    onSelect = { selectedDocType = it },
                    display  = { it.description }
                )
            }

            SMLGoTextField(
                value           = documentNumber,
                onValueChange   = { documentNumber = it },
                label           = "Número de documento",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction    = ImeAction.Next
                )
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // ── Sección nombre ────────────────────────────────────────
            EditSectionTitle(
                icon  = Icons.Filled.Person,
                title = "Nombre completo"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SMLGoTextField(
                    value           = firstName,
                    onValueChange   = { firstName = it },
                    label           = "Primer nombre",
                    modifier        = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                SMLGoTextField(
                    value           = secondName,
                    onValueChange   = { secondName = it },
                    label           = "Segundo nombre",
                    modifier        = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SMLGoTextField(
                    value           = firstSurname,
                    onValueChange   = { firstSurname = it },
                    label           = "Primer apellido",
                    modifier        = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                SMLGoTextField(
                    value           = secondSurname,
                    onValueChange   = { secondSurname = it },
                    label           = "Segundo apellido",
                    modifier        = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // ── Sección contacto ──────────────────────────────────────
            EditSectionTitle(
                icon  = Icons.Filled.ContactPhone,
                title = "Contacto"
            )

            SMLGoTextField(
                value           = cellphone ?: "No registrado",
                onValueChange   = { cellphone = it },
                label           = "Celular",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction    = ImeAction.Next
                )
            )

            SMLGoTextField(
                value           = email,
                onValueChange   = { email = it },
                label           = "Correo electrónico",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                )
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // ── Sección seguridad ─────────────────────────────────────
            EditSectionTitle(
                icon  = Icons.Filled.Lock,
                title = "Seguridad"
            )

            SMLGoTextField(
                value                = password,
                onValueChange        = { password = it },
                label                = "Nueva contraseña (opcional)",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                )
            )

            // ── Roles (solo lectura) ──────────────────────────────────
            if (selectedRoles.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                EditSectionTitle(
                    icon  = Icons.Filled.AdminPanelSettings,
                    title = "Roles asignados"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedRoles.forEach { role ->
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

            Spacer(Modifier.height(8.dp))

            // ── Botón guardar ─────────────────────────────────────────
            SMLGoButton(
                text      = "Guardar cambios",
                onClick   = {
                    uiState.user?.let { user ->
                        viewModel.updateProfile(
                            user.copy(
                                firstName      = firstName,
                                secondName     = secondName,
                                firstSurname   = firstSurname,
                                secondSurname  = secondSurname,
                                documentType   = selectedDocType,
                                documentNumber = documentNumber,
                                cellphone      = cellphone,
                                email          = email
                            )
                        )
                    }
                },
                isLoading = uiState.isLoading
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditSectionTitle(icon: ImageVector, title: String) {
    Row(
        verticalAlignment     = androidx.compose.ui.Alignment.CenterVertically,
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