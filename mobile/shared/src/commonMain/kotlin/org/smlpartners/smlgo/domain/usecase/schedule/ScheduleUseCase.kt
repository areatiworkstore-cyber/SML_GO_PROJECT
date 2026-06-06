package org.smlpartners.smlgo.domain.usecase.schedule

import org.smlpartners.smlgo.core.network.ApiError
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.ClientSchedule
import org.smlpartners.smlgo.domain.repository.ScheduleRepository
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class GetSchedulesUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(): ApiResult<List<ClientSchedule>> =
        repository.getSchedules()
}

class GetWeeklySchedulesUseCase(private val repository: ScheduleRepository) {

    suspend operator fun invoke(
        referenceDate: LocalDate
    ): ApiResult<List<ClientSchedule>> {
        val monday = referenceDate.startOfWeek()
        val sunday = monday.plus(DatePeriod(days = 6))
        return repository.getSchedulesByWeek(monday, sunday)
    }

    suspend fun groupedByDay(
        referenceDate: LocalDate
    ): ApiResult<Map<LocalDate, List<ClientSchedule>>> =
        when (val result = invoke(referenceDate)) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.groupBy { it.day }
            )
            is ApiResult.Error   -> result
        }

    private fun LocalDate.startOfWeek(): LocalDate {
        val daysFromMonday = dayOfWeek.ordinal
        return minus(DatePeriod(days = daysFromMonday))
    }
}

class CreateScheduleUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(
        clientId    : Int,
        day         : LocalDate,
        startTime   : String,
        observation : String?
    ): ApiResult<ClientSchedule> {
        if (startTime.isBlank()) return ApiResult.Error(
            ApiError.UnknownError("La hora de inicio es obligatoria")
        )
        return repository.createSchedule(
            clientId    = clientId,
            day         = day.toString(),
            startTime   = startTime,
            observation = observation
        )
    }
}

class DeleteScheduleUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(id: Int): ApiResult<Unit> =
        repository.deleteSchedule(id)
}