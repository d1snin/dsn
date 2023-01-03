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

package dev.d1s.dsn.job

import dev.d1s.dsn.config.ApplicationConfig
import dev.d1s.dsn.service.DutyPairService
import dev.d1s.dsn.service.GroupChatService
import dispatch.core.IOCoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging
import org.quartz.*
import java.util.*

class AnnounceDutyPairJob : ScheduledJob(), KoinComponent {

    private val dutyPairService by inject<DutyPairService>()

    private val groupChatService by inject<GroupChatService>()

    private val config by inject<ApplicationConfig>()

    private val announceDutyPairJobScope = IOCoroutineScope()

    private val log = logging()

    override fun execute(context: JobExecutionContext?) {
        announceDutyPairJobScope.launch {
            if (groupChatService.isGroupChatInfoInitialized()) {
                dutyPairService.switchDutyPair()
            }
        }
    }

    override fun schedule() {
        log.i {
            "Scheduling $IDENTITY job..."
        }

        val jobDetail = JobBuilder.newJob(AnnounceDutyPairJob::class.java)
            .withIdentity(IDENTITY)
            .build()

        val cron = CronScheduleBuilder.cronSchedule(config.announcing.cron).inTimeZone(TimeZone.getDefault())

        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(CRON_TRIGGER_IDENTITY)
            .withSchedule(cron)
            .build()

        scheduler.scheduleJob(jobDetail, trigger)
    }

    companion object {

        const val IDENTITY = "announce-duty-pair"

        private const val CRON_TRIGGER_IDENTITY = "announce-duty-pair-cron-trigger"

        val key = JobKey(IDENTITY)
    }
}