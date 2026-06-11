package org.smlpartners.smlgo.di

import org.smlpartners.smlgo.domain.usecase.auth.*
import org.smlpartners.smlgo.domain.usecase.client.*
import org.smlpartners.smlgo.domain.usecase.masterdata.*
import org.smlpartners.smlgo.domain.usecase.route.*
import org.smlpartners.smlgo.domain.usecase.schedule.*
import org.smlpartners.smlgo.domain.usecase.waypoint.*
import org.smlpartners.smlgo.domain.usecase.role.*
import org.smlpartners.smlgo.domain.usecase.geography.*
import org.koin.dsl.module

val useCaseModule = module {

    // ── Auth ──────────────────────────────────────────────────────────
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { IsLoggedInUseCase(get()) }

    // ── Next Code Client ──────────────────────────────────────────────
    factory { GetNextClientCodeUseCase(get()) }

    // ── Client ────────────────────────────────────────────────────────
    factory { GetClientsUseCase(get()) }
    factory { GetClientByIdUseCase(get()) }
    factory { GetClientsWithLocationUseCase(get()) }
    factory { CreateClientUseCase(get()) }
    factory { UpdateClientUseCase(get()) }

    // ── Route ─────────────────────────────────────────────────────────
    factory { GetRoutesUseCase(get()) }
    factory { GetRouteByIdUseCase(get()) }
    factory { CreateRouteUseCase(get(), get()) }
    factory { UpdateRouteUseCase(get()) }
    factory { DeleteRouteUseCase(get()) }
    factory { CreateRouteWithWaypointsUseCase(get(), get()) }

    // ── Waypoint ──────────────────────────────────────────────────────
    factory { CreateWaypointUseCase(get()) }
    factory { UpdateWaypointStatusUseCase(get()) }

    // ── Schedule ──────────────────────────────────────────────────────
    factory { GetSchedulesUseCase(get()) }
    factory { GetWeeklySchedulesUseCase(get()) }
    factory { CreateScheduleUseCase(get()) }
    factory { DeleteScheduleUseCase(get()) }

    // ── Master data ───────────────────────────────────────────────────
    factory { GetDocumentTypesUseCase(get()) }
    factory { GetBusinessTypesUseCase(get()) }
    factory { GetClientGroupsUseCase(get()) }
    factory { GetRolesUseCase(get()) }
    factory { GetDepartmentsUseCase(get()) }
    factory { GetProvincesUseCase(get()) }
    factory { GetDistrictsUseCase(get()) }
    factory { GetClientFormMasterDataUseCase(get()) }
    factory { GetProfileMasterDataUseCase(get(), get()) }
}