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
            //Implementacion de la libreria kotlinx-serialization para convertir los datos
            implementation(libs.kotlinx.serialization)
            //Implementacion de la libreria para la inyeccion de dependencias
            implementation(libs.koin.core)
            //Implementacion de la libreria para formatear la hora/fecha
            implementation(libs.kotlinx.datetime)
            //Implementacion de la libreria para multiplataform-settings
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.noarg)

            // Genera constantes accesibles desde commonMain
            kotlin.sourceSets.commonMain {
                kotlin.srcDir(
                    layout.buildDirectory.dir("generated/kotlin")
                )
            }

            tasks.register("generateBuildConfig") {
                val outputDir = layout.buildDirectory.dir("generated/kotlin/com/smlpartners/smlgo/core")
                outputs.dir(outputDir)
                doLast {
                    val isDebug = gradle.startParameter.taskNames.any { it.contains("debug", ignoreCase = true) }
                    val url = if (isDebug) apiBaseUrlDebug else apiBaseUrlRelease
                    outputDir.get().asFile.mkdirs()
                    File(outputDir.get().asFile, "BuildConfig.kt").writeText("""
                        package com.smlpartners.smlgo.core
                        
                        object BuildConfig {
                            const val BASE_URL         = "$url"
                            const val CONNECT_TIMEOUT  = ${connectTimeout}L
                            const val READ_TIMEOUT     = ${readTimeout}L
                            const val WRITE_TIMEOUT    = ${writeTimeout}L
                            const val IS_DEBUG         = $isDebug
                        }
                    """.trimIndent())
                            }
                        }
            tasks.matching { it.name.contains("compileKotlin") }.configureEach {
                dependsOn("generateBuildConfig")
            }
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}