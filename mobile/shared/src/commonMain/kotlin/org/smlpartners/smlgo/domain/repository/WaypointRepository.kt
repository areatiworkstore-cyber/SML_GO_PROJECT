package org.smlpartners.smlgo.domain.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Waypoint
import org.smlpartners.smlgo.domain.model.WaypointStatus

interface WaypointRepository {
    suspend fun createWaypoint(waypoint: Waypoint): ApiResult<Waypoint>
    suspend fun updateWaypointStatus(
        routeId    : Int,
        waypointId : Int,
        status     : WaypointStatus,
        comment    : String?
    ): ApiResult<Waypoint>
}