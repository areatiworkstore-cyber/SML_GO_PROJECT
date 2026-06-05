package org.smlpartners.smlgo.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.smlpartners.smlgo.data.remote.dto.RoleDto

class RoleApiService(private val client: HttpClient) {

    suspend fun getRoles(): List<RoleDto> =
        client.get("/master_data/roles").body()

    suspend fun getRoleById(id: Int): RoleDto =
        client.get("/master_data/role/$id").body()

}