package org.smlpartners.smlgo.data.remote.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import org.smlpartners.smlgo.data.remote.dto.RoleDto
import org.smlpartners.smlgo.core.network.HttpClientManager

class RoleApiService(private val manager: HttpClientManager) {
    private val client get() = manager.client

    suspend fun getRoles(): List<RoleDto> =
        client.get("master_data/roles").body()

    suspend fun getRoleById(id: Int): RoleDto =
        client.get("master_data/role/$id").body()

}