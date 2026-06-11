package org.smlpartners.smlgo.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import org.smlpartners.smlgo.domain.model.MapMarker
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKAnnotationView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformMapView(
    markers          : List<MapMarker>,
    onMarkerSelected : (MapMarker) -> Unit,
    modifier         : Modifier
) {
    val locationManager = remember { platform.CoreLocation.CLLocationManager() }
    val delegate = remember(markers) {
        object : NSObject(), MKMapViewDelegateProtocol {
            override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
                val annotation = didSelectAnnotationView.annotation ?: return
                val title = annotation.title
                val subtitle = annotation.subtitle
                val selected = markers.firstOrNull { it.title == title && it.snippet == subtitle }
                if (selected != null) {
                    onMarkerSelected(selected)
                }
            }
        }
    }

    val mapView = remember { 
        MKMapView().apply {
            showsUserLocation = true
        }
    }

    LaunchedEffect(delegate) {
        mapView.delegate = delegate
    }

    LaunchedEffect(Unit) {
        locationManager.requestWhenInUseAuthorization()
    }

    LaunchedEffect(markers) {
        // Limpia annotations anteriores
        mapView.removeAnnotations(mapView.annotations)

        if (markers.isEmpty()) return@LaunchedEffect

        // Agrega annotations
        markers.forEach { marker ->
            val annotation = MKPointAnnotation()
            annotation.setCoordinate(
                CLLocationCoordinate2DMake(marker.latitude, marker.longitude)
            )
            annotation.setTitle(marker.title)
            annotation.setSubtitle(marker.snippet)
            mapView.addAnnotation(annotation)
        }

        // Centra el mapa en el promedio de coordenadas
        val avgLat = markers.map { it.latitude }.average()
        val avgLng = markers.map { it.longitude }.average()
        val region = MKCoordinateRegionMakeWithDistance(
            CLLocationCoordinate2DMake(avgLat, avgLng),
            500_000.0,
            500_000.0
        )
        mapView.setRegion(region, animated = true)
    }

    UIKitView(
        factory  = { mapView },
        modifier = modifier,
        update   = {}
    )
}