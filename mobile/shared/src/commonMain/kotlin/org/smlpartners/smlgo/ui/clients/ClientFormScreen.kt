package org.smlpartners.smlgo.ui.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.domain.model.*
import org.smlpartners.smlgo.ui.shared.components.*

@Composable
fun ClientFormScreen(
    clientId        : Int?,
    onSaved         : () -> Unit,
    onBack          : () -> Unit,
    onGetLocation   : (onResult: (Double, Double) -> Unit) -> Unit
) {
    val viewModel: ClientViewModel = koinViewModel()
    val formState by viewModel.formState.collectAsState()

    // Campos del formulario
    var name           by remember { mutableStateOf("") }
    var address        by remember { mutableStateOf("") }
    var cellphone      by remember { mutableStateOf("") }
    var telephone      by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }
    var observation    by remember { mutableStateOf("") }

    var selectedDocumentType by remember { mutableStateOf<DocumentType?>(null) }
    var selectedBusinessType by remember { mutableStateOf<BusinessType?>(null) }
    var selectedClientGroup  by remember { mutableStateOf<ClientGroup?>(null) }
    var selectedSupplier     by remember { mutableStateOf<Supplier?>(null) }

    // Carga los datos del formulario
    LaunchedEffect(clientId) { viewModel.loadFormData(clientId) }

    // Pre-rellena si es edición
    LaunchedEffect(formState.client) {
        formState.client?.let { c ->
            name           = c.name           ?: ""
            address        = c.address        ?: ""
            cellphone      = c.cellphone      ?: ""
            telephone      = c.telephone      ?: ""
            documentNumber = c.documentNumber ?: ""
            observation    = c.observation    ?: ""
            selectedDocumentType = c.documentType
            selectedBusinessType = c.businessType
            selectedClientGroup  = c.clientGroup
            selectedSupplier     = c.supplier
        }
    }

    // Navega al guardar
    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) {
            viewModel.clearSaved()
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            SMLGoTopBar(
                title  = if (clientId == null) "Nuevo cliente" else "Editar cliente",
                onBack = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Datos básicos ─────────────────────────────────────
                SMLGoTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = "Nombre *"
                )
                SMLGoTextField(
                    value         = documentNumber,
                    onValueChange = { documentNumber = it },
                    label         = "Número de documento",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                SMLGoTextField(
                    value         = address,
                    onValueChange = { address = it },
                    label         = "Dirección"
                )
                SMLGoTextField(
                    value         = cellphone,
                    onValueChange = { cellphone = it },
                    label         = "Celular",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                SMLGoTextField(
                    value         = observation,
                    onValueChange = { observation = it },
                    label         = "Observación"
                )

                // ── Dropdowns catálogos ───────────────────────────────
                if (formState.documentTypes.isNotEmpty()) {
                    SMLGoDropdown(
                        label    = "Tipo de documento",
                        options  = formState.documentTypes,
                        selected = selectedDocumentType,
                        onSelect = { selectedDocumentType = it },
                        display  = { it.description }
                    )
                }
                if (formState.businessTypes.isNotEmpty()) {
                    SMLGoDropdown(
                        label    = "Tipo de negocio",
                        options  = formState.businessTypes,
                        selected = selectedBusinessType,
                        onSelect = { selectedBusinessType = it },
                        display  = { it.description }
                    )
                }
                if (formState.clientGroups.isNotEmpty()) {
                    SMLGoDropdown(
                        label    = "Grupo de cliente",
                        options  = formState.clientGroups,
                        selected = selectedClientGroup,
                        onSelect = { selectedClientGroup = it },
                        display  = { it.description }
                    )
                }
                if (formState.suppliers.isNotEmpty()) {
                    SMLGoDropdown(
                        label    = "Proveedor",
                        options  = formState.suppliers,
                        selected = selectedSupplier,
                        onSelect = { selectedSupplier = it },
                        display  = { it.names }
                    )
                }

                // ── Ubicación GPS ─────────────────────────────────────
                HorizontalDivider()
                Text(
                    text  = "Ubicación GPS",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value         = formState.client?.latitude?.toString() ?: "",
                        onValueChange = {},
                        label         = { Text("Latitud") },
                        enabled       = false,
                        modifier      = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value         = formState.client?.longitude?.toString() ?: "",
                        onValueChange = {},
                        label         = { Text("Longitud") },
                        enabled       = false,
                        modifier      = Modifier.weight(1f)
                    )
                }
                OutlinedButton(
                    onClick  = {
                        onGetLocation { lat, lng ->
                            viewModel.updateLocation(lat, lng)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Obtener ubicación actual")
                }

                // ── Guardar ───────────────────────────────────────────
                Spacer(Modifier.height(8.dp))
                SMLGoButton(
                    text      = "Guardar",
                    onClick   = {
                        viewModel.saveClient(
                            Client(
                                id             = clientId ?: 0,
                                code           = formState.client?.code,
                                name           = name,
                                documentType   = selectedDocumentType,
                                documentNumber = documentNumber.ifBlank { null },
                                address        = address.ifBlank { null },
                                district       = formState.client?.district,
                                businessType   = selectedBusinessType,
                                clientGroup    = selectedClientGroup,
                                cellphone      = cellphone.ifBlank { null },
                                telephone      = telephone.ifBlank { null },
                                active         = true,
                                latitude       = formState.client?.latitude,
                                longitude      = formState.client?.longitude,
                                observation    = observation.ifBlank { null },
                                supplier       = selectedSupplier
                            )
                        )
                    },
                    isLoading = formState.isLoading
                )
            }

            ErrorSnackbar(
                message   = formState.error,
                onDismiss = viewModel::clearError
            )
        }
    }
}

// ── Dropdown genérico ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SMLGoDropdown(
    label    : String,
    options  : List<T>,
    selected : T?,
    onSelect : (T) -> Unit,
    display  : (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value            = selected?.let { display(it) } ?: "",
            onValueChange    = {},
            readOnly         = true,
            label            = { Text(label) },
            trailingIcon     = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier         = Modifier.menuAnchor(
                ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                enabled = true
            ).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text    = { Text(display(option)) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}