package com.msc.serverbrowser.data

import com.msc.serverbrowser.data.entites.SampServer
import com.msc.serverbrowser.severe
import java.sql.SQLException
import java.util.ArrayList
import java.util.Optional

/**
 * Allows controller over server specific settings. TODO(MSC) I could still improve the setter
 * methods in case the table gets more fields.
 *
 * @author marcel
 * @since Jan 17, 2018
 */
object ServerConfig {

    /**
     * Sets the username to use when connect to to that specific server.
     *
     * @param ip server ip
     * @param port server port
     * @param username the username to be set
     * @return true if the action was a success, otherwise false
     */
    fun setUsernameForServer(ip: String, port: Int, username: String): Boolean {
        val query = ("INSERT OR REPLACE INTO serverconfig (ip, port, username, lastJoin ) values (" + "?," // IP

                + "?," // Port

                + "?," // Username

                + "(select lastjoin from serverconfig where ip=? and port=?));") // lastTimeJoined
        try {
            SQLDatabase.createPreparedStatement(query).use { statement ->
                statement.setString(1, ip)
                statement.setInt(2, port)
                statement.setString(3, username)
                statement.setString(4, ip)
                statement.setInt(5, port)

                return statement.execute()
            }
        } catch (exception: SQLException) {
            severe("Error while setting username.", exception)
            return false
        }

    }

    /**
     * Returns the username to use for a specific server or an empty [Optional] in case no
     * username has been set.
     *
     * @param ip server ip
     * @param port server port
     * @return An [Optional] containing the to be used username or empty
     */
    fun getUsernameForServer(ip: String, port: Int): Optional<String> {
        return getStringOfField(ip, port, "username")
    }

    /**
     * Saves the last time a server has been joined.
     *
     * @param ip ip
     * @param port port
     * @param lastTimeJoined lastTimeJoined
     * @return true if the action was a success, otherwise false
     */
    fun setLastTimeJoinedForServer(ip: String, port: Int, lastTimeJoined: Long): Boolean {
        val query = ("INSERT OR REPLACE INTO serverconfig (ip, port, username, lastJoin ) values (" + "?," // IP

                + "?," // Port

                + "(select username from serverconfig where ip=? and port=?)," // Username

                + "?);") // lastTimeJoined
        try {
            SQLDatabase.createPreparedStatement(query).use { statement ->
                statement.setString(1, ip)
                statement.setInt(2, port)
                statement.setString(3, ip)
                statement.setInt(4, port)
                statement.setLong(5, lastTimeJoined)

                return statement.execute()
            }
        } catch (exception: SQLException) {
            severe("Error while setting last join time.", exception)
            return false
        }

    }

    /**
     * Returns the username to use for a specific server or an empty [Optional] in case no
     * username has been set.
     *
     * @param ip server ip
     * @param port server port
     * @return An [Optional] containing the to be used username or empty
     */
    private fun getLastJoinForServer(ip: String, port: Int): Optional<Long> {
        return getStringOfField(ip, port, "lastJoin").map({ it.toLong() })
    }

    /**
     * Returns a list of all [SampServer]s which have a lastJoin date, the returned data
     * doesn't contain any other data whatsoever (hostname and so on).
     *
     * @return a [List] of all previously joined [SampServer]s
     */
    val lastJoinedServers: List<SampServer>
        get() {
            val statement = "SELECT ip, port, lastJoin FROM serverconfig WHERE lastJoin IS NOT NULL;"
            val resultSet = SQLDatabase.executeGetResult(statement)

            val servers = ArrayList<SampServer>()
            if (resultSet != null) {
                try {
                    resultSet.use {
                        while (it.next()) {
                            val server = SampServer(it.getString("ip"), it.getInt("port"))
                            server.lastJoin = java.lang.Long.parseLong(it.getString("lastJoin"))
                            servers.add(server)
                        }
                    }
                } catch (exception: SQLException) {
                    severe("Error while retrieving previously joined servers.", exception)
                }

            }

            return servers
        }

    /**
     * Fills a [Collection] of servers with their last join date.
     *
     * @param servers servers to inject their last join date into
     */
    fun initLastJoinData(servers: Collection<SampServer>) {
        servers.forEach { server -> getLastJoinForServer(server.address, server.port).ifPresent({ server.lastJoin = it }) }
    }

    private fun getStringOfField(ip: String, port: Int, field: String): Optional<String> {
        val query = "SELECT $field FROM serverconfig WHERE ip=? and port=? AND $field IS NOT NULL;"

        try {
            SQLDatabase.createPreparedStatement(query).use { statement ->

                statement.setString(1, ip)
                statement.setInt(2, port)

                val resultSet = SQLDatabase.executeGetResult(statement)

                if (resultSet != null) {
                    try {
                        resultSet.use({
                            if (it.next()) {
                                return Optional.of(it.getString(field))
                            }
                        })
                    } catch (exception: SQLException) {
                        severe("Error while retrieving field: '$field of server: $ip:$port", exception)
                    }

                }
            }
        } catch (exception: SQLException) {
            severe("Error getting field from server config.", exception)
        }

        return Optional.empty()
    }
}