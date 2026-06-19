package org.smlpartners.smlgo.domain.model

import kotlinx.datetime.LocalDateTime

data class Waypoint(
    val id             : Int,
    val routeId        : Int,
    val address        : String,
    val latitude       : Double?,
    val longitude      : Double?,
    val orderSequence  : Int,
    val clientId       : Int,
    val clientName     : String?,
    val status         : WaypointStatus,
    val visitedAt      : LocalDateTime?,
    val urlPhoto       : String?,
    val comment        : String?
) {
    val isPending   : Boolean get() = status == WaypointStatus.PENDIENTE
    val isVisited   : Boolean get() = status == WaypointStatus.VISITA
    val isCancelled : Boolean get() = status == WaypointStatus.CANCELADA
    val hasLocation : Boolean get() = latitude != null && longitude != null
}

enum class WaypointStatus {
    PENDIENTE,
    VISITA,
    CANCELADA;

    companion object {
        fun from(value: String) = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: PENDIENTE
    }
}