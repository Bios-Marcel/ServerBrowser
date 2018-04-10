package com.msc.serverbrowser.data

import com.msc.serverbrowser.constants.PathConstants
import com.msc.serverbrowser.data.entites.SampServer
import com.msc.serverbrowser.logging.Logging
import com.msc.serverbrowser.util.samp.SampQuery
import java.io.IOException
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.SQLException
import java.util.ArrayList

/**
 * Contains static methods for setting and retrieving favourite servers
 *
 * @author Marcel
 */
object FavouritesController {
    private const val UNKNOWN = "Unknown"

    /**
     * Returns a [List] of favourite servers.
     *
     * @return [List] of favourite servers
     */
    val favourites: List<SampServer>
        get() {
            val servers = ArrayList<SampServer>()

            val resultSet = SQLDatabase.executeGetResult("SELECT * FROM favourite;")
            if (resultSet != null) {
                try {
                    while (resultSet.next()) {
                        val server = SampServer(resultSet.getString("ip"), resultSet.getInt("port"))
                        server.hostname = resultSet.getString("hostname")
                        server.players = resultSet.getInt("players")
                        server.maxPlayers = resultSet.getInt("maxplayers")
                        server.mode = resultSet.getString("mode")
                        server.language = resultSet.getString("language")
                        server.website = resultSet.getString("website")
                        server.lagcomp = resultSet.getString("lagcomp")
                        server.version = resultSet.getString("version")
                        servers.add(server)
                    }
                } catch (exception: SQLException) {
                    Logging.error("Error while retrieving favourites", exception)
                }
            }

            return servers
        }

    /**
     * Adds a new server to the favourites and downloads its information.
     *
     * @param address the address of the server
     * @param port the port of the server
     * @return the server object that was created
     */
    fun addServerToFavourites(address: String, port: Int): SampServer {
        val server = SampServer(address, port)
        try {
            SampQuery(address, port).use { query ->
                query.basicServerInfo.ifPresent({ serverInfo ->
                    server.players = Integer.parseInt(serverInfo[1])
                    server.maxPlayers = Integer.parseInt(serverInfo[2])
                    server.hostname = serverInfo[3]!!
                    server.mode = serverInfo[4]!!
                    server.language = serverInfo[5]!!
                })

                query.serversRules.ifPresent({ rules ->
                    server.website = rules["weburl"]!!
                    server.version = rules["version"]!!
                })
            }
        } catch (exception: SocketException) {
            Logging.warn("Error updating server information.", exception)
            server.hostname = UNKNOWN
            server.language = UNKNOWN
            server.mode = UNKNOWN
            server.website = UNKNOWN
            server.version = UNKNOWN
            server.lagcomp = UNKNOWN
            server.players = 0
            server.maxPlayers = 0
        } catch (exception: UnknownHostException) {
            Logging.warn("Error updating server information.", exception)
            server.hostname = UNKNOWN
            server.language = UNKNOWN
            server.mode = UNKNOWN
            server.website = UNKNOWN
            server.version = UNKNOWN
            server.lagcomp = UNKNOWN
            server.players = 0
            server.maxPlayers = 0
        }

        Logging.info("Adding server to favourites: $server")
        addServerToFavourites(server)
        return server
    }

    /**
     * Adds a server to the favourites.
     *
     * @param server the server to add to the favourites
     * @return true if the action was a success, otherwise false
     */
    fun addServerToFavourites(server: SampServer): Boolean {
        if (isFavourite(server)) {
            Logging.info("Server wasn't added, because it already is a favourite.")
        } else {
            val query = "INSERT INTO favourite(hostname, ip, lagcomp, language, players, maxplayers, mode, port, version, website) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
            try {
                SQLDatabase.createPreparedStatement(query).use { statement ->
                    statement.setString(1, server.hostname)
                    statement.setString(2, server.address)
                    statement.setString(3, server.lagcomp)
                    statement.setString(4, server.language)
                    statement.setInt(5, server.players!!)
                    statement.setInt(6, server.maxPlayers!!)
                    statement.setString(7, server.mode)
                    statement.setInt(8, server.port)
                    statement.setString(9, server.version)
                    statement.setString(10, server.website)

                    return statement.execute()
                }
            } catch (exception: SQLException) {
                Logging.error("Error while adding server to favourites.", exception)
            }

        }

        return false
    }

    /**
     * Checks whether a server is favourite.
     *
     * @param server server to check if it is a favourite
     * @return true if it is, false otherwise
     */
    private fun isFavourite(server: SampServer): Boolean {
        return favourites.contains(server)
    }

    /**
     * Updates a servers info(data) in the database.
     *
     * @param server the server to update
     * @return true if the action was a success, otherwise false
     */
    fun updateServerData(server: SampServer): Boolean {
        val query = "UPDATE favourite SET hostname = ?, lagcomp = ?, language = ?, players = ?, maxplayers = ?, mode = ?, version = ?, website = ? WHERE ip = ? AND port = ?;"
        try {
            SQLDatabase.createPreparedStatement(query).use { statement ->
                statement.setString(1, server.hostname)
                statement.setString(2, server.lagcomp)
                statement.setString(3, server.language)
                statement.setInt(4, server.players!!)
                statement.setInt(5, server.maxPlayers!!)
                statement.setString(6, server.mode)
                statement.setString(7, server.version)
                statement.setString(8, server.website)
                statement.setString(9, server.address)
                statement.setInt(10, server.port)

                return statement.execute()
            }
        } catch (exception: SQLException) {
            Logging.error("Error while updaing server in favourites.", exception)
            return false
        }

    }

    /**
     * Removes a server from favourites.
     *
     * @param server the server to remove from favourites
     * @return true if the action was a success, otherwise false
     */
    fun removeServerFromFavourites(server: SampServer): Boolean {
        val query = "DELETE FROM favourite WHERE ip = ? AND port = ?;"

        try {
            SQLDatabase.createPreparedStatement(query).use { statement ->

                statement.setString(1, server.address)
                statement.setInt(2, server.port)

                return statement.execute()
            }
        } catch (exception: SQLException) {
            Logging.error("Error while deleting server from favourites.", exception)
            return false
        }

    }

    /**
     * TODO (Marcel 13.01.2018) I am still not using this ... why
     *
     * @return the List of all SampServers that the legacy favourite file contains.
     */
    fun retrieveLegacyFavourites(): List<SampServer> {
        val legacyFavourites = ArrayList<SampServer>()

        try {
            val data = Files.readAllBytes(Paths.get(PathConstants.SAMP_USERDATA))
            val buffer = ByteBuffer.wrap(data)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            // Skipping trash at the beginning
            buffer.position(buffer.position() + 8)

            val serverCount = buffer.int
            for (i in 0 until serverCount) {
                val ipBytes = ByteArray(buffer.int)
                buffer.get(ipBytes)
                val ip = String(ipBytes, StandardCharsets.US_ASCII)

                val port = buffer.int

                /* Skip unimportant stuff */
                var skip = buffer.int // Hostname
                buffer.position(buffer.position() + skip)
                skip = buffer.int // Rcon pw
                buffer.position(buffer.position() + skip)
                skip = buffer.int // Server pw
                buffer.position(buffer.position() + skip)

                legacyFavourites.add(SampServer(ip, port))
            }

            return legacyFavourites
        } catch (exception: IOException) {
            Logging.warn("Error loading legacy favourites.", exception)
            return legacyFavourites
        }
    }
}
