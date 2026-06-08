package org.smlpartners.smlgo.data.remote.api

import org.smlpartners.smlgo.data.remote.dto.DepartmentDto
import org.smlpartners.smlgo.data.remote.dto.ProvinceDto
import org.smlpartners.smlgo.data.remote.dto.DistrictDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

import org.smlpartners.smlgo.core.network.HttpClientManager

class GeographyApiService(private val manager: HttpClientManager) {
    private val client get() = manager.client

    suspend fun getDepartments(): List<DepartmentDto> =
        client.get("geography/departments").body()

    suspend fun getProvinces(departmentId: Int): List<ProvinceDto> =
        client.get("geography/provinces") {
            parameter("department_id", departmentId)
        }.body()

    suspend fun getDistricts(provinceId: Int): List<DistrictDto> =
        client.get("geography/districts") {
            parameter("province_id", provinceId)
        }.body()
}
