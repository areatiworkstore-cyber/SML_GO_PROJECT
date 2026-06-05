package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")               val id              : Int,
    @SerialName("code")             val code            : String,
    @SerialName("first_name")       val firstName       : String,
    @SerialName("second_name")      val secondName      : String,
    @SerialName("first_surname")    val firstSurname    : String,
    @SerialName("second_surname")   val secondSurname   : String,
    @SerialName("document_type")    val documentType    : DocumentTypeDto?,
    @SerialName("document_number")  val documentNumber  : String,
    @SerialName("cellphone")        val cellphone       : String,
    @SerialName("email")            val email           : String,
    @SerialName("roles")            val roles           : List<RoleUserDto> = emptyList()
)

@Serializable
data class RoleUserDto(
    @SerialName("id")           val id          : Int,
    @SerialName("role_details") val roleDetails : RoleDto?
)