/*
 * Copyright 2022-2023 Mikhail Titov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.d1s.dsn.util

import dev.d1s.dsn.service.GroupChatService
import dev.inmo.tgbotapi.extensions.api.chat.get.getChatAdministrators
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.PreviewFeature
import dev.inmo.tgbotapi.utils.RiskFeature

suspend fun <BC : BehaviourContext> BC.onCommand(botCommand: BotCommand, receiver: suspend BC.(TextMessage) -> Unit) {
    onCommand(botCommand.command, scenarioReceiver = receiver)
}

suspend fun <BC : BehaviourContext> BC.requireInitializedGroupChat(
    groupChatService: GroupChatService,
    message: TextMessage,
    block: suspend BC.() -> Unit
) {
    val initializedGroupChat = groupChatService.getGroupChatInfo() ?: run {
        commandNotAvailable(message)

        return
    }

    val thisChatId = message.chat.id.chatId

    if (initializedGroupChat.groupChatId.chatId == thisChatId) {
        block()
    } else {
        val wrongChatContent = makeTitle(Emoji.CROSS_MARK, "Этот чат не подходит для исполнения запрашиваемой команды.")

        reply(message, wrongChatContent)
    }
}

suspend fun <BC : BehaviourContext> BC.requireInitializedGroupChatAndAdmin(
    groupChatService: GroupChatService,
    message: TextMessage,
    block: suspend BC.() -> Unit
) {
    requireInitializedGroupChat(groupChatService, message) {
        requireAdmin(message, block)
    }
}

suspend fun <BC : BehaviourContext> BC.requireGroupChatAndAdmin(
    message: TextMessage,
    block: suspend BC.() -> Unit
) {
    requireGroupChat(message) {
        requireAdmin(message, block)
    }
}

private suspend inline fun <BC : BehaviourContext> BC.requireGroupChat(message: TextMessage, block: BC.() -> Unit) {
    if (message.chat !is GroupChat) {
        val notGroupChatContent = makeTitle(Emoji.CROSS_MARK, "Это не групповой чат.")

        reply(message, notGroupChatContent)
    } else {
        block()
    }
}

private suspend fun <BC : BehaviourContext> BC.requireAdmin(
    message: TextMessage,
    block: suspend BC.() -> Unit
) {
    if (isFromAdmin(message)) {
        block()
    } else {
        val noPermissionContent = makeTitle(Emoji.CROSS_MARK, "Нет привилегий.")

        reply(message, noPermissionContent)
    }
}

@OptIn(PreviewFeature::class, RiskFeature::class)
private suspend fun BehaviourContext.isFromAdmin(
    message: TextMessage
): Boolean {
    val chat = message.chat.asPublicChat() ?: error("Not a public chat")
    val user = message.from?.id ?: error("Not a user")
    val administrators = this.getChatAdministrators(chat).map { it.user.id }

    return user in administrators
}

private suspend fun <BC : BehaviourContext> BC.commandNotAvailable(message: TextMessage) {
    val commandNotAvailableContent = makeTitle(Emoji.CROSS_MARK, "Команда еще не доступа.")

    reply(message, commandNotAvailableContent)
}