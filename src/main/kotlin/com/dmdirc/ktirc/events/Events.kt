package com.dmdirc.ktirc.events

import com.dmdirc.ktirc.model.ConnectionError
import com.dmdirc.ktirc.model.ServerFeatureMap
import com.dmdirc.ktirc.model.User
import com.dmdirc.ktirc.util.RemoveIn
import java.time.LocalDateTime

/**
 * Metadata associated with an event.
 *
 * @property time The best-guess time at which the event occurred.
 * @property batchId The ID of the batch this event is part of, if any.
 * @property messageId The unique ID of this message, if any.
 * @property label The label of the command that this event was sent in response to, if any.
 */
data class EventMetadata internal constructor(
        val time: LocalDateTime,
        val batchId: String? = null,
        val messageId: String? = null,
        val label: String? = null)

/**
 * Base class for all events raised by KtIrc.
 *
 * An event occurs in response to some action by the IRC server. Most events correspond to a single
 * line received from the server (such as [NoticeReceived]), but some happen when a combination
 * of lines lead to a certain state (e.g. [ServerReady]), and the [BatchReceived] event in particular
 * can contain *many* lines received from the server.
 *
 * @property metadata Meta-data about the received event
 */
sealed class IrcEvent(val metadata: EventMetadata) {

    /** The time at which the event occurred. */
    @Deprecated("Moved to metadata", replaceWith = ReplaceWith("metadata.time"))
    @RemoveIn("2.0.0")
    val time: LocalDateTime
        get() = metadata.time

}

/**
 * Base class for events that are targeted to a channel or user.
 *
 * @param target The target of the event - either a channel name, or nick name
 */
sealed class TargetedEvent(metadata: EventMetadata, val target: String) : IrcEvent(metadata) {

    /** The channel (or user!) this event was targeted at. */
    @Deprecated("Use target instead", replaceWith = ReplaceWith("target"))
    @RemoveIn("2.0.0")
    val channel: String
        get() = target

}

/**
 * Interface implemented by events that come from a particular user.
 */
interface SourcedEvent {
    /** The user that caused the event. */
    val user: User
}

/** Raised when a connection to the server is being established. */
class ServerConnecting(metadata: EventMetadata) : IrcEvent(metadata)

/** Raised when the connection to the server has been established. The server will not be ready for use yet. */
class ServerConnected(metadata: EventMetadata) : IrcEvent(metadata)

/** Raised when the connection to the server has ended. */
class ServerDisconnected(metadata: EventMetadata) : IrcEvent(metadata)

/** Raised when an error occurred trying to connect. */
class ServerConnectionError(metadata: EventMetadata, val error: ConnectionError, val details: String?) : IrcEvent(metadata)

/**
 * Raised when the server is ready for use.
 *
 * At this point, you should be able to freely send messages to the IRC server and can start joining channels etc, and
 * the server state will contain relevant information about the server, its features, etc.
 */
class ServerReady(metadata: EventMetadata) : IrcEvent(metadata)

/** Raised when the server initially welcomes us to the IRC network. */
class ServerWelcome(metadata: EventMetadata, val server: String, val localNick: String) : IrcEvent(metadata)

/** Raised when the features supported by the server have changed. This may occur numerous times. */
class ServerFeaturesUpdated(metadata: EventMetadata, val serverFeatures: ServerFeatureMap) : IrcEvent(metadata)

/** Raised whenever a PING is received from the server. */
class PingReceived(metadata: EventMetadata, val nonce: ByteArray) : IrcEvent(metadata)

/** Raised when a user joins a channel. */
class ChannelJoined(metadata: EventMetadata, override val user: User, channel: String) : TargetedEvent(metadata, channel), SourcedEvent

/** Raised when a user leaves a channel. */
class ChannelParted(metadata: EventMetadata, override val user: User, channel: String, val reason: String = "") : TargetedEvent(metadata, channel), SourcedEvent

/** Raised when a [victim] is kicked from a channel. */
class ChannelUserKicked(metadata: EventMetadata, override val user: User, channel: String, val victim: String, val reason: String = "") : TargetedEvent(metadata, channel), SourcedEvent

/** Raised when a user quits, and is in a channel. */
class ChannelQuit(metadata: EventMetadata, override val user: User, channel: String, val reason: String = "") : TargetedEvent(metadata, channel), SourcedEvent

/** Raised when a user changes nickname, and is in a channel. */
class ChannelNickChanged(metadata: EventMetadata, override val user: User, channel: String, val newNick: String) : TargetedEvent(metadata, channel), SourcedEvent

/** Raised when a batch of the channel's member list has been received. More batches may follow. */
class ChannelNamesReceived(metadata: EventMetadata, channel: String, val names: List<String>) : TargetedEvent(metadata, channel)

/** Raised when the entirety of the channel's member list has been received. */
class ChannelNamesFinished(metadata: EventMetadata, channel: String) : TargetedEvent(metadata, channel)

/** Raised when a channel topic is discovered (not changed). Usually followed by [ChannelTopicMetadataDiscovered] if the [topic] is non-null. */
class ChannelTopicDiscovered(metadata: EventMetadata, channel: String, val topic: String?) : TargetedEvent(metadata, channel)

/** Raised when a channel topic's metadata is discovered. */
class ChannelTopicMetadataDiscovered(metadata: EventMetadata, channel: String, val user: User, val setTime: LocalDateTime) : TargetedEvent(metadata, channel)

/**
 * Raised when a channel's topic is changed.
 *
 * @property topic The new topic of the channel, or `null` if the topic has been unset (cleared)
 */
