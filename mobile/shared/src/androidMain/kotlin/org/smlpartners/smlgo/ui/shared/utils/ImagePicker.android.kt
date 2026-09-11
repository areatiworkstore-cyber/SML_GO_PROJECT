package org.smlpartners.smlgo.ui.shared.utils

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (bytes: ByteArray?, filename: String?) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current

    // Launcher para la Galería (archivos de imagen)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    val filename = "galeria_${System.currentTimeMillis()}.jpg"
                    onImagePicked(bytes, filename)
                }
            } catch (e: Exception) {
                onImagePicked(null, null)
            }
        }
    }

    // Launcher para la Cámara (captura directa de foto)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val bytes = outputStream.toByteArray()
                val filename = "camara_${System.currentTimeMillis()}.jpg"
                onImagePicked(bytes, filename)
            } catch (e: Exception) {
                onImagePicked(null, null)
            }
        }
    }

    return remember(galleryLauncher, cameraLauncher) {
        object : ImagePickerLauncher {
            override fun launch(source: ImageSource) {
                when (source) {
                    ImageSource.GALLERY -> galleryLauncher.launch("image/*")
                    ImageSource.CAMERA  -> cameraLauncher.launch(null)
                }
            }
        }
    }
}
