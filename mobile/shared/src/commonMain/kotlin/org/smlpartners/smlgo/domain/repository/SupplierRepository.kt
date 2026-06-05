package org.smlpartners.smlgo.domain.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Supplier

interface SupplierRepository {
    suspend fun getSuppliers(): ApiResult<List<Supplier>>
    suspend fun createSupplier(supplier: Supplier): ApiResult<Supplier>
    suspend fun updateSupplier(id: Int, supplier: Supplier): ApiResult<Supplier>
}