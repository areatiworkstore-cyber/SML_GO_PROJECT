package org.smlpartners.smlgo.domain.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.ClientSchedule
import kotlinx.datetime.LocalDate

interface ScheduleRepository {
    suspend fun getSchedules(): ApiResult<List<ClientSchedule>>
    suspend fun getSchedulesByWeek(
        start : LocalDate,
        end   : LocalDate
    ): ApiResult<List<ClientSchedule>>
    suspend fun createSchedule(
        clientId    : Int,
        day         : String,
        startTime   : String,
        observation : String?
    ): ApiResult<ClientSchedule>
    suspend fun deleteSchedule(id: Int): ApiResult<Unit>
}