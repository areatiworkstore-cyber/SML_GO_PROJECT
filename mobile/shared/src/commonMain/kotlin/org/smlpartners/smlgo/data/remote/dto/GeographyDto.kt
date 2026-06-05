package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class DepartmentDto(
    @SerialName("id")       val id       : Int,
    @SerialName("name")     val name     : String,
    @SerialName("active")   val active   : Boolean
)

@Serializable
data class ProvinceDto(
    @SerialName("id")             val id           : Int,
    @SerialName("name")           val name         : String,
    @SerialName("active")         val active       : Boolean,
    @SerialName("department_id")  val departmentId : Int
)

@Serializable
data class DistrictDto(
    @SerialName("id")           val id         : Int,
    @SerialName("name")         val name       : String,
    @SerialName("active")       val active     : Boolean,
    @SerialName("province_id")  val provinceId : Int
)