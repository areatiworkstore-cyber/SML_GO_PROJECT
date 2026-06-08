package org.smlpartners.smlgo.data.remote.api

import org.smlpartners.smlgo.data.remote.dto.RouteDto
import org.smlpartners.smlgo.data.remote.dto.RouteCreateDto
import org.smlpartners.smlgo.data.remote.dto.RouteUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RouteApiService(private val client: HttpClient) {

    suspend fun getRoutes(): List<RouteDto> =
        client.get("routes").body()

    suspend fun getRouteById(id: Int): RouteDto =
        client.get("routes/$id").body()

    suspend fun createRoute(request: RouteCreateDto): RouteDto =
        client.post("routes") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateRoute(id: Int, request: RouteUpdateDto): RouteDto =
        client.put("routes/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteRoute(id: Int): Unit =
        client.delete("routes/$id").body()
}