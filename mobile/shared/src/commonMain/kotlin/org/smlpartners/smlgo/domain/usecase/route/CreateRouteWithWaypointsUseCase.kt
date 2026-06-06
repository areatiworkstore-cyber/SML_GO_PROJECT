package org.smlpartners.smlgo.domain.usecase.route

import org.smlpartners.smlgo.core.network.ApiError
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.domain.model.Route
import org.smlpartners.smlgo.domain.model.Waypoint
import org.smlpartners.smlgo.domain.model.WaypointStatus
import org.smlpartners.smlgo.domain.usecase.waypoint.CreateWaypointUseCase
import kotlinx.datetime.LocalDate

data class WaypointInput(
    val client        : Client,
    val orderSequence : Int
)

class CreateRouteWithWaypointsUseCase(
    private val createRoute    : CreateRouteUseCase,
    private val createWaypoint : CreateWaypointUseCase
) {
    suspend operator fun invoke(
        name          : String,
        scheduledDate : LocalDate,
        waypoints     : List<WaypointInput>
    ): ApiResult<Route> {

        if (waypoints.isEmpty()) return ApiResult.Error(
            ApiError.UnknownError("La ruta debe tener al menos una parada")
        )

        // 1. Crea la ruta
        val routeResult = createRoute(name, scheduledDate)
        if (routeResult is ApiResult.Error) return routeResult
        val route = (routeResult as ApiResult.Success).data

        // 2. Agrega waypoints en orden — continúa si alguno falla
        val failures = mutableListOf<String>()
        waypoints.sortedBy { it.orderSequence }.forEach { input ->
            val result = createWaypoint(
                Waypoint(
                    id            = 0,
                    routeId       = route.id,
                    address       = input.client.address ?: "",
                    latitude      = input.client.latitude,
                    longitude     = input.client.longitude,
                    orderSequence = input.orderSequence,
                    clientId      = input.client.id,
                    clientName    = input.client.name,
                    status        = WaypointStatus.PENDIENTE,
                    visitedAt     = null,
                    comment       = null
                )
            )
            if (result is ApiResult.Error) {
                failures.add(input.client.name ?: "Cliente ${input.client.id}")
            }
        }

        return if (failures.isEmpty()) {
            routeResult
        } else {
            // Ruta creada pero con waypoints fallidos — la UI mostrará el warning
            ApiResult.Error(
                ApiError.UnknownError(
                    "Ruta creada pero fallaron: ${failures.joinToString(", ")}"
                )
            )
        }
    }
}