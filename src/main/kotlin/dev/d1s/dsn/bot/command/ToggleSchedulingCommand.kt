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

package dev.d1s.dsn.bot.command

import dev.d1s.dsn.job.AnnounceDutyPairJob
import dev.d1s.dsn.service.GroupChatService
import dev.d1s.dsn.service.SchedulingService
import dev.d1s.dsn.util.Emoji
import dev.d1s.dsn.util.makeTitle
import dev.d1s.dsn.util.requireInitializedGroupChatAndAdmin
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.content.TextMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean

class ToggleSchedulingCommand : Command, KoinComponent {

    override val name = "toggle"

    override val description = "Включить/выключить автоматическое переключение дежурных пар."

    private val paused = AtomicBoolean(false)

    private val groupChatService by inject<GroupChatService>()

    private val schedulingService by inject<SchedulingService>()

    override suspend fun BehaviourContext.onCommand(message: TextMessage) {
        requireInitializedGroupChatAndAdmin(groupChatService, message) {
            if (paused.get()) {
                schedulingService.resumeJob(AnnounceDutyPairJob.key)
                paused.set(false)

                val content = makeTitle(Emoji.CHECK_MARK, "Автоматическое переключение дежурных включено.")

                reply(message, content)
            } else {
                schedulingService.pauseJob(AnnounceDutyPairJob.key)
                paused.set(true)

                val content = makeTitle(Emoji.CHECK_MARK, "Автоматическое переключение дежурных выключено.")

                reply(message, content)
            }
        }
    }
}
