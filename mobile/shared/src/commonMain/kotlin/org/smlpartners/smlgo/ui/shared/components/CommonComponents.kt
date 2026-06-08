package org.smlpartners.smlgo.ui.shared.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import org.jetbrains.compose.resources.painterResource
import smlgo.shared.generated.resources.Res
import smlgo.shared.generated.resources.smlgo

@Composable
fun SMLGoButton(
    text      : String,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier,
    enabled   : Boolean  = true,
    isLoading : Boolean  = false
) {
    Button(
        onClick  = onClick,
        enabled  = enabled && !isLoading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color    = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SMLGoTextField(
    value        : String,
    onValueChange: (String) -> Unit,
    label        : String,
    modifier     : Modifier        = Modifier,
    error        : String?         = null,
    enabled      : Boolean         = true,
    trailingIcon : @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value            = value,
            onValueChange    = onValueChange,
            label            = { Text(label) },
            isError          = error != null,
            enabled          = enabled,
            trailingIcon     = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions  = keyboardOptions,
            shape            = RoundedCornerShape(12.dp),
            modifier         = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(
                text  = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ErrorSnackbar(
    message  : String?,
    onDismiss: () -> Unit
) {
    if (message == null) return
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action   = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    ) {
        Text(message)
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier        = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SMLGoTopBar(
    title    : String,
    onBack   : (() -> Unit)? = null,
    actions  : @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector   = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            } else {
                Image(
                    painter            = painterResource(Res.drawable.smlgo),
                    contentDescription = "Logo SML Go",
                    modifier           = Modifier
                        .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                        .height(36.dp)       // ← altura fija que encaja en el TopBar
                        .aspectRatio(1.5f),  // ← mantiene proporción del logo
                    contentScale = ContentScale.Fit
                )
            }
        },
        actions = actions
    )
}