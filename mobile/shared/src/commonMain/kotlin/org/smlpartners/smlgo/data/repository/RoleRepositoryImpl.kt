package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.remote.api.RoleApiService
import org.smlpartners.smlgo.domain.model.Role
import org.smlpartners.smlgo.domain.repository.RoleRepository

class RoleRepositoryImpl(
    private val api: RoleApiService
) : RoleRepository {

    override suspend fun getRoles(): ApiResult<List<Role>> =
        safeApiCall { api.getRoles().map { it.toDomain() } }
}
