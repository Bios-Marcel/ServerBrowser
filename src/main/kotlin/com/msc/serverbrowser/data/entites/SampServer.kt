package com.msc.serverbrowser.data.entites

import javafx.beans.property.*
import com.msc.serverbrowser.util.fx.OneLineStringProperty
import java.util.*

class SampServer(address: String, port: Int) {
    private val passwordedProperty = SimpleBooleanProperty()

    private val hostnameProperty = OneLineStringProperty()
    private val addressProperty = OneLineStringProperty()
    private val actualPlayersProperty = OneLineStringProperty()
    private val modeProperty = OneLineStringProperty()
    private val languageProperty = OneLineStringProperty()
    private val lagcompProperty = OneLineStringProperty()
    private val websiteProperty = OneLineStringProperty()
    private val versionProperty = OneLineStringProperty()
    private val mapProperty = OneLineStringProperty()

    private val portProperty = SimpleIntegerProperty()
    private val playersProperty = SimpleIntegerProperty()
    private val maxPlayersProperty = SimpleIntegerProperty()

    private val lastJoinProperty = SimpleLongProperty()

    var isPassworded: Boolean
        get() = passwordedProperty.get()
        set(passworded) = passwordedProperty.set(passworded)

    var hostname: String
        get() = hostnameProperty.get()
        set(hostname) = hostnameProperty.set(hostname)

    var address: String
        get() = addressProperty.get()
        set(address) = addressProperty.set(address)

    var lagcomp: String
        get() = lagcompProperty.get()
        set(lagcomp) = lagcompProperty.set(lagcomp)

    var language: String
        get() = languageProperty.get()
        set(language) = languageProperty.set(language)

    var maxPlayers: Int?
        get() = maxPlayersProperty.get()
        set(maxPlayers) {
            maxPlayersProperty.set(maxPlayers!!)
            updatePlayersAndMaxPlayers()
        }

    var mode: String
        get() = modeProperty.get()
        set(mode) = modeProperty.set(mode)

    var port: Int
        get() = portProperty.get()
        set(port) = portProperty.set(port)

    var players: Int?
        get() = playersProperty.get()
        set(players) {
            playersProperty.set(players!!)
            updatePlayersAndMaxPlayers()
        }

    var version: String
        get() = versionProperty.get()
        set(version) = versionProperty.set(version)

    var website: String?
        get() = websiteProperty.get()
        set(website) = websiteProperty.set(website!!)

    var map: String
        get() = mapProperty.get()
        set(map) = mapProperty.set(map)

    var lastJoin: Long?
        get() = lastJoinProperty.get()
        set(lastJoin) = lastJoinProperty.set(lastJoin!!)

    init {
        addressProperty.set(address)
        portProperty.set(port)
    }

    fun passwordedProperty(): BooleanProperty {
        return passwordedProperty
    }

    fun hostnameProperty(): StringProperty {
        return hostnameProperty
    }

    fun portProperty(): IntegerProperty {
        return portProperty
    }

    fun addressProperty(): StringProperty {
        return addressProperty
    }

    fun lagcompProperty(): StringProperty {
        return lagcompProperty
    }

    fun languageProperty(): StringProperty {
        return languageProperty
    }

    fun maxPlayersProperty(): IntegerProperty {
        return maxPlayersProperty
    }

    fun modeProperty(): StringProperty {
        return modeProperty
    }

    fun playersAndMaxPlayersProperty(): StringProperty {
        return actualPlayersProperty
    }

    fun playersProperty(): IntegerProperty {
        return playersProperty
    }

    fun versionProperty(): StringProperty {
        return versionProperty
    }

    fun websiteProperty(): StringProperty {
        return websiteProperty
    }

    fun mapProperty(): StringProperty {
        return mapProperty
    }

    fun lastJoinProperty(): LongProperty {
        return lastJoinProperty
    }

    private fun updatePlayersAndMaxPlayers() {
        actualPlayersProperty.set(playersProperty.get().toString() + "/" + maxPlayersProperty.get())
    }

    override fun toString(): String {
        return "$address:$port"
    }

    override fun equals(`object`: Any?): Boolean {
        if (Objects.isNull(`object`) || `object`!!.javaClass != SampServer::class.java) {
            return false
        }

        val compare = `object` as SampServer?
        return compare === this || address == compare!!.address && port == compare.port
    }

    override fun hashCode(): Int {
        return (address + port).hashCode()
    }
}
