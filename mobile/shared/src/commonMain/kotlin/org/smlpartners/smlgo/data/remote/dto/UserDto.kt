package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")               val id              : Int,
    @SerialName("code")             val code            : String,
    @SerialName("first_name")       val firstName       : String? = null,
    @SerialName("second_name")      val secondName      : String? = null,
    @SerialName("first_surname")    val firstSurname    : String? = null,
    @SerialName("second_surname")   val secondSurname   : String? = null,
    @SerialName("document_type")    val documentType    : DocumentTypeDto? = null,
    @SerialName("document_number")  val documentNumber  : String? = null,
    @SerialName("cellphone")        val cellphone       : String? = null,
    @SerialName("email")            val email           : String? = null,
    @SerialName("roles")            val roles           : List<RoleUserDto> = emptyList()
)

@Serializable
data class MyProfileDto(
    @SerialName("id")               val id              : Int,
    @SerialName("code")             val code            : String,
    @SerialName("first_name")       val firstName       : String,
    @SerialName("second_name")      val secondName      : String? = null,
    @SerialName("first_surname")    val firstSurname    : String,
    @SerialName("second_surname")   val secondSurname   : String,
    @SerialName("document_type")    val documentType    : DocumentTypeDto,
    @SerialName("document_number")  val documentNumber  : String,
    @SerialName("cellphone")        val cellphone       : String? = null,
    @SerialName("email")            val email           : String,
    @SerialName("roles")            val roles           : List<String>     = emptyList()
)

@Serializable
data class UserUpdateDto(
    @SerialName("first_name")       val firstName       : String? = null,
    @SerialName("second_name")      val secondName      : String? = null,
    @SerialName("first_surname")    val firstSurname    : String? = null,
    @SerialName("second_surname")   val secondSurname   : String? = null,
    @SerialName("document_type_id") val documentTypeId  : Int? = null,
    @SerialName("document_number")  val documentNumber  : String? = null,
    @SerialName("cellphone")        val cellphone       : String? = null,
    @SerialName("email")            val email           : String? = null,
    @SerialName("password")         val password        : String? = null,
    @SerialName("active")           val active          : Boolean? = null,
    @SerialName("role_ids")         val roleIds         : List<Int>? = null
)

@Serializable
data class RoleUserDto(
    @SerialName("id")           val id          : Int,
    @SerialName("user_id")      val userId     : Int      = 0,
    @SerialName("role_id")      val roleId     : Int      = 0,
    @SerialName("role_details") val roleDetails: RoleDto? = null
)