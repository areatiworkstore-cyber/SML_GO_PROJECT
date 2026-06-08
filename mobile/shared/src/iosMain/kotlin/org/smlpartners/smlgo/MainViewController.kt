package org.smlpartners.smlgo

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(
    onGetLocation: (onResult: (Double, Double) -> Unit) -> Unit
): UIViewController = ComposeUIViewController {
    App(onGetLocation = onGetLocation)
}