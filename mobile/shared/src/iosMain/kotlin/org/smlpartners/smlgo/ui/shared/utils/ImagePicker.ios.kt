package org.smlpartners.smlgo.ui.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.getBytes
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.*
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return byteArrayOf()
    val byteArray = ByteArray(size)
    byteArray.usePinned { pinned ->
        getBytes(pinned.addressOf(0), length)
    }
    return byteArray
}

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (bytes: ByteArray?, filename: String?) -> Unit
): ImagePickerLauncher {
    return remember(onImagePicked) {
        object : ImagePickerLauncher {
            override fun launch(source: ImageSource) {
                val picker = UIImagePickerController()
                val sourceType = when (source) {
                    ImageSource.CAMERA  -> UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                    ImageSource.GALLERY -> UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                }

                if (UIImagePickerController.isSourceTypeAvailable(sourceType)) {
                    picker.sourceType = sourceType
                    val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
                        override fun imagePickerController(
                            picker: UIImagePickerController,
                            didFinishPickingMediaWithInfo: Map<Any?, *>
                        ) {
                            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                            if (image != null) {
                                val nsData = UIImageJPEGRepresentation(image, 0.85)
                                if (nsData != null) {
                                    val bytes = nsData.toByteArray()
                                    val prefix = if (source == ImageSource.CAMERA) "camara" else "galeria"
                                    val filename = "${prefix}_${NSDate().timeIntervalSince1970.toLong()}.jpg"
                                    onImagePicked(bytes, filename)
                                } else {
                                    onImagePicked(null, null)
                                }
                            } else {
                                onImagePicked(null, null)
                            }
                            picker.dismissViewControllerAnimated(true, completion = null)
                        }

                        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                            picker.dismissViewControllerAnimated(true, completion = null)
                        }
                    }
                    picker.delegate = delegate

                    val keyWindow = UIApplication.sharedApplication.windows.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
                    val rootController = keyWindow?.rootViewController ?: UIApplication.sharedApplication.keyWindow?.rootViewController
                    rootController?.presentViewController(picker, animated = true, completion = null)
                } else {
                    if (source == ImageSource.CAMERA && UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)) {
                        launch(ImageSource.GALLERY)
                    } else {
                        onImagePicked(null, null)
                    }
                }
            }
        }
    }
}
