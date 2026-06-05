package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.remote.api.GeographyApiService
import org.smlpartners.smlgo.domain.model.Department
import org.smlpartners.smlgo.domain.model.District
import org.smlpartners.smlgo.domain.model.Province
import org.smlpartners.smlgo.domain.repository.GeographyRepository

class GeographyRepositoryImpl(
    private val api: GeographyApiService
) : GeographyRepository {

    override suspend fun getDepartments(): ApiResult<List<Department>> =
        safeApiCall { api.getDepartments().map { it.toDomain() } }

    override suspend fun getProvinces(departmentId: Int): ApiResult<List<Province>> =
        safeApiCall { api.getProvinces(departmentId).map { it.toDomain() } }

    override suspend fun getDistricts(provinceId: Int): ApiResult<List<District>> =
        safeApiCall { api.getDistricts(provinceId).map { it.toDomain() } }
}
