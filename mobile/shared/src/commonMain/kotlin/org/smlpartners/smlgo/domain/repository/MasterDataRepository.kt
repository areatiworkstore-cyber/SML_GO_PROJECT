package org.smlpartners.smlgo.domain.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.*

interface MasterDataRepository {
    suspend fun getDocumentTypes(): ApiResult<List<DocumentType>>
    suspend fun getBusinessTypes(): ApiResult<List<BusinessType>>
    suspend fun getClientGroups(): ApiResult<List<ClientGroup>>
}