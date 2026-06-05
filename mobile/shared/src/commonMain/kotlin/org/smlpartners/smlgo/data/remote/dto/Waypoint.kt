package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class WaypointDto(
    @SerialName("id")               val id              : Int,
    @SerialName("route_id")         val routeId         : Int,
    @SerialName("address")          val address         : String,
    @SerialName("latitud")          val latitude        : Double?,
    @SerialName("longitud")         val longitude       : Double?,
    @SerialName("order_sequence")   val orderSequence   : Int,
    @SerialName("client")           val client          : WaypointClientInfoDto?,
    @SerialName("status")           val status          : String,
    @SerialName("visited_at")       val visitedAt       : String?,
    @SerialName("comment")          val comment         : String?
)

@Serializable
data class WaypointCreateDto(
    @SerialName("address")          val address       : String,
    @SerialName("latitud")          val latitude      : Double?  = null,
    @SerialName("longitud")         val longitude     : Double?  = null,
    @SerialName("order_sequence")   val orderSequence : Int,
    @SerialName("client_id")        val clientId      : Int,
    @SerialName("status")           val status        : String   = "PENDIENTE",
    @SerialName("comment")          val comment       : String?  = null
)

@Serializable
data class WaypointUpdateDto(
    @SerialName("status")           val status        : String?  = null,
    @SerialName("visited_at")       val visitedAt     : String?  = null,
    @SerialName("comment")          val comment       : String?  = null,
    @SerialName("order_sequence")   val orderSequence : Int?     = null,
    @SerialName("address")          val address       : String?  = null,
    @SerialName("latitud")          val latitude      : Double?  = null,
    @SerialName("longitud")         val longitude     : Double?  = null
)

@Serializable
data class WaypointStatusRequestDto(
    @SerialName("status")           val status          : String,
    @SerialName("comment")          val comment         : String?
)