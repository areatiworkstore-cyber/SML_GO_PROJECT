// domain/model/Route.kt
package org.smlpartners.smlgo.domain.model

import kotlinx.datetime.LocalDate

data class Route(
    val id            : Int,
    val name          : String,
    val scheduledDate : LocalDate,
    val active        : Boolean,
    val waypoints     : List<Waypoint>
) {
    val pendingCount  : Int get() = waypoints.count { it.status == WaypointStatus.PENDIENTE }
    val visitedCount  : Int get() = waypoints.count { it.status == WaypointStatus.VISITA }
    val cancelledCount: Int get() = waypoints.count { it.status == WaypointStatus.CANCELADA }
    val totalCount    : Int get() = waypoints.size
    val progress      : Float get() = if (totalCount == 0) 0f else (visitedCount + cancelledCount).toFloat() / totalCount
}