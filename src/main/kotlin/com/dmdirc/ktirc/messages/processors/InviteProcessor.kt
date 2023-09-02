package com.dmdirc.ktirc.messages.processors

import com.dmdirc.ktirc.events.*
import com.dmdirc.ktirc.model.IrcMessage
import com.dmdirc.ktirc.util.logger

internal class InviteProcessor : MessageProcessor {

    private val log by logger()

    override val commands = arrayOf("INVITE")

    override fun process(message: IrcMessage) = when {
        message.sourceUser == null -> emptyList()
        message.params.size < 2 -> {
            log.warning { "Discarding INVITE line with insufficient parameters: $message" }
            emptyList()
        }
        else -> listOf(InviteReceived(message.metadata, message.sourceUser, String(message.params[0]), String(message.params[1])))
    }

}
