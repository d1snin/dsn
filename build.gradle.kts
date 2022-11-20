/*
 * Copyright 2022 Mikhail Titov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    application
}

val projectGroup: String by project
val projectVersion: String by project

group = projectGroup
version = projectVersion

application {
    mainClass.set("dev.d1s.dsn.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    val logbackVersion: String by project
    val kmLogVersion: String by project

    val kredsVersion: String by project

    val tgbotapiVersion: String by project

    val hopliteVersion: String by project

    val koinVersion: String by project

    val dispatchVersion: String by project

    val quartzVersion: String by project

    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.lighthousegames:logging:$kmLogVersion")

    implementation("io.github.crackthecodeabhi:kreds:$kredsVersion")

    implementation("dev.inmo:tgbotapi:$tgbotapiVersion")

    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")

    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    implementation("com.rickbusarow.dispatch:dispatch-core:$dispatchVersion")

    implementation("org.quartz-scheduler:quartz:$quartzVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
    }
}