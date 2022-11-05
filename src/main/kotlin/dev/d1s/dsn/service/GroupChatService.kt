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

import dev.d1s.dsn.database.Key
import dev.d1s.dsn.database.RedisClientFactory
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.UserId
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GroupChatService {

    suspend fun getGroupChat(): ChatId?

    suspend fun isGroupChatInitialized(): Boolean

    suspend fun setGroupChat(chat: ChatId)

    suspend fun getOwner(): UserId?

    suspend fun isOwnerInitialized(): Boolean

    suspend fun setOwner(owner: UserId)
}

class GroupChatServiceImpl : GroupChatService, KoinComponent {

    private val redisFactory by inject<RedisClientFactory>()

    private val redis by lazy  {
        redisFactory.redis
    }

    override suspend fun getGroupChat(): ChatId? {
        val chatId = getGroupChatId()

        return chatId?.let {
            ChatId(it)
        }
    }

    override suspend fun isGroupChatInitialized() = getGroupChatId() != null

    override suspend fun setGroupChat(chat: ChatId) {
        setGroupChatId(chat.chatId)
    }

    override suspend fun getOwner(): UserId? {
        val ownerId = getOwnerId()

        return ownerId?.let {
            UserId(it)
        }
    }

    override suspend fun isOwnerInitialized() = getOwnerId() != null

    override suspend fun setOwner(owner: UserId) {
        setOwnerId(owner.chatId)
    }

    private suspend fun getGroupChatId() = redis.get(Key.GROUP_CHAT_ID)?.toLong()

    private suspend fun setGroupChatId(chatId: Long) {
        redis.set(Key.GROUP_CHAT_ID, chatId.toString())
    }

    private suspend fun getOwnerId() = redis.get(Key.OWNER_ID)?.toLong()

    private suspend fun setOwnerId(ownerId: Long) {
        redis.set(Key.OWNER_ID, ownerId.toString())
    }
}