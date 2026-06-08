package org.smlpartners.smlgo.ui.shared.utils

import androidx.compose.runtime.Composable

@Composable
expect fun SMLGoBackHandler(enabled: Boolean = true, onBack: () -> Unit)