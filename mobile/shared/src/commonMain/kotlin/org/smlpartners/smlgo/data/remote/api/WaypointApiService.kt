package org.smlpartners.smlgo.data.remote.api

import org.smlpartners.smlgo.data.remote.dto.WaypointDto
import org.smlpartners.smlgo.data.remote.dto.WaypointCreateDto
import org.smlpartners.smlgo.data.remote.dto.WaypointStatusRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class WaypointApiService(private val client: HttpClient) {

    suspend fun createWaypoint(routeId: Int, request: WaypointCreateDto): WaypointDto =
        client.post("/routes/$routeId/waypoints") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateWaypointStatus(
        waypointId: Int,
        request: WaypointStatusRequestDto
    ): WaypointDto =
        client.put("/routes/waypoints/$waypointId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
