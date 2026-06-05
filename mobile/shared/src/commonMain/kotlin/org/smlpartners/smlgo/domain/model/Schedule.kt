// domain/model/ClientSchedule.kt
package org.smlpartners.smlgo.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class ClientSchedule(
    val id         : Int,
    val client     : Client,
    val day        : LocalDate,
    val startTime  : LocalTime,
    val observation: String?,
    val active     : Boolean
)