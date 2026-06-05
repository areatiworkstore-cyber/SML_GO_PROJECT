package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentTypeDto(
    @SerialName("id")           val id          : Int,
    @SerialName("description")  val description : String
)

@Serializable
data class BusinessTypeDto(
    @SerialName("id")           val id          : Int,
    @SerialName("description")  val description : String
)

@Serializable
data class ClientGroupDto(
    @SerialName("id")           val id          : Int,
    @SerialName("description")  val description : String
)