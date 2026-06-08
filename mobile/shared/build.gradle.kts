import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Lee gradle.properties de forma segura
fun getProp(key: String): String =
    project.findProperty(key)?.toString()
        ?: error("Propiedad '$key' no encontrada en gradle.properties")

val apiBaseUrlDebug   = getProp("API_BASE_URL_DEBUG")
val apiBaseUrlRelease = getProp("API_BASE_URL_RELEASE")
val connectTimeout    = getProp("API_CONNECT_TIMEOUT")
val readTimeout       = getProp("API_READ_TIMEOUT")
val writeTimeout      = getProp("API_WRITE_TIMEOUT")

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.pluginSerialization)
}

compose.resources {
    publicResClass = true
}

// ── Registro del directorio generado como fuente ──────────────────────────
kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/kotlin"))
            kotlin.srcDir(layout.buildDirectory.dir("generated/compose/resourceGenerator/kotlin/commonResClass"))
            kotlin.srcDir(layout.buildDirectory.dir("generated/compose/resourceGenerator/kotlin/commonMainResourceAccessors"))
        }
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    androidLibrary {
       namespace = "org.smlpartners.smlgo.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(libs.play.services.maps)
            implementation(libs.maps.compose)
            implementation(libs.play.services.location)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            //Implementacion de la libreria ktor-client para consumir servicios
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            //Implementacion de la libreria kotlinx-serialization para convertir los datos
            implementation(libs.kotlinx.serialization)
            //Implementacion de la libreria para la inyeccion de dependencias
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            //Implementacion de la libreria para formatear la hora/fecha
            implementation(libs.kotlinx.datetime)
            //Implementacion de la libreria para multiplataform-settings
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.noarg)
            //Implementacion de la libreria androidx security
            implementation(libs.androidx.security.crypto)
            //Implementacion de la libreria de Iconos
            implementation(libs.compose.material.icons)
            implementation(libs.compose.material.icons.extended)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// ── Tarea que genera BuildConfig.kt ───────────────────────────────────────
val generateBuildConfig by tasks.registering {
    description = "Generacion del archivo BuildConfig"
    val outputDir = layout.buildDirectory.dir("generated/kotlin/com/smlpartners/smlgo/core")
    outputs.dir(outputDir)

    doLast {
        val isDebug = gradle.startParameter.taskNames.any {
            it.contains("debug", ignoreCase = true)
        }
        val url = if (isDebug) apiBaseUrlDebug else apiBaseUrlRelease

        outputDir.get().asFile.mkdirs()

        File(outputDir.get().asFile, "BuildConfig.kt").writeText(
            """
            package com.smlpartners.smlgo.core

            object BuildConfig {
                const val BASE_URL        = "$url"
                const val CONNECT_TIMEOUT = ${connectTimeout}L
                const val READ_TIMEOUT    = ${readTimeout}L
                const val WRITE_TIMEOUT   = ${writeTimeout}L
                const val IS_DEBUG        = $isDebug
            }
            """.trimIndent()
        )
        println("[BuildConfig] Generado → BASE_URL=$url | IS_DEBUG=$isDebug")
    }
}

// ── Hace que cualquier compilación dependa de la tarea ────────────────────
tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
    dependsOn(generateBuildConfig)
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}