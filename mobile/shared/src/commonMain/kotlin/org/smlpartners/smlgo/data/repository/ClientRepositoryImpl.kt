package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.mapper.toRequestDto
import org.smlpartners.smlgo.data.remote.api.ClientApiService
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.domain.model.NextCode
import org.smlpartners.smlgo.domain.repository.ClientRepository

class ClientRepositoryImpl(
    private val api: ClientApiService
) : ClientRepository {

    override suspend fun getClients(): ApiResult<List<Client>> =
        safeApiCall { api.getClients().map { it.toDomain() } }

    override suspend fun getClientById(id: Int): ApiResult<Client> =
        safeApiCall { api.getClientById(id).toDomain() }

    override suspend fun createClient(client: Client): ApiResult<Client> =
        safeApiCall { api.createClient(client.toRequestDto()).toDomain() }

    override suspend fun updateClient(id: Int, client: Client): ApiResult<Client> =
        safeApiCall { api.updateClient(id, client.toRequestDto()).toDomain() }

    override suspend fun getClientsWithLocation(): ApiResult<List<Client>> =
        safeApiCall {
            api.getClients()
                .map { it.toDomain() }
                .filter { it.hasLocation }
        }
    override suspend fun getNextCode(): ApiResult<NextCode> =
        safeApiCall { api.getNextCode().toDomain() }
}