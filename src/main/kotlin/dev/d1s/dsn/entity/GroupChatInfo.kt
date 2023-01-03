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

package dev.d1s.dsn.entity

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.UserId

private const val SEPARATOR = ":"

data class GroupChatInfo(
    val groupChatId: IdChatIdentifier,
    val ownerId: UserId
) {
    fun serialize() = "${groupChatId.chatId}$SEPARATOR${ownerId.chatId}"

    companion object {
        fun deserialize(groupChatInfo: String) =
            groupChatInfo.split(SEPARATOR, limit = 2).let { pieces ->
                GroupChatInfo(ChatId(pieces[0].toLong()), ChatId(pieces[1].toLong()))
            }
    }
}

fun GroupChatInfo?.orThrow() =
    this ?: error("Group chat is not initialized.")