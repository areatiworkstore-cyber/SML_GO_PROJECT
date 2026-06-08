package org.smlpartners.smlgo.data.remote.api

import org.smlpartners.smlgo.data.remote.dto.ClientDto
import org.smlpartners.smlgo.data.remote.dto.ClientRequestDto
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.smlpartners.smlgo.data.remote.dto.NextCodeDto

import org.smlpartners.smlgo.core.network.HttpClientManager

class ClientApiService(private val manager: HttpClientManager) {
    private val client get() = manager.client

    suspend fun getNextCode(): NextCodeDto =
        client.get("clients/next-code").body()

    suspend fun getClients(): List<ClientDto> =
        client.get("clients").body()


    suspend fun getClientsWithLocation(id: Int): String? =
        client.get("clients/$id/maps-direct").body()

    suspend fun getClientById(id: Int): ClientDto =
        client.get("clients/$id").body()

    suspend fun createClient(request: ClientRequestDto): ClientDto =
        client.post("clients") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateClient(id: Int, request: ClientRequestDto): ClientDto =
        client.put("clients/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteClient(id: Int): Unit = client.delete("clients/$id").body()
}