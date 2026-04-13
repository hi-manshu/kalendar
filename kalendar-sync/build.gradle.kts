/*
 *
 *  * Copyright 2026 Kalendar Contributors (https://www.himanshoe.com). All rights reserved.
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.publish)
    alias(libs.plugins.dokka)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }
    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "kalendar-sync"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
    }
}

android {
    namespace = "com.himanshoe.kalendar.sync"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

val detekt by configurations.creating
dependencies {
    detekt(libs.detekt.cli)
    detekt(libs.detekt.formatting)
    dokkaPlugin(libs.dokka)
}
tasks.register<JavaExec>("detekt") {
    mainClass = "io.gitlab.arturbosch.detekt.cli.Main"
    classpath = detekt

    val input = projectDir
    val config = "$projectDir/detekt.yml"
    val exclude = ".*/build/.*,.*/resources/.*"
    val report = "sarif:${layout.buildDirectory.file("reports/detekt/detekt.sarif").get()}"
    val params = listOf("-i", input, "-c", config, "-ex", exclude, "-r", report, "--build-upon-default-config")
    args(params)
}
