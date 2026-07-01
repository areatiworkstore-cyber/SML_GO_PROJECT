package org.smlpartners.smlgo.domain.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.domain.model.NextCode

interface ClientRepository {
    suspend fun getClients(userId: Int? = null): ApiResult<List<Client>>
    suspend fun getClientById(id: Int): ApiResult<Client>
    suspend fun createClient(client: Client): ApiResult<Client>
    suspend fun updateClient(id: Int, client: Client): ApiResult<Client>
    suspend fun getClientsWithLocation(): ApiResult<List<Client>>
    suspend fun getNextCode(): ApiResult<NextCode>
    suspend fun deleteClient(id: Int): ApiResult<Unit>
    suspend fun activateClient(id: Int): ApiResult<Unit>
}