package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.mapper.toRequestDto
import org.smlpartners.smlgo.data.remote.api.SupplierApiService
import org.smlpartners.smlgo.domain.model.Supplier
import org.smlpartners.smlgo.domain.repository.SupplierRepository

class SupplierRepositoryImpl(
    private val api: SupplierApiService
) : SupplierRepository {

    override suspend fun getSuppliers(): ApiResult<List<Supplier>> =
        safeApiCall { api.getSuppliers().map { it.toDomain() } }

    override suspend fun createSupplier(supplier: Supplier): ApiResult<Supplier> =
        safeApiCall { api.createSupplier(supplier.toRequestDto()).toDomain() }

    override suspend fun updateSupplier(id: Int, supplier: Supplier): ApiResult<Supplier> =
        safeApiCall { api.updateSupplier(id, supplier.toRequestDto()).toDomain() }
}
