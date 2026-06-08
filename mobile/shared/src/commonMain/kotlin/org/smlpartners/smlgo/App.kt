package org.smlpartners.smlgo

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import org.smlpartners.smlgo.ui.shared.AppNavigation
import org.smlpartners.smlgo.ui.shared.theme.SMLGoTheme
import org.smlpartners.smlgo.ui.shared.GlobalErrorWrapper

@Composable
@Preview
fun App(
    onGetLocation: (onResult: (Double, Double) -> Unit) -> Unit = { _ -> }
) {
    SMLGoTheme {
        GlobalErrorWrapper(
            onSessionExpired = {
                // TODO: Manejar redirección global si es necesario
            }
        ) {
            AppNavigation(
                onGetLocation  = onGetLocation
            )
        }
    }
}