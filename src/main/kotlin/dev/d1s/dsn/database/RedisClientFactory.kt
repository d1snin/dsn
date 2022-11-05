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

package dev.d1s.dsn.database

import dev.d1s.dsn.config.ApplicationConfig
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

typealias RedisClient = KredsClient

interface RedisClientFactory {

    val redis: RedisClient

    suspend fun connect()
}

class RedisClientFactoryImpl : RedisClientFactory, KoinComponent {

    private val config by inject<ApplicationConfig>()

    private val log = logging()

    private var internalRedis: RedisClient? = null

    override val redis get() = internalRedis ?: error("Redis client is not initialized.")

    override suspend fun connect() {
        val endpoint = Endpoint.from(config.redis.endpoint)

        log.i {
            "Connecting to Redis endpoint $endpoint"
        }

        val client = newClient(endpoint)

        val clientId = client.clientId()

        log.i {
            "Connected to Redis endpoint with client ID $clientId"
        }

        internalRedis = client
    }
}