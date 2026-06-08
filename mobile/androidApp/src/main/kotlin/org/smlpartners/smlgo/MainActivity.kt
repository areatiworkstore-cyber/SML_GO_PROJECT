package org.smlpartners.smlgo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.smlpartners.smlgo.App
import org.smlpartners.smlgo.core.utils.LocationProvider
import org.smlpartners.smlgo.core.utils.LocationResult

class MainActivity : ComponentActivity() {

    private val locationProvider = LocationProvider()
    private var pendingLocationCallback: ((Double, Double) -> Unit)? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(
                onGetLocation = { callback ->
                    pendingLocationCallback = callback
                    requestLocationOrFetch()
                }
            )
        }
    }

    private fun requestLocationOrFetch() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fetchLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchLocation() {
        CoroutineScope(Dispatchers.Main).launch {
            when (val result = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    pendingLocationCallback?.invoke(result.latitude, result.longitude)
                    pendingLocationCallback = null
                }
                is LocationResult.Error          -> {
                    pendingLocationCallback = null
                }
                is LocationResult.PermissionDenied -> {
                    pendingLocationCallback = null
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}