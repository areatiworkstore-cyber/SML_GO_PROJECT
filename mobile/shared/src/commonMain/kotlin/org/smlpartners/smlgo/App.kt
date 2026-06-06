package org.smlpartners.smlgo

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import org.smlpartners.smlgo.ui.shared.AppNavigation
import org.smlpartners.smlgo.ui.shared.theme.SMLGoTheme

@Composable
@Preview
fun App() {
    SMLGoTheme {
        AppNavigation()
    }
}