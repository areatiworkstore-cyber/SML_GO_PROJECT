package org.smlpartners.smlgo.ui.shared.utils

import androidx.compose.runtime.Composable

@Composable
actual fun SMLGoBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS doesn't have a hardware back button. 
    // Back gestures are usually handled by the system or a navigation controller.
}