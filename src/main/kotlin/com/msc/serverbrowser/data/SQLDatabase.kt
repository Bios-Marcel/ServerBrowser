package com.msc.serverbrowser.data

import com.msc.serverbrowser.constants.PathConstants
import com.msc.serverbrowser.logging.Logging
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Optional

/**
 * Class for accessing the local SQL Lite database.
 *
 * @author Marcel
 * @since 19.09.2017
 */
object SQLDatabase {

    private val DB_LOCATION = PathConstants.SAMPEX_PATH + File.separator + "samp.db"

    /**
     * Private Constructor to keep outsiders from instantiating this class.
     */
    private lateinit var sqlConnection: Connection

    /**
     * Establishes the connection and creates the necessary tables if they don't exist.
     */
    init {
        try {
            sqlConnection = DriverManager.getConnection("jdbc:sqlite:$DB_LOCATION")

            sqlConnection.createStatement().use { statement ->
                // TODO(MSC) favourites could be merged with serverconfig and use the
                // servercache
                val createTableFavourites = "CREATE TABLE IF NOT EXISTS favourite (hostname TEXT, ip TEXT NOT NULL, lagcomp TEXT, language TEXT, players INTEGER, maxplayers integer, mode TEXT, port INTEGER, version TEXT, website TEXT);"
                statement.execute(createTableFavourites)

                // TODO SOON!
                // final String createTableServerCache = "CREATE TABLE IF NOT EXISTS servercache
                // (ip
                // TEXT NOT NULL, port INTEGER NOT NULL, hostname TEXT, language TEXT, players
                // INTEGER, maxplayers INTEGER, mode TEXT, version TEXT, PRIMARY KEY(ip,
                // port));";
                // statement.execute(createTableServerCache);

                val createTableServerConfig = "CREATE TABLE IF NOT EXISTS serverconfig (ip TEXT NOT NULL, port INTEGER, username TEXT, lastJoin TEXT, PRIMARY KEY(ip, port));"
                statement.execute(createTableServerConfig)

                val createTableUsernames = "CREATE TABLE IF NOT EXISTS username (id INTEGER PRIMARY KEY, username TEXT NOT NULL);"
                statement.execute(createTableUsernames)

                val createTableSettings = "CREATE TABLE IF NOT EXISTS setting (id INTEGER PRIMARY KEY, value TEXT);"
                statement.execute(createTableSettings)
            }
        } catch (exception: SQLException) {
            Logging.error("Error while initializing local Database connection.", exception)
        }

    }

    /**
     * @param query the query to be used for the [PreparedStatement]
     * @return a [PreparedStatement] using the given query
     * @throws SQLException if a database access error occurs or this method is called on a closed
     * connection
     */
    @Throws(SQLException::class)
    fun createPreparedStatement(query: String): PreparedStatement {
        return sqlConnection.prepareStatement(query)
    }

    /**
     * Executes a query on the local sqllite db.
     *
     * @param statement the statement to execute
     * @return `true` if successful and `false` otherwise
    `` */
    fun execute(statement: String): Boolean {
        try {
            return sqlConnection.createStatement().execute(statement)
        } catch (exception: SQLException) {
            Logging.error("Couldn't execute query.", exception)
            return false
        }

    }

    /**
     * Executes a query on the local sqllite and returns the results. A [PreparedStatement] is
     * created by using the given [String].
     *
     * @param statement the statement to execute
     * @return a [Optional] containing a [ResultSet] or an empty [Optional].
     */
    fun executeGetResult(statement: String): ResultSet? {
        return try {
            executeGetResult(sqlConnection.prepareStatement(statement))
        } catch (exception: SQLException) {
            Logging.error("Failed to execute SQL query!", exception)
            null
        }

    }

    /**
     * Executes a query on the local sqllite and returns the results.
     *
     * @param statement the statement to execute
     * @return a [Optional] containing a [ResultSet] or an empty [Optional].
     */
    fun executeGetResult(statement: PreparedStatement): ResultSet? {
        return try {
            statement.executeQuery()
        } catch (exception: SQLException) {
            Logging.error("Failed to execute SQL query!", exception)
            null
        }
    }
}
