package org.smlpartners.smlgo.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.smlpartners.smlgo.domain.model.MapMarker

@Composable
expect fun PlatformMapView(
    markers          : List<MapMarker>,
    onMarkerSelected : (MapMarker) -> Unit,
    modifier         : Modifier = Modifier
)