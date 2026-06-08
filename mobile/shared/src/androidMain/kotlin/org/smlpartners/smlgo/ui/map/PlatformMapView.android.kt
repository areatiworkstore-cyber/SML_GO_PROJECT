package org.smlpartners.smlgo.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import org.smlpartners.smlgo.domain.model.MapMarker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

// Coordenadas centradas en Perú por defecto
private val PERU_CENTER = LatLng(-9.19, -75.0152)

@Composable
actual fun PlatformMapView(
    markers          : List<MapMarker>,
    onMarkerSelected : (MapMarker) -> Unit,
    modifier         : Modifier
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(PERU_CENTER, 6f)
    }

    LaunchedEffect(markers) {
        if (markers.isEmpty()) return@LaunchedEffect
        if (markers.size == 1) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(markers.first().latitude, markers.first().longitude),
                    15f
                )
            )
            return@LaunchedEffect
        }
        val bounds = LatLngBounds.builder().apply {
            markers.forEach { include(LatLng(it.latitude, it.longitude)) }
        }.build()
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngBounds(bounds, 120)
        )
    }

    GoogleMap(
        modifier            = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings          = MapUiSettings(
            zoomControlsEnabled     = true,
            myLocationButtonEnabled = hasLocationPermission
        ),
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
    ) {
        markers.forEach { marker ->
            // ← key() garantiza que el MarkerState se recuerde por marker.id
            key(marker.id) {
                val markerState = rememberUpdatedMarkerState(
                    position = LatLng(marker.latitude, marker.longitude)
                )
                Marker(
                    state   = markerState,
                    title   = marker.title,
                    snippet = marker.snippet,
                    icon    = BitmapDescriptorFactory.defaultMarker(
                        if (marker.isSelected)
                            BitmapDescriptorFactory.HUE_ORANGE
                        else
                            BitmapDescriptorFactory.HUE_RED
                    ),
                    onClick = {
                        onMarkerSelected(marker)
                        false
                    }
                )
            }
        }
    }
}