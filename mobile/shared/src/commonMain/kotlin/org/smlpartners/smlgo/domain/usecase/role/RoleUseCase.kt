package org.smlpartners.smlgo.domain.usecase.role

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Role
import org.smlpartners.smlgo.domain.repository.RoleRepository

class GetRolesUseCase(private val repository: RoleRepository) {
    suspend operator fun invoke(): ApiResult<List<Role>> =
        repository.getRoles()
}