package org.smlpartners.smlgo.domain.usecase.waypoint

import org.smlpartners.smlgo.core.network.ApiError
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Waypoint
import org.smlpartners.smlgo.domain.model.WaypointStatus
import org.smlpartners.smlgo.domain.repository.WaypointRepository

class CreateWaypointUseCase(private val repository: WaypointRepository) {
    suspend operator fun invoke(waypoint: Waypoint): ApiResult<Waypoint> {
        if (waypoint.address.isBlank()) return ApiResult.Error(
            ApiError.UnknownError("La dirección del waypoint es obligatoria")
        )
        if (waypoint.clientId == 0) return ApiResult.Error(
            ApiError.UnknownError("El cliente es obligatorio")
        )
        return repository.createWaypoint(waypoint)
    }
}

class UpdateWaypointStatusUseCase(private val repository: WaypointRepository) {
    suspend operator fun invoke(
        routeId    : Int,
        waypointId : Int,
        status     : WaypointStatus,
        comment    : String?
    ): ApiResult<Waypoint> {
        if (status == WaypointStatus.PENDIENTE) return ApiResult.Error(
            ApiError.UnknownError("No se puede revertir el estado a PENDIENTE")
        )
        return repository.updateWaypointStatus(
            routeId    = routeId,
            waypointId = waypointId,
            status     = status,
            comment    = comment
        )
    }
}

class UploadWaypointPhotoUseCase(private val repository: WaypointRepository) {
    suspend operator fun invoke(
        waypointId : Int,
        imageBytes : ByteArray,
        filename   : String
    ): ApiResult<Waypoint> {
        if (waypointId <= 0) return ApiResult.Error(
            ApiError.UnknownError("ID de waypoint inválido")
        )
        if (imageBytes.isEmpty()) return ApiResult.Error(
            ApiError.UnknownError("El archivo de imagen no puede estar vacío")
        )
        return repository.uploadWaypointPhoto(
            waypointId = waypointId,
            imageBytes = imageBytes,
            filename   = filename
        )
    }
}