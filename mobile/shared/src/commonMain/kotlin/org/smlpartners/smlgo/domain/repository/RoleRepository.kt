package org.smlpartners.smlgo.domain.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Role

interface RoleRepository {
    suspend fun getRoles(): ApiResult<List<Role>>
}