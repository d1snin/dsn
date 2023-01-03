/*
 * Copyright 2022-2023 Mikhail Titov
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

package dev.d1s.dsn.util

import dev.d1s.dsn.entity.DutyPair
import dev.d1s.dsn.service.DutyPairService
import dev.inmo.tgbotapi.utils.*

fun EntitiesBuilder.formatDutyPair(dutyPair: DutyPair) {
    bold("1.")
    regularln(" ${dutyPair.firstStudent}")

    bold("2.")
    regular(" ${dutyPair.secondStudent}")
}

fun EntitiesBuilder.formatDutyPairs(dutyPairs: List<DutyPair>, indexed: Boolean = false) {
    dutyPairs.forEachIndexed { index, dutyPair ->
        if (indexed) {
            boldln("${index + 1})")
        }
        formatDutyPair(dutyPair)
        doubleNewLine()
    }
}

suspend fun EntitiesBuilder.formatCurrentDutyPair(dutyPairService: DutyPairService) {
    val currentDutyPair = dutyPairService.getCurrentDutyPair()

    formatDutyPair(currentDutyPair)
}

fun EntitiesBuilder.doubleNewLine() {
    regular("\n\n")
}