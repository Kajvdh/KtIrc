package com.dmdirc.ktirc.messages.processors

import com.dmdirc.ktirc.events.*
import com.dmdirc.ktirc.messages.CTCP_BYTE
import com.dmdirc.ktirc.model.IrcMessage
import com.dmdirc.ktirc.model.User
import com.dmdirc.ktirc.util.logger

internal class PrivmsgProcessor : MessageProcessor {

    private val log by logger()

    override val commands = arrayOf("PRIVMSG", "TAGMSG")

    override fun process(message: IrcMessage) = when(message.command) {
        "PRIVMSG" -> {
            when {
                message.sourceUser == null -> emptyList()
                message.params.size < 2 -> {
                    log.warning { "Discarding PRIVMSG line with insufficient parameters: $message" }
                    emptyList()
                }
                message.isCtcp() -> listOf(handleCtcp(message, message.sourceUser))
                else -> listOf(MessageReceived(message.metadata, message.sourceUser, String(message.params[0]), String(message.params[1])))
            }
        }
        else -> emptyList()
    }

    private fun handleCtcp(message: IrcMessage, user: User): IrcEvent {
        val content = String(message.params[1].sliceArray(1 until message.params[1].size - 1))
        val parts = content.split(' ', limit = 2)
        val body = if (parts.size == 2) parts[1] else ""
        return when (parts[0].toUpperCase()) {
            "ACTION" -> ActionReceived(message.metadata, user, String(message.params[0]), body)
            else -> CtcpReceived(message.metadata, user, String(message.params[0]), parts[0], body)
        }
    }

    private fun IrcMessage.isCtcp() = params[1].size > 2 && params[1][0] == CTCP_BYTE && params[1][params[1].size - 1] == CTCP_BYTE

}
