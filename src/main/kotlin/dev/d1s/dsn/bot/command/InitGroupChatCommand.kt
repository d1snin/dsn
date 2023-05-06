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

import dev.d1s.dsn.entity.GroupChatInfo
import dev.d1s.dsn.entity.UserAuthenticationToken
import dev.d1s.dsn.service.AuthenticationService
import dev.d1s.dsn.service.GroupChatService
import dev.d1s.dsn.util.Emoji
import dev.d1s.dsn.util.makeTitle
import dev.d1s.dsn.util.requireGroupChatAndAdmin
import dev.inmo.tgbotapi.abstracts.FromUser
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InitGroupChatCommand : Command, KoinComponent {

    override val name = "init"

    override val description = "Инициализировать чат."

    private val groupChatService by inject<GroupChatService>()

    private val authenticationService by inject<AuthenticationService>()

    @OptIn(PreviewFeature::class)
    override suspend fun BehaviourContext.onCommand(message: TextMessage) {
        requireGroupChatAndAdmin(message) {
            if (groupChatService.isGroupChatInfoInitialized()) {
                replyChatAlreadyInitialized(message)

                return@requireGroupChatAndAdmin
            }

            val user = message.fromUserOrNull() ?: run {
                replyInvalidUser(message)

                return@requireGroupChatAndAdmin
            }

            val (token, authKeyMessage) = retrieveAuthToken(message, user)

            val authenticationResult = authenticationService.authenticate(token)

            if (!authenticationResult.authenticated) {
                replyInvalidToken(authKeyMessage)

                return@requireGroupChatAndAdmin
            }

            val groupChatInfo = GroupChatInfo(message.chat.id)

            groupChatService.setGroupChatInfo(groupChatInfo)

            val chatInitializedContent = makeTitle(Emoji.CHECK_MARK, "Чат инициализирован.")

            reply(message, chatInitializedContent)
        }
    }

    private suspend fun BehaviourContext.replyChatAlreadyInitialized(message: TextMessage) {
        val chatAlreadyInitializedContent = makeTitle(Emoji.CROSS_MARK, "Чат уже инициализирован.")

        reply(message, chatAlreadyInitializedContent)
    }

    private suspend fun BehaviourContext.replyInvalidUser(message: TextMessage) {
        val invalidUserContent = makeTitle(Emoji.CROSS_MARK, "Невалидный пользователь.")

        reply(message, invalidUserContent)
    }

    @OptIn(PreviewFeature::class)
    private suspend fun BehaviourContext.retrieveAuthToken(
        contextMessage: TextMessage,
        user: FromUser
    ): Pair<UserAuthenticationToken, TextMessage> {
        val userId = user.user.id

        val sendAuthKeyContent = makeTitle(Emoji.LOCKED_WITH_KEY, "Отправьте авторизационный ключ.")

        reply(contextMessage, sendAuthKeyContent)

        val authKeyMessage = waitTextMessage().filter { message ->
            message.fromUserOrNull()?.from?.id == userId
        }.first()

        val authKey = authKeyMessage.content.text

        return UserAuthenticationToken(authKey) to authKeyMessage
    }

    private suspend fun BehaviourContext.replyInvalidToken(message: TextMessage) {
        val invalidKeyContent = makeTitle(Emoji.CROSS_MARK, "Невалидный ключ.")

        reply(message, invalidKeyContent)
    }
}