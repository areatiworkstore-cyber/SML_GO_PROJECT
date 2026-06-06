package org.smlpartners.smlgo.domain.usecase.geography

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Department
import org.smlpartners.smlgo.domain.model.District
import org.smlpartners.smlgo.domain.model.Province
import org.smlpartners.smlgo.domain.repository.GeographyRepository

class GetDepartmentsUseCase(private val repository: GeographyRepository) {
    suspend operator fun invoke(): ApiResult<List<Department>> =
        repository.getDepartments()
}

class GetProvincesUseCase(private val repository: GeographyRepository) {
    suspend operator fun invoke(departmentId: Int): ApiResult<List<Province>> =
        repository.getProvinces(departmentId)
}

class GetDistrictsUseCase(private val repository: GeographyRepository) {
    suspend operator fun invoke(provinceId: Int): ApiResult<List<District>> =
        repository.getDistricts(provinceId)
}