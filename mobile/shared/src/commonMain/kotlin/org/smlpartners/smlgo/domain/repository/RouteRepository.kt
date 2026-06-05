package org.smlpartners.smlgo.domain.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Route

interface RouteRepository {
    suspend fun getRoutes(): ApiResult<List<Route>>
    suspend fun getRouteById(id: Int): ApiResult<Route>
    suspend fun createRoute(
        name          : String,
        scheduledDate : String,
        waypointIds   : List<Int>
    ): ApiResult<Route>
    suspend fun deleteRoute(id: Int): ApiResult<Unit>
}