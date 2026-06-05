package org.smlpartners.smlgo.data.remote.api

import org.smlpartners.smlgo.data.remote.dto.SupplierDto
import org.smlpartners.smlgo.data.remote.dto.SupplierRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SupplierApiService(private val client: HttpClient) {

    suspend fun getSuppliers(): List<SupplierDto> =
        client.get("/suppliers").body()

    suspend fun createSupplier(request: SupplierRequestDto): SupplierDto =
        client.post("/suppliers") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getSupplierByCode(code: String): SupplierDto =
        client.get("/suppliers/search") {
            contentType(ContentType.Application.Json)
            parameter("code", code)
        }.body()

    suspend fun updateSupplier(id: Int, request: SupplierRequestDto): SupplierDto =
        client.put("/suppliers/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
