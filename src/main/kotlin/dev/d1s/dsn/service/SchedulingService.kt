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

package dev.d1s.dsn.service

import dev.d1s.dsn.database.Key
import dev.d1s.dsn.database.RedisClientFactory
import dev.d1s.dsn.di.Qualifier
import dev.d1s.dsn.job.PausedJobs
import dev.d1s.dsn.job.ScheduledJob
import dev.d1s.dsn.util.setAndPersist
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging
import org.quartz.JobKey
import org.quartz.Scheduler

interface SchedulingService {

    suspend fun scheduleJobs()

    suspend fun pauseJob(key: JobKey)

    suspend fun resumeJob(key: JobKey)

    suspend fun getPausedJobs(): PausedJobs
}

class SchedulingServiceImpl : SchedulingService, KoinComponent {

    private val log = logging()

    private val redisFactory by inject<RedisClientFactory>()

    private val redis by lazy {
        redisFactory.redis
    }

    private val scheduler by inject<Scheduler>()

    private val announceDutyPairJob by inject<ScheduledJob>(Qualifier.AnnounceDutyPairJob)

    override suspend fun scheduleJobs() {
        log.i {
            "Initializing jobs..."
        }

        announceDutyPairJob.schedule()
        scheduler.start()

        getPausedJobs().jobs.forEach { job ->
            scheduler.pauseJob(job)
        }
    }

    override suspend fun pauseJob(key: JobKey) {
        scheduler.pauseJob(key)

        appendPausedJobs(key)
    }

    override suspend fun resumeJob(key: JobKey) {
        scheduler.resumeJob(key)

        removePausedJob(key)
    }

    override suspend fun getPausedJobs(): PausedJobs {
        val jobs = redis.get(Key.PAUSED_JOBS) ?: ""

        return PausedJobs.deserialize(jobs)
    }

    private suspend fun appendPausedJobs(job: JobKey) {
        val existingJobs = getPausedJobs()
        existingJobs.jobs += job

        setPausedJobs(existingJobs)
    }

    private suspend fun removePausedJob(job: JobKey) {
        val existingJobs = getPausedJobs()
        existingJobs.jobs -= job

        setPausedJobs(existingJobs)
    }

    private suspend fun setPausedJobs(pausedJobs: PausedJobs) {
        val serializedJobs = pausedJobs.serialize()

        redis.setAndPersist(Key.PAUSED_JOBS, serializedJobs)
    }
}
