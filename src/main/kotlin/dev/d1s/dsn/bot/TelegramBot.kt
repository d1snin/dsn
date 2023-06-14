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

package dev.d1s.dsn.bot

import dev.d1s.dsn.bot.command.CommandHolder
import dev.d1s.dsn.config.ApplicationConfig
import dev.d1s.dsn.util.onCommand
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.bot.exceptions.CommonBotException
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.createSubContext
import io.ktor.client.plugins.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

interface TelegramBot {

    val requestExecutor: RequestsExecutor

    suspend fun startTelegramBot(): Job

    suspend fun withBehaviourContext(block: suspend BehaviourContext.() -> Unit)
}

class TelegramBotImpl : TelegramBot, KoinComponent {

    override val requestExecutor
        get() = internalRequestExecutor ?: error("Request executor is not yet initialized.")

    private val config by inject<ApplicationConfig>()

    private val commandHolder by inject<CommandHolder>()

    private val log = logging()

    private var internalRequestExecutor: RequestsExecutor? = null

    private var behaviourContext: BehaviourContext? = null

    override suspend fun startTelegramBot(): Job {
        log.i {
            "Starting DSN Telegram bot..."
        }

        internalRequestExecutor?.let {
            error("Request executor has already started.")
        }

        val token = config.bot.token

        val bot = telegramBot(token)

        internalRequestExecutor = bot

        val job = bot.buildBehaviourWithLongPolling(defaultExceptionsHandler = ::handleException) {
            behaviourContext = this

            configureCommands()
        }

        return job
    }

    override suspend fun withBehaviourContext(block: suspend BehaviourContext.() -> Unit) {
        val context = behaviourContext?.createSubContext() ?: error("Behaviour context is not available")

        block(context)
    }

    private suspend fun BehaviourContext.configureCommands() {
        val botCommands = commandHolder.botCommands

        setMyCommands(botCommands)

        configureCommandHandlers()
    }

    private suspend fun BehaviourContext.configureCommandHandlers() {
        val commands = commandHolder.commands

        commands.forEach { command ->
            onCommand(command.toBotCommand()) { message ->
                val context = this

                with(command) {
                    context.onCommand(message)
                }
            }
        }
    }

    private fun handleException(throwable: Throwable) {
        when (throwable) {
            is HttpRequestTimeoutException -> {
                log.d {
                    "Request timed out."
                }
            }

            is CommonBotException -> {
                log.d {
                    "Something went wrong: ${throwable.cause?.message ?: "no message"}"
                }
            }

            is CancellationException -> {
                // ignore
            }

            else -> {
                log.e(throwable) {
                    "An error occurred."
                }
            }
        }
    }
}