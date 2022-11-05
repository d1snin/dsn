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

package dev.d1s.dsn.service

import dev.d1s.dsn.entity.AuthenticationResult
import dev.d1s.dsn.entity.Token
import dispatch.core.withIO
import org.koin.core.component.KoinComponent
import org.lighthousegames.logging.logging
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

interface AuthenticationService {

    suspend fun initTokenFile()

    suspend fun authenticate(authenticationToken: Token): AuthenticationResult
}

class AuthenticationServiceImpl : AuthenticationService, KoinComponent {

    private val tokenFilePath = Paths.get(TOKEN_FILE)

    private val log = logging()

    override suspend fun initTokenFile() {
        log.i {
            "Initializing token file..."
        }

        withIO {
            if (!Files.exists(tokenFilePath)) {
                writeToken()
            }
        }
    }

    override suspend fun authenticate(authenticationToken: Token): AuthenticationResult {
        val realToken = getRawToken()

        return AuthenticationResult(authenticationToken.token == realToken)
    }

    private suspend fun getRawToken() = withIO {
        @Suppress("BlockingMethodInNonBlockingContext")
        Files.readString(tokenFilePath)
    }

    private suspend fun writeToken() {
        withIO {
            val uuid = generateUuid()

            @Suppress("BlockingMethodInNonBlockingContext")
            Files.write(tokenFilePath, uuid)
        }
    }

    private fun generateUuid() = UUID.randomUUID().toString().toByteArray()

    private companion object {

        private const val TOKEN_FILE = "token.txt"
    }
}