package org.smlpartners.smlgo.domain.usecase.masterdata

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.*
import org.smlpartners.smlgo.domain.repository.MasterDataRepository
import org.smlpartners.smlgo.domain.repository.RoleRepository

data class ProfileMasterData(
    val documentTypes : List<DocumentType>,
    val roles         : List<Role>
)

class GetProfileMasterDataUseCase(
    private val masterDataRepository : MasterDataRepository,
    private val roleRepository       : RoleRepository
) {
    suspend operator fun invoke(): ApiResult<ProfileMasterData> {
        val documentTypesResult = masterDataRepository.getDocumentTypes()
        val rolesResult         = roleRepository.getRoles()

        if (documentTypesResult is ApiResult.Error) return documentTypesResult
        if (rolesResult is ApiResult.Error) return rolesResult

        return ApiResult.Success(
            ProfileMasterData(
                documentTypes = (documentTypesResult as ApiResult.Success).data,
                roles         = (rolesResult as ApiResult.Success).data
            )
        )
    }
}