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
import dev.d1s.dsn.entity.GroupChatInfo
import dev.d1s.dsn.util.setAndPersist
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GroupChatService {

    suspend fun getGroupChatInfo(): GroupChatInfo?

    suspend fun isGroupChatInfoInitialized(): Boolean

    suspend fun setGroupChatInfo(groupChatInfo: GroupChatInfo)
}

class GroupChatServiceImpl : GroupChatService, KoinComponent {

    private val redisFactory by inject<RedisClientFactory>()

    private val redis by lazy {
        redisFactory.redis
    }

    override suspend fun getGroupChatInfo(): GroupChatInfo? {
        val rawGroupChatInfo = this.getRawGroupChatInfo()

        return rawGroupChatInfo?.let {
            GroupChatInfo.deserialize(it)
        }
    }

    override suspend fun isGroupChatInfoInitialized(): Boolean =
        this.getGroupChatInfo() != null

    override suspend fun setGroupChatInfo(groupChatInfo: GroupChatInfo) {
        val serializedGroupChatInfo = groupChatInfo.serialize()

        setRawGroupChatInfo(serializedGroupChatInfo)
    }

    private suspend fun getRawGroupChatInfo() =
        redis.get(Key.GROUP_CHAT_INFO)

    private suspend fun setRawGroupChatInfo(rawGroupChatInfo: String) =
        redis.setAndPersist(Key.GROUP_CHAT_INFO, rawGroupChatInfo)
}