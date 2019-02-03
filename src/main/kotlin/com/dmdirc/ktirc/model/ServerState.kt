package com.dmdirc.ktirc.model

import com.dmdirc.ktirc.io.CaseMapping
import kotlin.reflect.KClass

/**
 * Contains the current state of a single IRC server.
 */
class ServerState internal constructor(initialNickname: String) {

    /** Whether we've received the 'Welcome to IRC' (001) message. */
    internal var receivedWelcome = false

    /** The current status of the server. */
    var status = ServerStatus.Connecting
        internal set

    /** Our present nickname on the server. */
    var localNickname: String = initialNickname
        internal set

    /** The features that the server has declared it supports (from the 005 header). */
    val features = ServerFeatureMap()

    /** The capabilities we have negotiated with the server (from IRCv3). */
    val capabilities = CapabilitiesState()

}

class ServerFeatureMap {

    private val features = HashMap<ServerFeature<*>, Any?>()

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(feature: ServerFeature<T>) = features.getOrDefault(feature, feature.default) as? T? ?: feature.default

    internal operator fun set(feature: ServerFeature<*>, value: Any) {
        require(feature.type.isInstance(value))
        features[feature] = value
    }

    internal fun setAll(featureMap: ServerFeatureMap) = featureMap.features.forEach { feature, value -> features[feature] = value }
    internal fun reset(feature: ServerFeature<*>) = features.put(feature, null)

}

data class ModePrefixMapping(val modes: String, val prefixes: String) {

    fun isPrefix(char: Char) = prefixes.contains(char)
    fun getMode(prefix: Char) = modes[prefixes.indexOf(prefix)]
    fun getModes(prefixes: String) = String(prefixes.map(this::getMode).toCharArray())

}

sealed class ServerFeature<T : Any>(val name: String, val type: KClass<T>, val default: T? = null) {
    object ServerCaseMapping : ServerFeature<CaseMapping>("CASEMAPPING", CaseMapping::class, CaseMapping.Rfc)
    object ModePrefixes : ServerFeature<ModePrefixMapping>("PREFIX", ModePrefixMapping::class, ModePrefixMapping("ov", "@+"))
    object MaximumChannels : ServerFeature<Int>("MAXCHANNELS", Int::class) // TODO: CHANLIMIT also exists
    object ChannelModes : ServerFeature<String>("CHANMODES", String::class)
    object MaximumChannelNameLength : ServerFeature<Int>("CHANNELLEN", Int::class, 200)
    object WhoxSupport : ServerFeature<Boolean>("WHOX", Boolean::class, false)
}

internal val serverFeatures: Map<String, ServerFeature<*>> by lazy {
    ServerFeature::class.nestedClasses.map { it.objectInstance as ServerFeature<*> }.associateBy { it.name }
}

/**
 * Enumeration of the possible states of a server.
 */
enum class ServerStatus {
    /** We are attempting to connect to the server. It is not yet ready for use. */
    Connecting,
    /** We are logging in, dealing with capabilities, etc. The server is not yet ready for use. */
    Negotiating,
    /** We are connected and commands can be sent. */
    Ready,
}
