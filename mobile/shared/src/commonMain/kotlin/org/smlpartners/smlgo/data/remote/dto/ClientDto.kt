package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    @SerialName("id")               val id             : Int,
    @SerialName("code")             val code           : String?  = null,
    @SerialName("name")             val name           : String?  = null,
    @SerialName("document_type_id") val documentTypeId : Int?     = null,
    @SerialName("document_number")  val documentNumber : String?  = null,
    @SerialName("address")          val address        : String?  = null,
    @SerialName("district_id")      val districtId     : Int?     = null,
    @SerialName("district")         val district       : DistrictDto?  = null,
    @SerialName("province")         val province       : ProvinceDto?  = null,
    @SerialName("department")       val department     : DepartmentDto? = null,
    @SerialName("business_type_id") val businessTypeId : Int?     = null,
    @SerialName("client_group_id")  val clientGroupId  : Int?     = null,
    @SerialName("cellphone")        val cellphone      : String?  = null,
    @SerialName("telephone")        val telephone      : String?  = null,
    @SerialName("active")           val active         : Boolean  = true,
    @SerialName("latitud")          val latitude       : Double?  = null,
    @SerialName("longitud")         val longitude      : Double?  = null,
    @SerialName("observation")      val observation    : String?  = null,
    @SerialName("user_id")          val userId         : Int?     = null,
    @SerialName("created_at")       val createdAt      : String?  = null,
    @SerialName("updated_at")       val updatedAt      : String?  = null
)

@Serializable
data class ClientRequestDto(
    @SerialName("code")                 val code            : String?,
    @SerialName("name")                 val name            : String?,
    @SerialName("document_type_id")     val documentTypeId  : Int?,
    @SerialName("document_number")      val documentNumber  : String?,
    @SerialName("address")              val address         : String?,
    @SerialName("district_id")          val districtId      : Int?,
    @SerialName("business_type_id")     val businessTypeId  : Int?,
    @SerialName("client_group_id")      val clientGroupId   : Int?,
    @SerialName("cellphone")            val cellphone       : String?,
    @SerialName("telephone")            val telephone       : String?,
    @SerialName("active")               val active          : Boolean = true,
    @SerialName("latitud")              val latitude        : Double?,
    @SerialName("longitud")             val longitude       : Double?,
    @SerialName("observation")          val observation     : String?,
)

@Serializable
data class WaypointClientInfoDto(
    @SerialName("id")               val id             : Int,
    @SerialName("name")             val name           : String?,
)

@Serializable
data class NextCodeDto(
    @SerialName("next_code")        val code           : String?,
)