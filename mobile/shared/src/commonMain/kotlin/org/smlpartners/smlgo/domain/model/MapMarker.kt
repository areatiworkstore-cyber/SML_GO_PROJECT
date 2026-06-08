package org.smlpartners.smlgo.domain.model

data class MapMarker(
    val id        : Int,
    val latitude  : Double,
    val longitude : Double,
    val title     : String,
    val snippet   : String? = null,
    val isSelected: Boolean = false
)

fun Client.toMapMarker() = MapMarker(
    id        = id,
    latitude  = latitude ?: 0.0,
    longitude = longitude ?: 0.0,
    title     = name ?: "Cliente $id",
    snippet   = address
)