package com.dmdirc.ktirc.model

import com.dmdirc.ktirc.io.CaseMapping

class UserState(private val caseMappingProvider: () -> CaseMapping): Iterable<KnownUser> {

    private val users = UserMap(caseMappingProvider)

    operator fun get(nickname: String) = users[nickname]
    operator fun get(user: User) = users[user.nickname]

    internal operator fun plusAssign(details: User) { users += KnownUser(caseMappingProvider, details) }
    internal operator fun minusAssign(details: User) { users -= details.nickname }

    override operator fun iterator() = users.iterator().iterator()

    internal fun removeIf(predicate: (KnownUser) -> Boolean) = users.removeIf(predicate)

    internal fun update(user: User, oldNick: String = user.nickname) {
        users[oldNick]?.details?.updateFrom(user)
    }

    internal fun addToChannel(user: User, channel: String) {
        users[user.nickname]?.let {
            it += channel
        } ?: run {
            users += KnownUser(caseMappingProvider, user).apply { channels += channel }
        }
    }

}

class KnownUser(caseMappingProvider: () -> CaseMapping, val details: User) {

    val channels = CaseInsensitiveSet(caseMappingProvider)

    internal operator fun plusAssign(channel: String) { channels += channel }
    internal operator fun minusAssign(channel: String) { channels -= channel }
    operator fun contains(channel: String) = channel in channels

}
