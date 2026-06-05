package org.smlpartners.smlgo.data.remote.api

import org.smlpartners.smlgo.data.remote.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class MasterDataApiService(private val client: HttpClient) {

    suspend fun getDocumentTypes(): List<DocumentTypeDto> =
        client.get("/master_data/document-types").body()

    suspend fun getBusinessTypes(): List<BusinessTypeDto> =
        client.get("/master_data/business-types").body()

    suspend fun getClientGroups(): List<ClientGroupDto> =
        client.get("/master_data/client-groups").body()

    suspend fun getRoles(): List<RoleDto> =
        client.get("/master_data/roles").body()

    suspend fun getRoleById(id: Int): RoleDto =
        client.get("/master_data/role/$id").body()
}