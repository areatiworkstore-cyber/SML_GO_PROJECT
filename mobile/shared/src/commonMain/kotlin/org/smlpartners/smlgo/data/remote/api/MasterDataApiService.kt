package org.smlpartners.smlgo.data.remote.api

import org.smlpartners.smlgo.data.remote.dto.*
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.smlpartners.smlgo.core.network.HttpClientManager

class MasterDataApiService(private val manager: HttpClientManager) {
    private val client get() = manager.client


    suspend fun getDocumentTypes(): List<DocumentTypeDto> =
        client.get("master_data/document-types").body()

    suspend fun getBusinessTypes(): List<BusinessTypeDto> =
        client.get("master_data/business-types").body()

    suspend fun getClientGroups(): List<ClientGroupDto> =
        client.get("master_data/client-groups").body()
}