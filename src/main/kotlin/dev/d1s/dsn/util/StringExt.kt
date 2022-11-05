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

package dev.d1s.dsn.util

import dev.d1s.dsn.entity.DutyPair

private const val PAIR_SEPARATOR = ";"
private const val VALUE_SEPARATOR = ","

fun String.parseDutyPairs() = this.split(PAIR_SEPARATOR).map { rawPair ->
    val rawValues = rawPair.split(VALUE_SEPARATOR, limit = 2)

    DutyPair(
        firstStudent = rawValues[0],
        secondStudent = rawValues[1]
    )
}