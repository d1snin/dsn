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

import dev.d1s.dsn.entity.DutyPairIndex
import dev.d1s.dsn.service.DutyPairService
import dev.d1s.dsn.service.GroupChatService
import dev.d1s.dsn.util.*
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.types.message.content.TextMessage
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SetDutyPairCommand : Command, KoinComponent {

    override val name = "set"

    override val description = "Установить текущую дежурную пару."

    private val groupChatService by inject<GroupChatService>()

    private val dutyPairService by inject<DutyPairService>()

    override suspend fun BehaviourContext.onCommand(message: TextMessage) {
        requireOwner(groupChatService, message) {
            val dutyPairs = dutyPairService.getDutyPairs()

            val selectDutyPair = withTitle(Emoji.ARROW_DOWN, "Выберите дежурную пару.") {
                formatDutyPairs(dutyPairs, indexed = true)
            }

            reply(message, selectDutyPair)

            val indexMessage = waitTextMessage().first()

            val index = indexMessage.content.text.trim().toIntOrNull()?.minus(1)

            val dutyPair = index?.let {
                dutyPairs.getOrNull(it)
            } ?: run {
                val invalidDutyPair = makeTitle(Emoji.CROSS_MARK, "Невалидный номер.")

                reply(message, invalidDutyPair)

                return@requireOwner
            }

            dutyPairService.setCurrentDutyPair(DutyPairIndex(index))

            val currentDutyPair = withTitle(Emoji.CHECK_MARK, "Текущая дежурная пара") {
                formatDutyPair(dutyPair)
            }

            reply(indexMessage, currentDutyPair)
        }
    }
}