package org.smlpartners.smlgo.data.remote.api

import org.smlpartners.smlgo.data.remote.dto.ClientScheduleDto
import org.smlpartners.smlgo.data.remote.dto.ClientScheduleRequestDto
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

import org.smlpartners.smlgo.core.network.HttpClientManager

class ScheduleApiService(private val manager: HttpClientManager) {
    private val client get() = manager.client

    suspend fun getClientSchedules(): List<ClientScheduleDto> =
        client.get("client_schedules").body()

    suspend fun getClientScheduleById(id: Int): ClientScheduleDto =
        client.get("client_schedules/$id").body()

    suspend fun getClientSchedulesByWeek(start: String, end: String): List<ClientScheduleDto> =
        client.get("client_schedules") {
            parameter("start", start)
            parameter("end",   end)
        }.body()

    suspend fun createClientSchedule(request: ClientScheduleRequestDto): ClientScheduleDto =
        client.post("client_schedules") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateClientSchedule(id: Int, request: ClientScheduleRequestDto): ClientScheduleDto =
        client.put("client_schedules/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteClientSchedule(id: Int): Unit =
        client.delete("client_schedules/$id").body()
}