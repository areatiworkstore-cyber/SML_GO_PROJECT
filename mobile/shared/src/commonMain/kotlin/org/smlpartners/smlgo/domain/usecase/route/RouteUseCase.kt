package org.smlpartners.smlgo.domain.usecase.route

import org.smlpartners.smlgo.core.network.ApiError
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Route
import org.smlpartners.smlgo.domain.repository.AuthRepository
import org.smlpartners.smlgo.domain.repository.RouteRepository
import kotlinx.datetime.LocalDate

class GetRoutesUseCase(private val repository: RouteRepository) {
    suspend operator fun invoke(): ApiResult<List<Route>> =
        repository.getRoutes()
}

class GetRouteByIdUseCase(private val repository: RouteRepository) {
    suspend operator fun invoke(id: Int): ApiResult<Route> =
        repository.getRouteById(id)
}

class CreateRouteUseCase(
    private val routeRepository : RouteRepository,
    private val authRepository  : AuthRepository
) {
    suspend operator fun invoke(
        name          : String,
        scheduledDate : LocalDate,
        waypointIds   : List<Int> = emptyList()
    ): ApiResult<Route> {
        if (name.isBlank()) return ApiResult.Error(
            ApiError.UnknownError("El nombre de la ruta es obligatorio")
        )
        val userId = authRepository.getCurrentUserId() ?: return ApiResult.Error(
            ApiError.UnknownError("No hay sesión activa")
        )
        return routeRepository.createRoute(
            name          = name.trim(),
            scheduledDate = scheduledDate.toString(),
            userId        = userId,
            waypointIds   = waypointIds
        )
    }
}

class UpdateRouteUseCase(private val repository: RouteRepository) {
    suspend operator fun invoke(id: Int, route: Route): ApiResult<Route> {
        if (route.name.isBlank()) return ApiResult.Error(
            ApiError.UnknownError("El nombre de la ruta es obligatorio")
        )
        return repository.updateRoute(id, route)
    }
}

class DeleteRouteUseCase(private val repository: RouteRepository) {
    suspend operator fun invoke(id: Int): ApiResult<Unit> =
        repository.deleteRoute(id)
}