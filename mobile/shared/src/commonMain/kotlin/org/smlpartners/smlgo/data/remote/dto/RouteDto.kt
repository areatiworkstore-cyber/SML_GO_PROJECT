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
data class RouteCreateDto(
    @SerialName("name")             val name          : String,
    @SerialName("scheduled_date")   val scheduledDate : String,  // "2024-01-15"
    @SerialName("user_id")          val userId        : Int,
    @SerialName("waypoint_ids")     val waypointIds   : List<Int> = emptyList(),
    @SerialName("active")           val active        : Boolean  = true
)

@Serializable
data class RouteUpdateDto(
    @SerialName("name")             val name          : String?  = null,
    @SerialName("scheduled_date")   val scheduledDate : String?  = null,
    @SerialName("user_id")          val userId        : Int?     = null,
    @SerialName("active")           val active        : Boolean? = null
)