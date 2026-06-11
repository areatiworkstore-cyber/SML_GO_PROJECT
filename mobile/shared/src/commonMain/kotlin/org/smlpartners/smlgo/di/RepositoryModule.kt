package org.smlpartners.smlgo.di

import org.smlpartners.smlgo.data.remote.api.*
import org.smlpartners.smlgo.data.repository.*
import org.smlpartners.smlgo.domain.repository.*
import org.koin.dsl.module

val repositoryModule = module {

    // ── API Services (reciben HttpClientManager para usar siempre el cliente activo) ──
    single { AuthApiService(get()) }
    single { ClientApiService(get()) }
    single { GeographyApiService(get()) }
    single { MasterDataApiService(get()) }
    single { RoleApiService(get()) }
    single { RouteApiService(get()) }
    single { ScheduleApiService(get()) }
    single { WaypointApiService(get()) }

    // ── Repositories ─────────────────────────────────────────────────
    single<AuthRepository>       { AuthRepositoryImpl(get(), get(), get()) }
    single<ClientRepository>     { ClientRepositoryImpl(get()) }
    single<GeographyRepository>  { GeographyRepositoryImpl(get()) }
    single<MasterDataRepository> { MasterDataRepositoryImpl(get()) }
    single<RoleRepository>       { RoleRepositoryImpl(get()) }
    single<RouteRepository>      { RouteRepositoryImpl(get()) }
    single<ScheduleRepository>   { ScheduleRepositoryImpl(get()) }
    single<WaypointRepository>   { WaypointRepositoryImpl(get()) }
}