package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.mapper.toUpdateDto
import org.smlpartners.smlgo.data.remote.api.RouteApiService
import org.smlpartners.smlgo.data.remote.dto.RouteCreateDto
import org.smlpartners.smlgo.domain.model.Route
import org.smlpartners.smlgo.domain.repository.RouteRepository

class RouteRepositoryImpl(
    private val api: RouteApiService
) : RouteRepository {

    override suspend fun getRoutes(): ApiResult<List<Route>> =
        safeApiCall { api.getRoutes().map { it.toDomain() } }

    override suspend fun getRouteById(id: Int): ApiResult<Route> =
        safeApiCall { api.getRouteById(id).toDomain() }

    override suspend fun createRoute(
        name          : String,
        scheduledDate : String,
        userId        : Int,
        waypointIds   : List<Int>
    ): ApiResult<Route> =
        safeApiCall {
            api.createRoute(
                RouteCreateDto(
                    name          = name,
                    scheduledDate = scheduledDate,
                    userId        = userId,
                    waypointIds   = waypointIds
                )
            ).toDomain()
        }

    override suspend fun deleteRoute(id: Int): ApiResult<Unit> =
        safeApiCall { api.deleteRoute(id) }

    override suspend fun updateRoute(id: Int, route: Route): ApiResult<Route> =
        safeApiCall {
            api.updateRoute(id, route.toUpdateDto()).toDomain()
        }
}
