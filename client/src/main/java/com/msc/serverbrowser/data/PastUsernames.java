package com.msc.serverbrowser.data;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.msc.serverbrowser.logging.Logging;

/**
 * Contains methods for adding and removing past usernames.
 *
 * @author Marcel
 */
public final class PastUsernames {
	private PastUsernames() {
		// Constructor to prevent instantiation
	}

	/**
	 * Adds a username to the past usernames list.
	 *
	 * @param username
	 *            the username to add
	 */
	public static void addPastUsername(final String username) {
		if (!getPastUsernames().contains(username)) {
			String statement = "INSERT INTO username (username) VALUES (''{0}'');";
			statement = MessageFormat.format(statement, username);
			SQLDatabase.getInstance().execute(statement);
		}
	}

	/**
	 * Removes a username from the past usernames list.
	 *
	 * @param username
	 *            the username to removes
	 */
	public static void removePastUsername(final String username) {
		String statement = "DELETE FROM username WHERE username = ''{0}'';";
		statement = MessageFormat.format(statement, username);
		SQLDatabase.getInstance().execute(statement);
	}

	/**
	 * @return a {@link List} containing all past usernames
	 */
	public static List<String> getPastUsernames() {
		final List<String> usernames = new ArrayList<>();

		SQLDatabase.getInstance().executeGetResult("SELECT username FROM username;").ifPresent(resultSet -> {
			try {
				while (resultSet.next()) {
					usernames.add(resultSet.getString("username"));
				}
			}
			catch (final SQLException exception) {
				Logging.error("Error while retrieving past usernames", exception);
			}
		});

		return usernames;
	}
}
