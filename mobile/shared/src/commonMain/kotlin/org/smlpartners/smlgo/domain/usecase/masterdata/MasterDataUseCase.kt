package org.smlpartners.smlgo.domain.usecase.masterdata

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.*
import org.smlpartners.smlgo.domain.repository.GeographyRepository
import org.smlpartners.smlgo.domain.repository.MasterDataRepository
import org.smlpartners.smlgo.domain.repository.RoleRepository
import org.smlpartners.smlgo.domain.repository.SupplierRepository

class GetDocumentTypesUseCase(private val repository: MasterDataRepository) {
    suspend operator fun invoke(): ApiResult<List<DocumentType>> =
        repository.getDocumentTypes()
}

class GetBusinessTypesUseCase(private val repository: MasterDataRepository) {
    suspend operator fun invoke(): ApiResult<List<BusinessType>> =
        repository.getBusinessTypes()
}

class GetClientGroupsUseCase(private val repository: MasterDataRepository) {
    suspend operator fun invoke(): ApiResult<List<ClientGroup>> =
        repository.getClientGroups()
}

// ── Use case compuesto para el formulario de cliente ─────────────────────

data class ClientFormMasterData(
    val documentTypes : List<DocumentType>,
    val businessTypes : List<BusinessType>,
    val clientGroups  : List<ClientGroup>,
    val suppliers     : List<Supplier>
)

class GetClientFormMasterDataUseCase(
    private val masterDataRepository : MasterDataRepository,
    private val supplierRepository   : SupplierRepository
) {
    suspend operator fun invoke(): ApiResult<ClientFormMasterData> {
        val documentTypes = masterDataRepository.getDocumentTypes()
        val businessTypes = masterDataRepository.getBusinessTypes()
        val clientGroups  = masterDataRepository.getClientGroups()
        val suppliers     = supplierRepository.getSuppliers()

        listOf(documentTypes, businessTypes, clientGroups, suppliers).forEach {
            if (it is ApiResult.Error) return it
        }

        return ApiResult.Success(
            ClientFormMasterData(
                documentTypes = (documentTypes as ApiResult.Success).data,
                businessTypes = (businessTypes as ApiResult.Success).data,
                clientGroups  = (clientGroups  as ApiResult.Success).data,
                suppliers     = (suppliers     as ApiResult.Success).data
            )
        )
    }
}