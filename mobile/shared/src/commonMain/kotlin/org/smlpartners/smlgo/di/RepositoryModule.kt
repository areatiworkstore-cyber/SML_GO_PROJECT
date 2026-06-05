package org.smlpartners.smlgo.di

import org.smlpartners.smlgo.data.remote.api.*
import org.smlpartners.smlgo.data.repository.*
import org.smlpartners.smlgo.domain.repository.*
import io.ktor.client.HttpClient
import org.koin.dsl.module

val repositoryModule = module {

    // ── API Services ─────────────────────────────────────────────────
    single { AuthApiService(get<HttpClient>()) }
    single { ClientApiService(get<HttpClient>()) }
    single { GeographyApiService(get<HttpClient>()) }
    single { MasterDataApiService(get<HttpClient>()) }
    single { RoleApiService(get<HttpClient>()) }
    single { RouteApiService(get<HttpClient>()) }
    single { ScheduleApiService(get<HttpClient>()) }
    single { SupplierApiService(get<HttpClient>()) }
    single { WaypointApiService(get<HttpClient>()) }

    // ── Repositories ─────────────────────────────────────────────────
    single<AuthRepository>       { AuthRepositoryImpl(get(), get()) }
    single<ClientRepository>     { ClientRepositoryImpl(get()) }
    single<GeographyRepository>  { GeographyRepositoryImpl(get()) }
    single<MasterDataRepository> { MasterDataRepositoryImpl(get()) }
    single<RoleRepository>       { RoleRepositoryImpl(get()) }
    single<RouteRepository>      { RouteRepositoryImpl(get()) }
    single<ScheduleRepository>   { ScheduleRepositoryImpl(get()) }
    single<SupplierRepository>   { SupplierRepositoryImpl(get()) }
    single<WaypointRepository>   { WaypointRepositoryImpl(get()) }
}