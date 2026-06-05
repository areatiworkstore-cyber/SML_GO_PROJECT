package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    @SerialName("id")               val id             : Int,
    @SerialName("code")             val code           : String?,
    @SerialName("name")             val name           : String?,
    @SerialName("document_type")    val documentType   : DocumentTypeDto?,
    @SerialName("document_number")  val documentNumber : String?,
    @SerialName("address")          val address        : String?,
    @SerialName("district")         val district       : DistrictDto?,
    @SerialName("business_type")    val businessType   : BusinessTypeDto?,
    @SerialName("client_group")     val clientGroup    : ClientGroupDto?,
    @SerialName("cellphone")        val cellphone      : String?,
    @SerialName("telephone")        val telephone      : String?,
    @SerialName("active")           val active         : Boolean,
    @SerialName("latitud")          val latitude       : Double?,
    @SerialName("longitud")         val longitude      : Double?,
    @SerialName("observation")      val observation    : String?,
    @SerialName("supplier")         val supplier       : SupplierDto?
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
    @SerialName("latitud")              val latitude        : Double?,
    @SerialName("longitud")             val longitude       : Double?,
    @SerialName("observation")          val observation     : String?,
    @SerialName("supplier_id")          val supplierId      : Int?
)

@Serializable
data class WaypointClientInfoDto(
    @SerialName("id")               val id             : Int,
    @SerialName("name")             val name           : String?,
)