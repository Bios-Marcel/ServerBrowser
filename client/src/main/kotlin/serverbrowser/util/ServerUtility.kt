package serverbrowser.util

import com.eclipsesource.json.Json
import serverbrowser.data.entites.SampServer
import serverbrowser.logging.Logging
import serverbrowser.util.basic.StringUtility
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*

/**
 * @author Marcel
 * @since 19.09.2017
 */
object ServerUtility {
    /**
     * The port, that every SA-MP server uses by default.
     */
    const val DEFAULT_SAMP_PORT = 7777

    private const val UNKNOWN = "Unknown"

    private const val MAX_PORT = 65535
    private const val MIN_PORT = 0

    private const val MAX_PAGES = 20

    /**
     * Retrieves servers from the SA-MP masterlist for the given version.
     *
     * @param version to filter for
     * @return List of [SampServer] instances
     */
    fun retrieveMasterlistServers(version: String): List<SampServer> {
        val servers = ArrayList<SampServer>()
        try {
            val openConnection = URL("http://lists.sa-mp.com/$version/servers").openConnection()
            openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0")
            BufferedReader(InputStreamReader(openConnection.getInputStream())).use { `in` ->
                `in`.lines().forEach({ inputLine ->
                    val data = inputLine.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val server = SampServer(data[0], Integer.parseInt(data[1]))
                    if (!servers.contains(server)) {
                        servers.add(server)
                    }
                })
            }
        } catch (exception: IOException) {
            Logging.error("Error retrieving servers from masterlist.", exception)
        }

        return servers
    }

    @Throws(IOException::class)
    private fun readUrl(urlString: String): String {
        BufferedReader(InputStreamReader(URL(urlString).openStream())).use { reader ->
            val buffer = StringBuilder()

            reader.lines().forEach { buffer.append(it) }

            return buffer.toString()
        }
    }

    /**
     * Queries Southclaws Rest API for servers.
     *
     * @return a [List] of [SampServers][SampServer]
     * @throws IOException when querying Southclaws server has failed
     */
    @JvmStatic
    @Throws(IOException::class)
    fun fetchServersFromSouthclaws(): List<SampServer> {
        return fetchFromAPI("http://api.samp.southcla.ws/v2/servers")
    }

    @Throws(IOException::class)
    private fun fetchFromAPI(apiAddress: String): List<SampServer> {
        val servers = ArrayList<SampServer>()

        // The pages are 1 indexed, so we start at 1 and go up to the given max amount of pages
        for (page in 1 until MAX_PAGES) {
            val json = readUrl("$apiAddress?page=$page")

            // In case the page equals 'null', we have already received all data.
            if ("null".equals(json, ignoreCase = true)) {
                break
            }

            val jsonArray = Json.parse(json).asArray()

            jsonArray.forEach { `object` ->
                val jsonServerData = `object`.asObject()
                val addressData = jsonServerData.getString("ip", UNKNOWN).split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val port = if (addressData.size == 2) Integer.parseInt(addressData[1]) else ServerUtility.DEFAULT_SAMP_PORT
                val server = SampServer(addressData[0], port)

                server.players = jsonServerData.getInt("pc", 0)
                server.maxPlayers = jsonServerData.getInt("pm", 0)
                server.mode = jsonServerData.getString("gm", UNKNOWN)
                server.hostname = jsonServerData.getString("hn", UNKNOWN)
                server.language = jsonServerData.getString("la", UNKNOWN)
                server.version = jsonServerData.getString("vn", UNKNOWN)

                // If a server doesn't meet the following, it is invalid.
                if (!server.hostname.isEmpty() || server.players!! <= server.maxPlayers!!) {
                    servers.add(server)
                }
            }
        }

        return servers
    }

    /**
     * Validates the given port.
     *
     * @param portAsString the port to be validated
     * @return true if it is an integer and between 0 and 65535
     */
    fun isPortValid(portAsString: String): Boolean {
        val portNumber = StringUtility.parseInteger(portAsString) ?: -1
        return isPortValid(portNumber)
    }

    /**
     * Validates the given port.
     *
     * @param portNumber the port to be validated
     * @return true if it is between 0 and 65535
     */
    private fun isPortValid(portNumber: Int): Boolean {
        return portNumber in MIN_PORT..MAX_PORT
    }

    /**
     * Validates an IP-Address against a simple regex. Works only for IPv4.
     *
     * @param address the address that needs to be validated
     * @return true if the IP-Address was valid, otherwise false.
     */
    fun isValidIPAddress(address: String): Boolean {
        return address
                .matches("(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)".toRegex())
    }
}// Constructor to prevent instantiation