class ChannelTopicChanged(metadata: EventMetadata, override val user: User, channel: String, val topic: String?) : TargetedEvent(metadata, channel), SourcedEvent

/** Raised when a message is received. */
class MessageReceived(metadata: EventMetadata, override val user: User, target: String, val message: String) : TargetedEvent(metadata, target), SourcedEvent {

    /** The message ID of this message. */
    @Deprecated("Moved to metadata", replaceWith = ReplaceWith("metadata.messageId"))
    @RemoveIn("2.0.0")
    val messageId: String?
        get() = metadata.messageId

}

/**
 * Raised when a notice is received.
 *
 * The [user] may in fact be a server, or have a nickname of `*` while connecting.
 */
class NoticeReceived(metadata: EventMetadata, override val user: User, target: String, val message: String) : TargetedEvent(metadata, target), SourcedEvent

/** Raised when an action is received. */
class ActionReceived(metadata: EventMetadata, override val user: User, target: String, val action: String) : TargetedEvent(metadata, target), SourcedEvent {

    /** The message ID of this action. */
    @Deprecated("Moved to metadata", replaceWith = ReplaceWith("metadata.messageId"))
    @RemoveIn("2.0.0")
    val messageId: String?
        get() = metadata.messageId

}

/** Raised when a CTCP is received. */
class CtcpReceived(metadata: EventMetadata, override val user: User, target: String, val type: String, val content: String) : TargetedEvent(metadata, target), SourcedEvent

/** Raised when a CTCP reply is received. */
class CtcpReplyReceived(metadata: EventMetadata, override val user: User, target: String, val type: String, val content: String) : TargetedEvent(metadata, target), SourcedEvent

/** Raised when a user quits. */
class UserQuit(metadata: EventMetadata, override val user: User, val reason: String = "") : IrcEvent(metadata), SourcedEvent

/** Raised when a user changes nickname. */
class UserNickChanged(metadata: EventMetadata, override val user: User, val newNick: String) : IrcEvent(metadata), SourcedEvent

/** Raised when a user changes hostname. */
class UserHostChanged(metadata: EventMetadata, override val user: User, val newIdent: String, val newHost: String) : IrcEvent(metadata), SourcedEvent

/**
 * Raised when a user's account changes (i.e., they auth'd or deauth'd with services).
 *
 * This event is only raised if the server supports the `account-notify` capability.
 */
class UserAccountChanged(metadata: EventMetadata, override val user: User, val newAccount: String?) : IrcEvent(metadata), SourcedEvent

/** Raised when available server capabilities are received. More batches may follow. */
class ServerCapabilitiesReceived(metadata: EventMetadata, val capabilities: Map<String, String>) : IrcEvent(metadata)

/** Raised when our requested capabilities are acknowledged. More batches may follow. */
class ServerCapabilitiesAcknowledged(metadata: EventMetadata, val capabilities: Map<String, String>) : IrcEvent(metadata)

/** Raised when the server has finished sending us capabilities. */
class ServerCapabilitiesFinished(metadata: EventMetadata) : IrcEvent(metadata)

/** Raised when a line of the Message Of the Day has been received. */
class MotdLineReceived(metadata: EventMetadata, val line: String, val first: Boolean = false) : IrcEvent(metadata)

/** Raised when a Message Of the Day has completed. */
class MotdFinished(metadata: EventMetadata, val missing: Boolean = false) : IrcEvent(metadata)

/**
 * Raised when a mode change occurs.
 *
 * If [discovered] is true then the event is in response to the server providing the full set of modes on the target,
 * and the given modes are thus exhaustive. Otherwise, the modes are a sequence of changes to apply to the existing
 * state.
 */
class ModeChanged(metadata: EventMetadata, target: String, val modes: String, val arguments: Array<String>, val discovered: Boolean = false) : TargetedEvent(metadata, target)

/** Raised when an AUTHENTICATION message is received. [argument] is `null` if the server sent an empty reply ("+") */
class AuthenticationMessage(metadata: EventMetadata, val argument: String?) : IrcEvent(metadata)

/** Raised when a SASL attempt finishes, successfully or otherwise. */
class SaslFinished(metadata: EventMetadata, var success: Boolean) : IrcEvent(metadata)

/** Raised when the server says our SASL mechanism isn't available, but gives us a list of others. */
class SaslMechanismNotAvailableError(metadata: EventMetadata, var mechanisms: Collection<String>) : IrcEvent(metadata)

/** Indicates a batch of messages has begun. */
class BatchStarted(metadata: EventMetadata, val referenceId: String, val batchType: String, val params: Array<String>) : IrcEvent(metadata)

/** Indicates a batch of messages has finished. */
class BatchFinished(metadata: EventMetadata, val referenceId: String) : IrcEvent(metadata)

/** A batch of events that should be handled together. */
class BatchReceived(metadata: EventMetadata, val type: String, val params: Array<String>, val events: List<IrcEvent>) : IrcEvent(metadata)

/**
 * Raised when attempting to set or change our nickname fails.
 *
 * If this happens before {ServerReady], the nickname must be changed for registration to continue.
 */
class NicknameChangeFailed(metadata: EventMetadata, val cause: NicknameChangeError): IrcEvent(metadata) {
    /** Reasons a nick change may fail. */
    enum class NicknameChangeError {
        /** The nickname is not allowed by the server. */
        ErroneousNickname,
        /** The nickname is already in use. */
        AlreadyInUse,
        /** The nickname has collided with another somehow. */
        Collision,
        /** No nickname was provided. */
        NoNicknameGiven
    }
}

