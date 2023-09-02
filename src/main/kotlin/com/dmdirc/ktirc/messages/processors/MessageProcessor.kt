package com.dmdirc.ktirc.messages.processors

import com.dmdirc.ktirc.events.IrcEvent
import com.dmdirc.ktirc.model.IrcMessage

internal interface MessageProcessor {

    /**
     * The messages which this handler can process.
     */
    val commands: Array<String>

    /**
     * Processes the given message.
     */
    fun process(message: IrcMessage): List<IrcEvent>

}

internal val messageProcessors = listOf(
        AccountProcessor(),
        AwayProcessor(),
        AuthenticationProcessor(),
        BatchProcessor(),
        CapabilityProcessor(),
        ChangeHostProcessor(),
        ISupportProcessor(),
        JoinProcessor(),
        KickProcessor(),
        ModeProcessor(),
        MotdProcessor(),
        NamesProcessor(),
        NickChangeErrorProcessor(),
        NickProcessor(),
        NoticeProcessor(),
        PartProcessor(),
        PingProcessor(),
        PrivmsgProcessor(),
        InviteProcessor(),
        TopicProcessor(),
        QuitProcessor(),
        WelcomeProcessor()
)
