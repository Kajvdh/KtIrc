package com.dmdirc.ktirc.messages

import com.dmdirc.ktirc.IrcClient
import com.dmdirc.ktirc.model.MessageTag

/** Sends a request to join the given channel. */
fun IrcClient.sendJoin(channel: String) = send("JOIN", channel)

/** Sends a request to part the given channel. */
fun IrcClient.sendPart(channel: String, reason: String? = null) =
        reason?.let { send("PART", channel, reason) } ?: send("PART", channel)

/** Sends a request to see the modes of a given target. */
fun IrcClient.sendModeRequest(target: String) = send("MODE", target)

/** Sends a request to change to the given nickname. */
fun IrcClient.sendNickChange(nick: String) = send("NICK", nick)

/** Sends a request to set or unset our away state. */
fun IrcClient.sendAway(reason: String? = null) =
        // We need to pass emptyMap() in the second case to avoid using the deprecated send(String) method
        // Once that's removed, the redundant map can be taken out.
        reason?.let { send("AWAY", reason) } ?: send(emptyMap(), "AWAY")

/** Sends a CTCP message of the specified [type] and with optional [data] to [target] (a user or a channel). */
fun IrcClient.sendCtcp(target: String, type: String, data: String? = null) = when(data) {
        null -> sendMessage(target, "\u0001${type.toUpperCase()}\u0001")
        else -> sendNotice(target, "\u0001${type.toUpperCase()}${data.let { " $it" } ?: ""}\u0001")
}

/** Sends an action to the given [target] (a user or a channel). */
fun IrcClient.sendAction(target: String, action: String) = sendMessage(target, "\u0001ACTION ${action}\u0001")

/** Sends a private message to a user or channel. */
fun IrcClient.sendMessage(target: String, message: String, inReplyTo: String? = null) =
        send(
                inReplyTo?.let { tagMap(MessageTag.Reply to inReplyTo) } ?: emptyMap(),
                "PRIVMSG",
                target,
                message)

fun IrcClient.sendNotice(target: String, message: String) =
        send(
                "NOTICE",
                target,
                message)

/**
 * Sends a tag-only message.
 *
 * If [inReplyTo] is specified then the [MessageTag.Reply] tag will be automatically added.
 */
fun IrcClient.sendTagMessage(target: String, tags: Map<MessageTag, String>, inReplyTo: String? = null) {
    send(inReplyTo?.let { tags + (MessageTag.Reply to inReplyTo) } ?: tags, "TAGMSG", target)
}

/**
 * Utility method for creating a map of tags to avoid type inference problems.
 */
fun tagMap(vararg tags: Pair<MessageTag, String>) = mapOf(*tags)
