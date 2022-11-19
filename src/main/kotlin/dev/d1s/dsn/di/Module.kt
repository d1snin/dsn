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

package dev.d1s.dsn.di

import dev.d1s.dsn.DsnApplication
import dev.d1s.dsn.bot.TelegramBot
import dev.d1s.dsn.bot.TelegramBotImpl
import dev.d1s.dsn.bot.command.*
import dev.d1s.dsn.config.ApplicationConfigFactory
import dev.d1s.dsn.config.ApplicationConfigFactoryImpl
import dev.d1s.dsn.database.RedisClientFactory
import dev.d1s.dsn.database.RedisClientFactoryImpl
import dev.d1s.dsn.job.AnnounceDutyPairJob
import dev.d1s.dsn.job.ScheduledJob
import dev.d1s.dsn.service.*
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.logger.SLF4JLogger
import org.quartz.impl.StdSchedulerFactory

fun setupDi() {
    startKoin {
        logger(SLF4JLogger())

        val mainModule = module {
            application()
            telegramBot()
            commandHolder()
            commands()
            applicationConfig()
            applicationConfigFactory()
            redisClientFactory()
            authenticationService()
            dutyPairService()
            groupChatService()
            schedulingService()
            scheduler()
            jobs()
        }

        modules(mainModule)
    }
}

fun Module.application() {
    singleOf(::DsnApplication)
}

fun Module.telegramBot() {
    singleOf<TelegramBot>(::TelegramBotImpl)
}

fun Module.commandHolder() {
    singleOf<CommandHolder>(::CommandHolderImpl)
}

fun Module.commands() {
    singleOf<Command>(::GetCurrentDutyPairCommand) {
        qualifier = Qualifier.GetCurrentDutyPairCommand
    }

    singleOf<Command>(::GetDutyPairsCommand) {
        qualifier = Qualifier.GetDutyPairsCommand
    }

    singleOf<Command>(::InitGroupChatCommand) {
        qualifier = Qualifier.InitGroupChatCommand
    }

    singleOf<Command>(::PostponeDutyPairCommand) {
        qualifier = Qualifier.PostponeDutyPairCommand
    }

    singleOf<Command>(::ResetGroupChatCommand) {
        qualifier = Qualifier.ResetGroupChatCommand
    }

    singleOf<Command>(::SetDutyPairCommand) {
        qualifier = Qualifier.SetDutyPairCommand
    }

    singleOf<Command>(::SwitchDutyPairCommand) {
        qualifier = Qualifier.SwitchDutyPairCommand
    }

    singleOf<Command>(::ToggleSchedulingCommand) {
        qualifier = Qualifier.ToggleSchedulingCommand
    }
}

fun Module.applicationConfig() {
    single {
        get<ApplicationConfigFactory>().config
    }
}

fun Module.applicationConfigFactory() {
    singleOf<ApplicationConfigFactory>(::ApplicationConfigFactoryImpl)
}

fun Module.redisClientFactory() {
    singleOf<RedisClientFactory>(::RedisClientFactoryImpl)
}

fun Module.authenticationService() {
    singleOf<AuthenticationService>(::AuthenticationServiceImpl)
}

fun Module.dutyPairService() {
    singleOf<DutyPairService>(::DutyPairServiceImpl)
}

fun Module.groupChatService() {
    singleOf<GroupChatService>(::GroupChatServiceImpl)
}

fun Module.schedulingService() {
    singleOf<SchedulingService>(::SchedulingServiceImpl)
}

fun Module.scheduler() {
    single {
        val schedulerFactory = StdSchedulerFactory()

        schedulerFactory.scheduler
    }
}

fun Module.jobs() {
    single<ScheduledJob>(named(AnnounceDutyPairJob.IDENTITY)) {
        AnnounceDutyPairJob()
    }
}