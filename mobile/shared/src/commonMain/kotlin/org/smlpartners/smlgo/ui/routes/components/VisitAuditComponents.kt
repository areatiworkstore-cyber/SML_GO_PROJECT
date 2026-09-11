package org.smlpartners.smlgo.ui.routes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import org.smlpartners.smlgo.domain.model.Waypoint
import org.smlpartners.smlgo.ui.shared.theme.Radius
import org.smlpartners.smlgo.ui.shared.theme.Spacing
import org.smlpartners.smlgo.ui.shared.theme.Success
import org.smlpartners.smlgo.ui.shared.utils.ImageSource
import org.smlpartners.smlgo.ui.shared.utils.rememberImagePickerLauncher

@Composable
fun VisitAuditDialog(
    waypoint     : Waypoint,
    isSubmitting : Boolean,
    onConfirm    : (comment: String?, photoBytes: ByteArray?, filename: String?) -> Unit,
    onDismiss    : () -> Unit
) {
    var comment          by remember { mutableStateOf("") }
    var photoBytes       by remember { mutableStateOf<ByteArray?>(null) }
    var photoFilename    by remember { mutableStateOf<String?>(null) }
    var showSourceChoice by remember { mutableStateOf(false) }

    // Launcher de selección de imagen (Cámara o Galería)
    val imagePickerLauncher = rememberImagePickerLauncher { bytes, filename ->
        if (bytes != null && bytes.isNotEmpty()) {
            photoBytes = bytes
            photoFilename = filename
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Icon(
                    imageVector        = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint               = Success,
                    modifier           = Modifier.size(24.dp)
                )
                Text(
                    text       = "Auditoría de Visita",
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // ── Info del cliente ─────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(Radius.sm),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(Spacing.sm)) {
                        Text(
                            text       = waypoint.clientName ?: "Cliente #${waypoint.clientId}",
                            style      = MaterialTheme.typography.titleSmall,
                            color      = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Filled.LocationOn,
                                contentDescription = null,
                                modifier           = Modifier.size(14.dp),
                                tint               = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text     = waypoint.address,
                                style    = MaterialTheme.typography.bodySmall,
                                color    = Color.Black,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // ── Campo Observación ────────────────────────────────────
                OutlinedTextField(
                    value         = comment,
                    onValueChange = { if (it.length <= 250) comment = it },
                    label         = { Text("Observación / Comentario", color = Color.Black, fontWeight = FontWeight.SemiBold) },
                    placeholder   = { Text("Ej. El cliente recibió el pedido...", color = Color.Gray) },
                    shape         = RoundedCornerShape(Radius.md),
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3,
                    minLines      = 2,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedTextColor      = Color.Black,
                        unfocusedTextColor    = Color.Black,
                        focusedBorderColor    = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor  = Color.Black,
                        focusedLabelColor     = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor   = Color.Black
                    )
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text  = "${comment.length}/250",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ── Foto de Evidencia ────────────────────────────────────
                Text(
                    text       = "Foto de Evidencia",
                    style      = MaterialTheme.typography.titleSmall,
                    color      = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                if (photoBytes == null) {
                    OutlinedButton(
                        onClick  = { showSourceChoice = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(Radius.md),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.AddAPhoto,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(Spacing.xs))
                        Text(
                            text       = "Adjuntar Foto de Evidencia",
                            style      = MaterialTheme.typography.labelLarge,
                            color      = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Previsualización de la foto seleccionada
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(Radius.md),
                        colors   = CardDefaults.cardColors(
                            containerColor = Success.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier              = Modifier.padding(Spacing.sm).fillMaxWidth(),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier          = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.Image,
                                    contentDescription = null,
                                    tint               = Success,
                                    modifier           = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(Spacing.xs))
                                Column {
                                    Text(
                                        text     = photoFilename ?: "evidencia.jpg",
                                        style    = MaterialTheme.typography.labelMedium,
                                        color    = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text       = "${(photoBytes!!.size / 1024)} KB - Lista para subir",
                                        style      = MaterialTheme.typography.labelSmall,
                                        color      = Color.Black,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    photoBytes = null
                                    photoFilename = null
                                }
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.Delete,
                                    contentDescription = "Remover foto",
                                    tint               = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        comment.ifBlank { null },
                        photoBytes,
                        photoFilename
                    )
                },
                enabled = !isSubmitting,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = Success,
                    contentColor   = Color.White
                ),
                shape   = RoundedCornerShape(Radius.md)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text("Guardando...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector        = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp),
                        tint               = Color.White
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text("Confirmar Visita", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isSubmitting) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        shape          = RoundedCornerShape(Radius.lg),
        containerColor = MaterialTheme.colorScheme.surface
    )

    // ── Diálogo modal para elegir Origen de Foto (Cámara vs Galería) ───
    if (showSourceChoice) {
        AlertDialog(
            onDismissRequest = { showSourceChoice = false },
            title = {
                Text(
                    text       = "Seleccionar Origen de Imagen",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        onClick  = {
                            showSourceChoice = false
                            imagePickerLauncher.launch(ImageSource.CAMERA)
                        }
                    ) {
                        Row(
                            modifier          = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.PhotoCamera,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(Spacing.md))
                            Text(
                                text       = "Tomar Foto con la Cámara",
                                style      = MaterialTheme.typography.bodyLarge,
                                color      = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xs))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        onClick  = {
                            showSourceChoice = false
                            imagePickerLauncher.launch(ImageSource.GALLERY)
                        }
                    ) {
                        Row(
                            modifier          = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.PhotoLibrary,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(Spacing.md))
                            Text(
                                text       = "Elegir de la Galería",
                                style      = MaterialTheme.typography.bodyLarge,
                                color      = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceChoice = false }) {
                    Text("Cancelar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun FullPhotoViewerModal(
    photoUrl  : String,
    clientName: String?,
    onDismiss : () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Foto de Evidencia",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.Black)
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier            = Modifier.fillMaxWidth()
            ) {
                if (!clientName.isNullOrBlank()) {
                    Text(
                        text       = "Cliente: $clientName",
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                val cleanUrl = photoUrl.replace("\"", "").trim()
                println("URL PHOTO CLEAN: $cleanUrl")
                // Imagen remota con estados loading / error via SubcomposeAsyncImage
                SubcomposeAsyncImage(
                    model             = cleanUrl,
                    contentDescription = "Foto de evidencia",
                    contentScale      = ContentScale.Crop,
                    modifier          = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(Radius.md)),
                    loading = {
                        Box(
                            modifier         = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(32.dp),
                                    color       = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(Modifier.height(Spacing.xs))
                                Text(
                                    text       = "Cargando imagen...",
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    error = {
                        Box(
                            modifier         = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector        = Icons.Filled.BrokenImage,
                                    contentDescription = null,
                                    modifier           = Modifier.size(40.dp),
                                    tint               = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.height(Spacing.xs))
                                Text(
                                    text       = "No se pudo cargar la imagen",
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Cerrar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}
