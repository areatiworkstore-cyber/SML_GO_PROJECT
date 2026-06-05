package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteDto(
    @SerialName("id")               val id            : Int,
    @SerialName("name")             val name          : String,
    @SerialName("scheduled_date")   val scheduledDate : String,
    @SerialName("active")           val active        : Boolean,
    @SerialName("waypoints")        val waypoints     : List<WaypointDto> = emptyList()
)

@Serializable
data class RouteRequestDto(
    @SerialName("name")             val name          : String,
    @SerialName("scheduled_date")   val scheduledDate : String,
    @SerialName("waypoints")        val waypoints     : List<WaypointRequestDto> = emptyList()
)

@Serializable
data class WaypointDto(
    @SerialName("id")               val id              : Int,
    @SerialName("route_id")         val routeId         : Int,
    @SerialName("address")          val address         : String,
    @SerialName("latitud")          val latitude        : Double?,
    @SerialName("longitud")         val longitude       : Double?,
    @SerialName("order_sequence")   val orderSequence   : Int,
    @SerialName("client")           val client          : ClientDto,
    @SerialName("status")           val status          : String,
    @SerialName("visited_at")       val visitedAt       : String?,
    @SerialName("comment")          val comment         : String?
)

@Serializable
data class WaypointRequestDto(
    @SerialName("client_id")        val clientId        : Int,
    @SerialName("address")          val address         : String,
    @SerialName("order_sequence")   val orderSequence   : Int,
    @SerialName("latitud")          val latitude        : Double?,
    @SerialName("longitud")         val longitude       : Double?
)

@Serializable
data class WaypointStatusRequestDto(
    @SerialName("status")           val status          : String,
    @SerialName("comment")          val comment         : String?
)