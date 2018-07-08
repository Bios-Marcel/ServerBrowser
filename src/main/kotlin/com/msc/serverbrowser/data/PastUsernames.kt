package com.msc.serverbrowser.data

import com.msc.serverbrowser.severe
import java.sql.SQLException
import java.text.MessageFormat
import java.util.ArrayList

/**
 * Contains methods for adding and removing past usernames.
 *
 * @author Marcel
 */
object PastUsernames {

    /**
     * @return a [List] containing all past usernames
     */
    val pastUsernames: List<String>
        get() {
            val usernames = ArrayList<String>()

            val resultSet = SQLDatabase.executeGetResult("SELECT username FROM username;")
            if (resultSet != null) {
                try {
                    while (resultSet.next()) {
                        usernames.add(resultSet.getString("username"))
                    }
                } catch (exception: SQLException) {
                    severe("Error while retrieving past usernames", exception)
                }
            }

            return usernames
        }

    /**
     * Adds a username to the past usernames list.
     *
     * @param username the username to add
     */
    fun addPastUsername(username: String) {
        if (!pastUsernames.contains(username)) {
            var statement = "INSERT INTO username (username) VALUES (''{0}'');"
            statement = MessageFormat.format(statement, username)
            SQLDatabase.execute(statement)
        }
    }

    /**
     * Removes a username from the past usernames list.
     *
     * @param username the username to removes
     */
    fun removePastUsername(username: String) {
        var statement = "DELETE FROM username WHERE username = ''{0}'';"
        statement = MessageFormat.format(statement, username)
        SQLDatabase.execute(statement)
    }
}
