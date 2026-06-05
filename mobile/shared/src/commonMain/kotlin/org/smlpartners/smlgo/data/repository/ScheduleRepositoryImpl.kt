package org.smlpartners.smlgo.data.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.core.network.safeApiCall
import org.smlpartners.smlgo.data.mapper.toDomain
import org.smlpartners.smlgo.data.remote.api.ScheduleApiService
import org.smlpartners.smlgo.data.remote.dto.ClientScheduleRequestDto
import org.smlpartners.smlgo.domain.model.ClientSchedule
import org.smlpartners.smlgo.domain.repository.ScheduleRepository
import kotlinx.datetime.LocalDate

class ScheduleRepositoryImpl(
    private val api: ScheduleApiService
) : ScheduleRepository {

    override suspend fun getSchedules(): ApiResult<List<ClientSchedule>> =
        safeApiCall { api.getClientSchedules().map { it.toDomain() } }

    override suspend fun getSchedulesByWeek(
        start: LocalDate,
        end: LocalDate
    ): ApiResult<List<ClientSchedule>> =
        safeApiCall {
            api.getClientSchedulesByWeek(start.toString(), end.toString()).map { it.toDomain() }
        }

    override suspend fun createSchedule(
        clientId: Int,
        day: String,
        startTime: String,
        observation: String?
    ): ApiResult<ClientSchedule> =
        safeApiCall {
            api.createClientSchedule(
                ClientScheduleRequestDto(
                    clientId    = clientId,
                    day         = day,
                    startTime   = startTime,
                    observation = observation
                )
            ).toDomain()
        }

    override suspend fun deleteSchedule(id: Int): ApiResult<Unit> =
        safeApiCall { api.deleteClientSchedule(id) }
}
