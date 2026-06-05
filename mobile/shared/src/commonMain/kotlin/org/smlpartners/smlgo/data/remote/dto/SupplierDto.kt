package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupplierDto(
    @SerialName("id")     val id     : Int,
    @SerialName("code")   val code   : String,
    @SerialName("names")  val names  : String,
    @SerialName("active") val active : Boolean
)