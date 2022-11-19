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

import dev.d1s.dsn.config.ApplicationConfig
import dev.d1s.dsn.service.GroupChatService
import dev.d1s.dsn.util.*
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.content.TextMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GetCommand : Command, KoinComponent {

    override val name = "pairs"

    override val description = "Показать все дежурные пары."

    private val config by inject<ApplicationConfig>()

    private val groupChatService by inject<GroupChatService>()

    override suspend fun BehaviourContext.onCommand(message: TextMessage) {
        requireInitializedGroupChat(groupChatService, message) {
            val dutyPairs = config.parsedDutyPairs

            val entities = withTitle(Emoji.SCROLL, "Полный список дежурных пар:") {
                dutyPairs.forEach { pair ->
                    formatDutyPair(pair)
                    doubleNewLine()
                }
            }

            reply(message, entities)
        }
    }
}