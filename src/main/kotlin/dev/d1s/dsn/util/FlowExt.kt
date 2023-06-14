package dev.d1s.dsn.util

import dev.inmo.tgbotapi.abstracts.FromUser
import dev.inmo.tgbotapi.extensions.api.chat.get.getChatAdministrators
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.IdChatIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

fun <T : FromUser> Flow<T>.filterIsAdmin(context: BehaviourContext, chatId: IdChatIdentifier) =
    filter { fromUser ->
        context.getChatAdministrators(chatId).map { it.user.id }.contains(fromUser.from.id)
    }