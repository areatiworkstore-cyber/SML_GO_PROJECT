package org.smlpartners.smlgo.di

import org.koin.compose.viewmodel.dsl.viewModel
import org.smlpartners.smlgo.ui.clients.ClientViewModel
import org.smlpartners.smlgo.ui.dashboard.DashboardViewModel
import org.smlpartners.smlgo.ui.auth.LoginViewModel
import org.smlpartners.smlgo.ui.profile.ProfileViewModel
import org.smlpartners.smlgo.ui.routes.RouteViewModel
import org.smlpartners.smlgo.ui.schedule.ScheduleViewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { ClientViewModel(get(), get(), get(), get(), get()) }
    viewModel { RouteViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ScheduleViewModel(get(), get(), get(), get()) }
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
}