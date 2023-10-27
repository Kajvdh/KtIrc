package com.dmdirc.ktirc.model

import com.dmdirc.ktirc.events.EventMetadata
import com.dmdirc.ktirc.util.currentTimeProvider
import com.dmdirc.ktirc.util.currentTimeZoneProvider
import com.dmdirc.ktirc.util.logger
import java.time.Instant
import java.time.LocalDateTime
import java.util.logging.Level

/**
 * Represents an IRC protocol message.
 */
internal class IrcMessage(val tags: Map<MessageTag, String>, val prefix: ByteArray?, val command: String, val params: List<ByteArray>) {

    private val log by logger()

    /** The time at which the message was sent, or our best guess at it. */
    val metadata = EventMetadata(
            time = tags[MessageTag.ServerTime]?.toLocalDateOrNull() ?: currentTimeProvider(),
            batchId = tags[MessageTag.Batch],
            messageId = tags[MessageTag.MessageId],
            label = tags[MessageTag.Label])

    /** The user that generated the message, if any. */
    val sourceUser = prefix?.asUser()?.apply {
        tags[MessageTag.AccountName]?.let { account = it }
    }

    private fun String.toLocalDateOrNull() = try {
        LocalDateTime.ofInstant(Instant.parse(this), currentTimeZoneProvider())
    } catch (e: Exception) {
        log.log(Level.WARNING, e) { "Received unparsable server-time tag: $this" }
        null
    }

    override fun toString(): String {
        return "IrcMessage(tags=$tags, prefix=${prefix?.let { String(prefix) }}, command='$command', params=${params.map { String(it) }}, metadata=$metadata, sourceUser=$sourceUser)"
    }

}

/**
 * Supported tags that may be applied to messages.
 */
@Suppress("unused")
sealed class MessageTag(val name: String) {
    /** Specifies the account name of the user, if the `account-tag` capability is negotiated. */
    object AccountName : MessageTag("account")

    /** Specifies the ID that a batch message belongs to. */
    object Batch : MessageTag("batch")

    /** An arbitrary label to identify the response to messages we generate. */
    object Label : MessageTag("draft/label")

    /** A unique ID for the message, used to reply, react, edit, delete, etc. */
    object MessageId : MessageTag("msgid")

    /** Indicates that the message was sent by a bot */
    object Bot : MessageTag("bot")

    /** Indicated that the message was sent by a bot (draft) */
    object DraftBot : MessageTag("draft/bot")

    /** Used to identify a message ID that was replied to, to enable threaded conversations. */
    object Reply : MessageTag("+draft/reply")

    /** Used to specify a slack-like reaction to another message. */
    object React : MessageTag("+draft/react")

    /** Used to specify that someone is typing a message. */
    object Typing : MessageTag("+typing")

    /** Specifies the time the server received the message, if the `server-time` capability is negotiated. */
    object ServerTime : MessageTag("time")
}

internal val messageTags: Map<String, MessageTag> by lazy {
    MessageTag::class.nestedClasses.map { it.objectInstance as MessageTag }.associateBy { it.name }
}
