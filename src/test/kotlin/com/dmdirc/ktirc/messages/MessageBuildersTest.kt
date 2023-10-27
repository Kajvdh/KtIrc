package com.dmdirc.ktirc.messages

import com.dmdirc.ktirc.IrcClient
import com.dmdirc.ktirc.model.MessageTag
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class MessageBuildersTest {

    private val mockClient = mockk<IrcClient>()

    @Test
    fun `sendCapabilityRequest sends CAP REQ message with single argument`() {
        mockClient.sendCapabilityRequest(listOf("a"))
        verify { mockClient.send("CAP", "REQ", "a") }
    }

    @Test
    fun `sendCapabilityRequest sends CAP REQ message with multiple args`() {
        mockClient.sendCapabilityRequest(listOf("a b c"))
        verify { mockClient.send("CAP", "REQ", "a b c") }
    }

    @Test
    fun `sendJoin sends correct JOIN message`() {
        mockClient.sendJoin("#TheGibson")
        verify { mockClient.send("JOIN", "#TheGibson") }
    }

    @Test
    fun `sendPart sends correct PART message without reason`() {
        mockClient.sendPart("#TheGibson")
        verify { mockClient.send("PART", "#TheGibson") }
    }

    @Test
    fun `sendPart sends correct PART message with reason`() {
        mockClient.sendPart("#TheGibson", "Mess with the best...")
        verify { mockClient.send("PART", "#TheGibson", "Mess with the best...") }
    }

    @Test
    fun `sendModeRequest sends correct MODE message`() {
        mockClient.sendModeRequest("#TheGibson")
        verify { mockClient.send("MODE", "#TheGibson") }
    }

    @Test
    fun `sendNickChange sends correct NICK message`() {
        mockClient.sendNickChange("AcidBurn")
        verify { mockClient.send("NICK", "AcidBurn") }
    }

    @Test
    fun `sendAway sends blank AWAY message with no args`() {
        mockClient.sendAway()
        verify { mockClient.send(emptyMap(), "AWAY") }
    }

    @Test
    fun `sendAway sends AWAY message with reason`() {
        mockClient.sendAway("Hacking the planet")
        verify { mockClient.send("AWAY", "Hacking the planet") }
    }

    @Test
    fun `sendPassword sends correct PASS message`() {
        mockClient.sendPassword("hacktheplanet")
        verify { mockClient.send("PASS", "hacktheplanet") }
    }

    @Test
    fun `sendPong sends correct PONG message`() {
        mockClient.sendPong("abcdef".toByteArray())
        verify { mockClient.send("PONG", "abcdef") }
    }

    @Test
    fun `sendMessage sends correct PRIVMSG message`() {
        mockClient.sendMessage("acidBurn", "Hack the planet!")
        verify { mockClient.send(tagMap(), "PRIVMSG", "acidBurn", "Hack the planet!") }
    }

    @Test
    fun `sendMessage sends correct PRIVMSG message with reply to tag`() {
        mockClient.sendMessage("acidBurn", "Hack the planet!", "abc123")
        verify { mockClient.send(tagMap(MessageTag.Reply to "abc123"), "PRIVMSG", "acidBurn", "Hack the planet!") }
    }

    @Test
    fun `sendCtcp sends correct CTCP message with no arguments`() {
        mockClient.sendCtcp("acidBurn", "ping")
        verify { mockClient.send(tagMap(), "PRIVMSG", "acidBurn", "\u0001PING\u0001") }
    }

    @Test
    fun `sendCtcp sends correct CTCP message with arguments`() {
        mockClient.sendCtcp("acidBurn", "ping", "12345")
        verify { mockClient.send("NOTICE", "acidBurn", "\u0001PING 12345\u0001") }
    }

    @Test
    fun `sendAction sends correct action`() {
        mockClient.sendAction("acidBurn", "hacks the planet")
        verify { mockClient.send(tagMap(), "PRIVMSG", "acidBurn", "\u0001ACTION hacks the planet\u0001") }
    }

    @Test
    fun `sendUser sends correct USER message`() {
        mockClient.sendUser("AcidBurn", "Kate")
        verify { mockClient.send("USER", "AcidBurn", "0", "*", "Kate") }
    }

    @Test
    fun `sendUser sends correct AUTHENTICATE message`() {
        mockClient.sendAuthenticationMessage("SCRAM-MD5")
        verify { mockClient.send("AUTHENTICATE", "SCRAM-MD5") }
    }

    @Test
    fun `sendUser sends correct blank AUTHENTICATE message`() {
        mockClient.sendAuthenticationMessage()
        verify { mockClient.send("AUTHENTICATE", "+") }
    }

    @Test
    fun `sendTagMessage sends tags`() {
        val tags = mapOf(MessageTag.MessageId to "id", MessageTag.AccountName to "foo")
        mockClient.sendTagMessage("#thegibson", tags)
        verify { mockClient.send(tags, "TAGMSG", "#thegibson") }
    }

    @Test
    fun `sendTagMessage sends tags with reply ID`() {
        val tags = mapOf(MessageTag.MessageId to "id", MessageTag.AccountName to "foo")
        mockClient.sendTagMessage("#thegibson", tags, "otherId")
        verify { mockClient.send(tags + (MessageTag.Reply to "otherId"), "TAGMSG", "#thegibson") }
    }

}
