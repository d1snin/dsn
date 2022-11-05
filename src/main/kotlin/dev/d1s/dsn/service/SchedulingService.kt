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

import dev.d1s.dsn.di.Qualifier
import dev.d1s.dsn.job.ScheduledJob
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging
import org.quartz.Scheduler

interface SchedulingService {

    fun scheduleJobs()
}

class SchedulingServiceImpl : SchedulingService, KoinComponent {

    private val log = logging()

    private val scheduler by inject<Scheduler>()

    private val announceDutyPairJob by inject<ScheduledJob>(Qualifier.AnnounceDutyPairJob)

    override fun scheduleJobs() {
        log.i {
            "Initializing jobs..."
        }

        announceDutyPairJob.schedule()

        scheduler.start()
    }
}
