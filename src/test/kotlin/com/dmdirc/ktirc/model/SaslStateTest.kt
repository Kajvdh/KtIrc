package com.dmdirc.ktirc.model

import com.dmdirc.ktirc.sasl.SaslMechanism
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SaslStateTest {

    private val mech1 = mock<SaslMechanism> {
        on { priority } doReturn 1
        on { ircName } doReturn "mech1"
    }

    private val mech2 = mock<SaslMechanism> {
        on { priority } doReturn 2
        on { ircName } doReturn "mech2"
    }

    private val mech3 = mock<SaslMechanism> {
        on { priority } doReturn 3
        on { ircName } doReturn "mech3"
    }

    private val mechanisms = listOf(mech1, mech2, mech3)

    @Test
    fun `gets most preferred client SASL mechanism if none are specified by server`() {
        val state = SaslState(null)
        state.mechanisms.addAll(mechanisms)

        assertEquals(mech3, state.getPreferredSaslMechanism(emptyList()))
    }

    @Test
    fun `gets next preferred client SASL mechanism if one was tried`() {
        val state = SaslState(null)
        state.mechanisms.addAll(mechanisms)
        state.currentMechanism = mech3

        assertEquals(mech2, state.getPreferredSaslMechanism(emptyList()))
    }

    @Test
    fun `gets no preferred client SASL mechanism if all were tried`() {
        val state = SaslState(null)
        state.mechanisms.addAll(mechanisms)
        state.currentMechanism = mech1

        assertNull(state.getPreferredSaslMechanism(emptyList()))
    }

    @Test
    fun `gets most preferred client SASL mechanism if the server supports all`() {
        val state = SaslState(null)
        state.mechanisms.addAll(mechanisms)

        assertEquals(mech3, state.getPreferredSaslMechanism(listOf("mech1", "mech3", "mech2")))
    }

    @Test
    fun `gets most preferred client SASL mechanism if the server supports some`() {
        val state = SaslState(null)
        state.mechanisms.addAll(mechanisms)

        assertEquals(mech2, state.getPreferredSaslMechanism(listOf("mech2", "mech1", "other")))
    }

    @Test
    fun `gets no preferred client SASL mechanism if the server supports none`() {
        val state = SaslState(null)
        state.mechanisms.addAll(mechanisms)

        assertNull(state.getPreferredSaslMechanism(listOf("foo", "bar", "baz")))
    }

    @Test
    fun `setting the current mechanism clears the existing state`() {
        val state = SaslState(null)
        state.mechanisms.addAll(mechanisms)
        state.mechanismState = "in progress"
        state.currentMechanism = mech2
        assertNull(state.mechanismState)
    }

    @Test
    fun `reset clears all state`() = with(SaslState(null)) {
        currentMechanism = mech2
        mechanismState = "in progress"
        saslBuffer = "abcdef"

        reset()

        assertNull(currentMechanism)
        assertNull(mechanismState)
        assertTrue(saslBuffer.isEmpty())
    }

}
