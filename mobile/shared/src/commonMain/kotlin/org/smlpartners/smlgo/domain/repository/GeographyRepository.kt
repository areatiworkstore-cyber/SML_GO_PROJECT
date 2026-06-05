package org.smlpartners.smlgo.domain.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.*

interface GeographyRepository {
    suspend fun getDepartments(): ApiResult<List<Department>>
    suspend fun getProvinces(departmentId: Int): ApiResult<List<Province>>
    suspend fun getDistricts(provinceId: Int): ApiResult<List<District>>
}