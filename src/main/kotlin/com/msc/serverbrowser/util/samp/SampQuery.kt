package com.msc.serverbrowser.util.samp

import com.msc.serverbrowser.data.entites.Player
import com.msc.serverbrowser.util.basic.Encoding

import java.io.IOException
import java.net.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.IntStream

/**
 * Provides Methods for retrieving information from a SA-MP Server.
 *
 * @author Marcel
 * @see [Wiki SA-MP - Query Mechanism](http://wiki.sa-mp.com/wiki/Query_Mechanism)
 */
class SampQuery
/**
 * Configures the socket and the address that will be used for doing the queries.
 *
 * @param serverAddress hostname / ip
 * @param serverPort port
 * @param timeout the maximum time, that the socket tries connecting
 * @throws SocketException Thrown if the connection is closed unexpectedly / has never been
 * opened properly
 * @throws UnknownHostException if the host is unknown
 */
@Throws(SocketException::class, UnknownHostException::class)
@JvmOverloads constructor(serverAddress: String, private val serverPort: Int, timeout: Int = 2000) : AutoCloseable {

    private val socket: DatagramSocket = DatagramSocket()
    private val server: InetAddress = InetAddress.getByName(serverAddress)

    /**
     * Returns a String array, containing information about the server.
     *
     * @return String[]:<br></br>
     * Index 0: password (0 or 1)<br></br>
     * Index 1: players<br></br>
     * Index 2: maxplayers<br></br>
     * Index 3: hostname<br></br>
     * Index 4: gamemode<br></br>
     * Index 5: language
     */
    // Password Yes / No
    // Players connected
    // Max Players
    // add hostname, gamemode and language
    val basicServerInfo: Optional<Array<String?>>
        get() {
            if (send(PACKET_GET_SERVERINFO)) {
                val reply = receiveBytes()
                if (reply != null) {
                    val buffer = wrapReply(reply)
                    val serverInfo = arrayOfNulls<String>(6)
                    val encoding = Encoding.getEncoding(reply).orElse(StandardCharsets.UTF_8.toString())
                    val password = buffer.get().toShort()
                    serverInfo[0] = password.toString()
                    val players = buffer.short
                    serverInfo[1] = players.toString()
                    val maxPlayers = buffer.short
                    serverInfo[2] = maxPlayers.toString()
                    for (valueIndex in 3..5) {
                        val len = buffer.int
                        val value = ByteArray(len)
                        IntStream.range(0, len).forEach { j -> value[j] = buffer.get() }
                        serverInfo[valueIndex] = Encoding.decodeUsingCharsetIfPossible(value, encoding)
                    }

                    return Optional.of(serverInfo)
                }
            }
            return Optional.empty()
        }

    /**
     * Returns an [Optional] of a [List] of [Player] objects, containing all
     * players on the server.
     *
     * @return an [Optional] containing a [List] of [Players][Player] or an empty
     * [Optional] in case the query failed.
     */
    val basicPlayerInfo: Optional<List<Player>>
        get() {
            var players: MutableList<Player>? = null

            if (send(PACKET_GET_BASIC_PLAYERINFO)) {
                val reply = receiveBytes()
                if (reply != null) {
                    val buffer = wrapReply(reply)
                    val numberOfPlayers = buffer.short.toInt()
                    players = ArrayList()

                    for (i in 0 until numberOfPlayers) {
                        val len = buffer.get().toInt()
                        val playerName = ByteArray(len)
                        IntStream.range(0, len).forEach { j -> playerName[j] = buffer.get() }
                        players.add(Player(String(playerName), buffer.int))
                    }
                }
            }
            return Optional.ofNullable(players)
        }

    /**
     * Returns a Map containing all server rules. The Key is always the rules name.
     *
     * @return a Map containing all server rules
     */
    // fill string for rule name
    // fill string for rule value
    val serversRules: Optional<Map<String, String>>
        get() {
            if (send(PACKET_GET_RULES)) {
                val reply = receiveBytes()
                if (reply != null) {
                    val buffer = wrapReply(reply)
                    val rules = HashMap<String, String>()

                    val ruleCount = buffer.short

                    for (i in 0 until ruleCount) {
                        var len = buffer.get().toInt()
                        val ruleName = ByteArray(len)
                        IntStream.range(0, len).forEach { j -> ruleName[j] = buffer.get() }
                        len = buffer.get().toInt()
                        val ruleValue = ByteArray(len)
                        IntStream.range(0, len).forEach { j -> ruleValue[j] = buffer.get() }

                        rules[String(ruleName)] = String(ruleValue)
                    }
                    return Optional.of(rules)
                }
            }
            return Optional.empty()
        }

    /**
     * Returns the server's ping.
     *
     * @return ping
     */
    val ping: Long
        get() {
            val beforeSend = System.currentTimeMillis()
            send(PACKET_MIRROR_CHARACTERS)
            receiveBytes()
            return System.currentTimeMillis() - beforeSend
        }

    init {
        socket.soTimeout = timeout
        checkConnection()
    }

    /**
     * Returns whether a successful connection was made.
     */
    @Throws(SocketException::class)
    private fun checkConnection() {
        /*
		 * TODO(MSC) Check if server deactivated querying, since this will only tell if the server
		 * is online, but will still work with servers that have deactivated querying
		 */
        send(PACKET_MIRROR_CHARACTERS)
        val reply = receiveBytes()
        // Removed the checks if the reply was valid, i think its not even necessary
        if (Objects.isNull(reply)) {
            throw SocketException("Couldn't connect to Server")
        }
    }

    @Throws(SocketException::class)
    override fun close() {
        socket.close()
    }

    private fun assemblePacket(type: Char): DatagramPacket {
        val tok = StringTokenizer(server.hostAddress, ".")
        val packetData = StringBuilder("SAMP")

        while (tok.hasMoreTokens()) {
            // The split parts of the ip will be parsed into integers and casted into characters
            packetData.append(Integer.parseInt(tok.nextToken()).toChar())
        }

        /*
		 * At this point the buffer contains something like 'SAMPx!2.' where each character after
		 * 'SAMP' is a part of the ip address
		 */

        packetData.append((serverPort and 0xFF).toChar()).append((serverPort shr 8 and 0xFF).toChar()).append(type)

        if (type == PACKET_MIRROR_CHARACTERS) {

            /*
			 * Applying random bytes for the server to mirror them back. TODO Currently those bytes
			 * aren't reused to check if the server did everything correctly.
			 */

            val random = ThreadLocalRandom.current()
            val toMirror = ByteArray(4)
            random.nextBytes(toMirror)
            packetData.append(String(toMirror, StandardCharsets.US_ASCII))
        }

        val data = packetData.toString().toByteArray(StandardCharsets.US_ASCII)
        return DatagramPacket(data, data.size, server, serverPort)
    }

    /**
     * Sends a packet to te server. The packet will automatically be assembled, depending
     * on thw given packet type.
     *
     * @param packetType character that defines the packets type
     */
    private fun send(packetType: Char): Boolean {
        return try {
            val packet = assemblePacket(packetType)
            socket.send(packet)
            true
        } catch (exception: IOException) {
            false
        }

    }

    /**
     * Receives a package from the server
     *
     * @return the package data as a byte array or null on fail
     */
    private fun receiveBytes(): ByteArray? {
        return try {
            // This is enough for at least 100 players information.
            val receivedData = ByteArray(14000)
            val receivedPacket = DatagramPacket(receivedData, receivedData.size)
            socket.receive(receivedPacket)
            receivedPacket.data
        } catch (exception: IOException) {
            null
        }

    }

    companion object {
        private const val PACKET_GET_SERVERINFO = 'i'
        private const val PACKET_GET_RULES = 'r'
        private const val PACKET_MIRROR_CHARACTERS = 'p'
        private const val PACKET_GET_BASIC_PLAYERINFO = 'c'

        /**
         *
         *
         * Wraps the received bytes in a [ByteBuffer] for easier usage.
         *
         * Contents of the byte array:
         *
         *  * Byte 0 - 3: "SAMP"
         *  * Byte 4 - 7: IP
         *  * Byte 8 - 9: Port
         *  * Byte 10: Message Type
         *  * Byte 11+: Data
         *
         *
         *
         * Because the replies contain some irrelevant data that we do not care for as of now, we are
         * setting the byte buffers initial position to eleven.
         *
         *
         * @param reply the byte array to be wrapped
         * @return the [ByteBuffer] that wraps the byte array
         */
        private fun wrapReply(reply: ByteArray): ByteBuffer {
            val buffer = ByteBuffer.wrap(reply)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.position(11)
            return buffer
        }
    }
}