package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.remote.api.MasterDataApiService
import org.smlpartners.smlgo.domain.model.BusinessType
import org.smlpartners.smlgo.domain.model.ClientGroup
import org.smlpartners.smlgo.domain.model.DocumentType
import org.smlpartners.smlgo.domain.repository.MasterDataRepository

class MasterDataRepositoryImpl(
    private val api: MasterDataApiService
) : MasterDataRepository {

    override suspend fun getDocumentTypes(): ApiResult<List<DocumentType>> =
        safeApiCall { api.getDocumentTypes().map { it.toDomain() } }

    override suspend fun getBusinessTypes(): ApiResult<List<BusinessType>> =
        safeApiCall { api.getBusinessTypes().map { it.toDomain() } }

    override suspend fun getClientGroups(): ApiResult<List<ClientGroup>> =
        safeApiCall { api.getClientGroups().map { it.toDomain() } }
}
