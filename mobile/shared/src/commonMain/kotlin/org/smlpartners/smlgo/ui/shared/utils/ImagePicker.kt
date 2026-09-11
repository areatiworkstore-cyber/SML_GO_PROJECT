package org.smlpartners.smlgo.ui.shared.utils

import androidx.compose.runtime.Composable

enum class ImageSource {
    CAMERA,
    GALLERY
}

interface ImagePickerLauncher {
    fun launch(source: ImageSource)
}

@Composable
expect fun rememberImagePickerLauncher(
    onImagePicked: (bytes: ByteArray?, filename: String?) -> Unit
): ImagePickerLauncher
