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

package dev.d1s.dsn

import dev.d1s.dsn.bot.TelegramBot
import dev.d1s.dsn.database.RedisClientFactory
import dev.d1s.dsn.service.SchedulingService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

class DsnApplication : KoinComponent {

    private val log = logging()

    private val redisClientFactory by inject<RedisClientFactory>()

    private val telegramBot by inject<TelegramBot>()

    private val schedulingService by inject<SchedulingService>()

    suspend fun run() {
        log.i {
            "DSN is starting..."
        }

        redisClientFactory.connect()

        val job = telegramBot.startTelegramBot()

        schedulingService.scheduleJobs()

        log.i {
            "DSN is ready!"
        }

        job.join()
    }
}