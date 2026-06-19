package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.data.mapper.toCreateDto
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.remote.api.WaypointApiService
import org.smlpartners.smlgo.data.remote.dto.WaypointStatusRequestDto
import org.smlpartners.smlgo.domain.model.Waypoint
import org.smlpartners.smlgo.domain.model.WaypointStatus
import org.smlpartners.smlgo.domain.repository.WaypointRepository

class WaypointRepositoryImpl(
    private val api: WaypointApiService
) : WaypointRepository {

    override suspend fun createWaypoint(waypoint: Waypoint): ApiResult<Waypoint> =
        safeApiCall {
            api.createWaypoint(waypoint.routeId, waypoint.toCreateDto()).toDomain()
        }

    override suspend fun updateWaypointStatus(
        routeId: Int,
        waypointId: Int,
        status: WaypointStatus,
        comment: String?
    ): ApiResult<Waypoint> =
        safeApiCall {
            api.updateWaypointStatus(
                waypointId = waypointId,
                request    = WaypointStatusRequestDto(
                    status  = status.name,
                    comment = comment
                )
            ).toDomain()
        }

    override suspend fun uploadWaypointPhoto(
        waypointId: Int,
        imageBytes: ByteArray,
        filename: String
    ): ApiResult<Waypoint> =
        safeApiCall {
            api.uploadWaypointPhoto(
                waypointId = waypointId,
                imageBytes = imageBytes,
                filename = filename
            ).toDomain()
        }
}
