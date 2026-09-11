package org.smlpartners.smlgo.core.security

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class, ExperimentalSettingsImplementation::class)
actual fun createSettings(): Settings = KeychainSettings("org.smlpartners.smlgo.secure")