package org.smlpartners.smlgo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientScheduleDto(
    @SerialName("id")           val id          : Int,
    @SerialName("client")       val client      : ClientDto,
    @SerialName("day")          val day         : String,
    @SerialName("start_time")   val startTime   : String,
    @SerialName("observation")  val observation : String?,
    @SerialName("active")       val active      : Boolean
)

@Serializable
data class ClientScheduleRequestDto(
    @SerialName("client_id")    val clientId    : Int,
    @SerialName("day")          val day         : String,
    @SerialName("start_time")   val startTime   : String,
    @SerialName("observation")  val observation : String?
)