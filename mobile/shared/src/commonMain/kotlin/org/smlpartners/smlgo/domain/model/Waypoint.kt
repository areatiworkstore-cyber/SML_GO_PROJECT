package org.smlpartners.smlgo.domain.model

import kotlinx.datetime.LocalDateTime

data class Waypoint(
    val id             : Int,
    val routeId        : Int,
    val address        : String,
    val latitude       : Double?,
    val longitude      : Double?,
    val orderSequence  : Int,
    val client         : Client,
    val status         : WaypointStatus,
    val visitedAt      : LocalDateTime?,
    val comment        : String?
)

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