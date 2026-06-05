package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoleDto(
    @SerialName("id")   val id   : Int,
    @SerialName("role") val role : String
)