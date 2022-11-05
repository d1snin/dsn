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

package dev.d1s.dsn.bot.command

import dev.d1s.dsn.di.Qualifier
import dev.inmo.tgbotapi.types.BotCommand
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface CommandHolder {

    val commands: List<Command>

    val botCommands: List<BotCommand>
}

class CommandHolderImpl : CommandHolder, KoinComponent {

    private val getCurrentDutyPairCommand by inject<Command>(Qualifier.GetCurrentDutyPairCommand)
    private val getDutyPairsCommand by inject<Command>(Qualifier.GetDutyPairsCommand)
    private val initCommand by inject<Command>(Qualifier.InitCommand)
    private val postponeCommand by inject<Command>(Qualifier.PostponeCommand)
    private val switchCommand by inject<Command>(Qualifier.SwitchCommand)

    override val commands by lazy {
        listOf(getCurrentDutyPairCommand, getDutyPairsCommand, initCommand, postponeCommand, switchCommand)
    }

    override val botCommands by lazy {
        commands.map {
            it.toBotCommand()
        }
    }
}