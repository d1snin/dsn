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

import dev.d1s.dsn.bot.TelegramBot
import dev.d1s.dsn.config.ApplicationConfig
import dev.d1s.dsn.database.Key
import dev.d1s.dsn.database.RedisClientFactory
import dev.d1s.dsn.entity.DutyPair
import dev.d1s.dsn.entity.DutyPairIndex
import dev.d1s.dsn.entity.orThrow
import dev.d1s.dsn.util.*
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DutyPairService {

    suspend fun getCurrentDutyPair(): DutyPair

    suspend fun postponeCurrentDutyPair()

    suspend fun switchDutyPair()
}

class DutyPairServiceImpl : DutyPairService, KoinComponent {

    private val firstDutyPair = DutyPairIndex(FIRST_DUTY_PAIR_INDEX)

    private val groupChatService by inject<GroupChatService>()

    private val redisFactory by inject<RedisClientFactory>()

    private val redis by lazy {
        redisFactory.redis
    }

    private val config by inject<ApplicationConfig>()

    private val bot by inject<TelegramBot>()

    override suspend fun getCurrentDutyPair(): DutyPair {
        val dutyPairIndex = getCurrentDutyPairIndex()

        val dutyPairs = config.parsedDutyPairs

        return dutyPairs.getOrNull(dutyPairIndex.index)
            ?: error("Index is out of bounds ($dutyPairIndex, total elements: ${dutyPairs.size})")
    }

    override suspend fun postponeCurrentDutyPair() {
        val currentDutyPairIndex = getCurrentDutyPairIndex()

        setPostponedDutyPairIndex(currentDutyPairIndex)
    }

    override suspend fun switchDutyPair() {
        val dutyPairIndex = getPostponedDutyPairIndex() ?: run {
            val currentDutyPairIndex = getCurrentDutyPairIndex()

            getNextDutyPairIndex(currentDutyPairIndex)
        }

        setCurrentDutyPairIndex(dutyPairIndex)

        announceDutyPair(dutyPairIndex)
    }

    private suspend fun announceDutyPair(dutyPairIndex: DutyPairIndex) {
        val chatId = groupChatService.getGroupChatInfo().orThrow().groupChatId

        val entities = withTitle(Emoji.REPEAT, "Дежурные на этот день:") {
            val dutyPair = config.parsedDutyPairs[dutyPairIndex.index]

            formatDutyPair(dutyPair)
        }

        bot.requestExecutor.sendMessage(chatId, entities)
    }

    private suspend fun getCurrentDutyPairIndex(): DutyPairIndex {
        val index = redis.get(Key.CURRENT_DUTY_PAIR_INDEX)?.toInt() ?: run {
            setCurrentDutyPairIndex(firstDutyPair)

            FIRST_DUTY_PAIR_INDEX
        }

        return DutyPairIndex(index)
    }

    private suspend fun setCurrentDutyPairIndex(index: DutyPairIndex) {
        redis.setAndPersist(Key.CURRENT_DUTY_PAIR_INDEX, index.toString())
    }

    private suspend fun getPostponedDutyPairIndex(): DutyPairIndex? {
        val index = redis.get(Key.POSTPONED_DUTY_PAIR_INDEX)?.toInt()

        return index?.let {
            DutyPairIndex(it)
        }
    }

    private suspend fun setPostponedDutyPairIndex(dutyPairIndex: DutyPairIndex) {
        redis.setAndPersist(Key.POSTPONED_DUTY_PAIR_INDEX, dutyPairIndex.toString())
    }

    private fun getNextDutyPairIndex(current: DutyPairIndex): DutyPairIndex {
        val dutyPairs = config.parsedDutyPairs

        val incrementedCurrent = current.index + 1

        val firstElement = 0

        val finalIndex = if (!dutyPairs.isOutOfBounds(incrementedCurrent)) {
            incrementedCurrent
        } else {
            firstElement
        }

        return DutyPairIndex(finalIndex)
    }

    private companion object {

        private const val FIRST_DUTY_PAIR_INDEX = 0
    }
}