package org.smlpartners.smlgo.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.smlpartners.smlgo.domain.model.*
import org.smlpartners.smlgo.ui.shared.components.ErrorSnackbar
import org.smlpartners.smlgo.ui.shared.components.SMLGoButton
import org.smlpartners.smlgo.ui.shared.components.SMLGoTextField
import org.smlpartners.smlgo.ui.shared.components.SMLGoTopBar
import org.smlpartners.smlgo.ui.shared.theme.Radius
import org.smlpartners.smlgo.ui.shared.theme.Spacing

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

    // Campos geográficos
    var selectedDept by remember { mutableStateOf<Department?>(null) }
    var selectedProv by remember { mutableStateOf<Province?>(null) }
    var selectedDist by remember { mutableStateOf<District?>(null) }

    // Carga los datos del formulario
    LaunchedEffect(Unit) {
        viewModel.resetForm()
        viewModel.loadFormData(clientId)
    }

    // Pre-rellena si es edición
    LaunchedEffect(formState.isLoading) {
        if (!formState.isLoading) {
            val c = formState.client
            println("[ClientForm] Pre-llenando formulario. client=${c?.name} district=${c?.district?.id}")

            name                 = c?.name           ?: ""
            address              = c?.address        ?: ""
            cellphone            = c?.cellphone      ?: ""
            telephone            = c?.telephone      ?: ""
            documentNumber       = c?.documentNumber ?: ""
            observation          = c?.observation    ?: ""
            selectedDocumentType = c?.documentType
            selectedBusinessType = c?.businessType
            selectedClientGroup  = c?.clientGroup

            // ← Ubigeo pre-seleccionado desde el cliente
            selectedDept = c?.department
                ?: formState.departments.firstOrNull { dept ->
                    dept.id == c?.province?.departmentId
                }
            selectedProv = c?.province
                ?: formState.provinces.firstOrNull { it.id == c?.district?.provinceId }
            selectedDist = formState.districts.firstOrNull { it.id == c?.district?.id }
                ?: c?.district

            println("[ClientForm] Ubigeo: dept=${selectedDept?.name} prov=${selectedProv?.name} dist=${selectedDist?.name}")
        }
    }

    // Resolver geografía en edición si las listas se cargan
    LaunchedEffect(formState.provinces) {
        val c = formState.client
        if (c?.district != null && selectedProv == null) {
            selectedProv = formState.provinces.firstOrNull { it.id == c.district.provinceId }
        }
    }

    LaunchedEffect(selectedProv, formState.departments) {
        if (selectedProv != null && selectedDept == null) {
            selectedDept = formState.departments.firstOrNull { it.id == selectedProv?.departmentId }
        }
    }

    LaunchedEffect(formState.districts) {
        val c = formState.client
        if (c?.district != null && selectedDist == null) {
            selectedDist = formState.districts.firstOrNull { it.id == c.district.id }
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
        if (formState.isLoading) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Box(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Código solo lectura ───────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = androidx.compose.foundation.shape.RoundedCornerShape(Radius.md),
                    color    = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Filled.Tag,
                                contentDescription = null,
                                modifier           = Modifier.size(18.dp),
                                tint               = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                text  = "Código",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text       = formState.clientCode?.nextCode ?: "Generando...",
                            style      = MaterialTheme.typography.titleMedium,
                            color      = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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

                // ── Ubicación Geográfica ───────────────────────────────
                if (formState.departments.isNotEmpty()) {
                    SMLGoDropdown(
                        label    = "Departamento *",
                        options  = formState.departments,
                        selected = selectedDept,
                        onSelect = { dept ->
                            selectedDept = dept
                            selectedProv = null
                            selectedDist = null
                            viewModel.loadProvinces(dept.id)
                        },
                        display  = { it.name }
                    )
                }
                if (formState.provinces.isNotEmpty() || selectedProv != null) {
                    SMLGoDropdown(
                        label    = "Provincia *",
                        options  = formState.provinces,
                        selected = selectedProv,
                        onSelect = { prov ->
                            selectedProv = prov
                            selectedDist = null
                            viewModel.loadDistricts(prov.id)
                        },
                        display  = { it.name }
                    )
                }
                if (formState.districts.isNotEmpty() || selectedDist != null) {
                    SMLGoDropdown(
                        label    = "Distrito *",
                        options  = formState.districts,
                        selected = selectedDist,
                        onSelect = { dist ->
                            selectedDist = dist
                        },
                        display  = { it.name }
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
                        viewModel.startLocating()
                        onGetLocation { lat, lng ->
                            viewModel.updateLocation(lat, lng)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !formState.isLocating
                ) {
                    if (formState.isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(Icons.Filled.MyLocation, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (formState.isLocating) "Obteniendo ubicación..." else "Obtener ubicación actual",
                        color = Color.Black
                    )
                }

                // ── Guardar ───────────────────────────────────────────
                Spacer(Modifier.height(8.dp))
                SMLGoButton(
                    text      = "Guardar",
                    onClick   = {
                        viewModel.saveClient(
                            Client(
                                id             = clientId ?: 0,
                                code           = formState.clientCode?.nextCode,
                                name           = name,
                                documentType   = selectedDocumentType,
                                documentNumber = documentNumber.ifBlank { null },
                                address        = address.ifBlank { null },
                                district       = selectedDist,
                                businessType   = selectedBusinessType,
                                clientGroup    = selectedClientGroup,
                                cellphone      = cellphone.ifBlank { null },
                                telephone      = telephone.ifBlank { null },
                                active         = true,
                                latitude       = formState.client?.latitude,
                                longitude      = formState.client?.longitude,
                                observation    = observation.ifBlank { null },
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