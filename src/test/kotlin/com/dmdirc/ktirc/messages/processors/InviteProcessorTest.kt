package com.dmdirc.ktirc.messages.processors

import com.dmdirc.ktirc.TestConstants
import com.dmdirc.ktirc.events.InviteReceived
import com.dmdirc.ktirc.model.IrcMessage
import com.dmdirc.ktirc.model.User
import com.dmdirc.ktirc.params
import com.dmdirc.ktirc.util.currentTimeProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class InviteProcessorTest {

    @BeforeEach
    fun setUp() {
        currentTimeProvider = { TestConstants.time }
    }

    @Test
    fun `raises invite received event`() {
        val events = InviteProcessor().process(
                IrcMessage(emptyMap(), "acidburn!libby@root.localhost".toByteArray(), "INVITE", params("crashOverride", "#crashandburn")))
        assertEquals(1, events.size)

        val event = events[0] as InviteReceived
        assertEquals(TestConstants.time, event.metadata.time)
        assertEquals(User("acidburn", "libby", "root.localhost"), event.user)
        assertEquals("crashOverride", event.target)
        assertEquals("#crashandburn", event.inviteChannel)
    }

    @Test
    fun `does nothing if prefix missing`() {
        val events = InviteProcessor().process(
                IrcMessage(emptyMap(), null, "INVITE", params("crashOverride", "#crashandburn")))
        assertEquals(0, events.size)
    }

    @Test
    fun `does nothing if arguments missing`() {
        val events = InviteProcessor().process(
                IrcMessage(emptyMap(), "acidburn!libby@root.localhost".toByteArray(), "INVITE", params("#crashandburn")))
        assertEquals(0, events.size)
    }
}
