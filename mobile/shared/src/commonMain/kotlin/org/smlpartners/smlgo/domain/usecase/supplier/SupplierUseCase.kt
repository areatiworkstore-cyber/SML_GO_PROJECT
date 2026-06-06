package org.smlpartners.smlgo.domain.usecase.supplier

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Supplier
import org.smlpartners.smlgo.domain.repository.SupplierRepository

class GetSuppliersUseCase(private val repository: SupplierRepository) {
    suspend operator fun invoke(): ApiResult<List<Supplier>> =
        repository.getSuppliers()
}