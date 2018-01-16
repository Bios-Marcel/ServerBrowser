package com.msc.serverbrowser.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;

import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.logging.Logging;

/**
 * Class for accessing the local SQL Lite database.
 *
 * @author Marcel
 * @since 19.09.2017
 */
public final class SQLDatabase {
	private static final String DB_LOCATION = PathConstants.SAMPEX_PATH + File.separator + "samp.db";

	private static SQLDatabase	instance;
	private Connection			sqlConnection;

	/**
	 * @return the singleton instance of this class
	 */
	public static SQLDatabase getInstance() {
		if (Objects.isNull(instance)) {
			instance = new SQLDatabase();
		}

		return instance;
	}

	/**
	 * Private Constructor to keep outsiders from instantiating this class.
	 */
	private SQLDatabase() {
		init();
	}

	/**
	 * Establishes the connection and creates the necessary tables if they don't exist.
	 */
	private void init() {
		try {
			sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + DB_LOCATION);

			try (final Statement statement = sqlConnection.createStatement()) {
				final String createTableFavourites = "CREATE TABLE IF NOT EXISTS favourite (hostname text, ip text NOT NULL, lagcomp text, language text, players integer, maxplayers integer, mode text, port integer, version text, website text);";
				statement.execute(createTableFavourites);

				final String createTableUsernames = "CREATE TABLE IF NOT EXISTS username (id integer PRIMARY KEY, username text NOT NULL);";
				statement.execute(createTableUsernames);

				final String createTableSettings = "CREATE TABLE IF NOT EXISTS setting (id integer UNIQUE, value text);";
				statement.execute(createTableSettings);

				// TODO(MSC) Implement
				// final String createTableServerHistory = "CREATE TABLE IF NOT EXISTS favourite
				// (hostname text, ip text NOT NULL, lagcomp text, language text, players integer,
				// maxplayers integer, mode text, port integer, version text, website text);";
				// statement.execute(createTableServerHistory);
			}
		}
		catch (final SQLException exception) {
			Logging.error("Error while initializing local Database connection.", exception);
		}
	}

	/**
	 * Executes a query on the local sqllite db.
	 *
	 * @param statement
	 *            the statement to execute
	 * @return <code>true</code> if successful and <code>false<code> otherwise
	 */
	public boolean execute(final String statement) {
		try {
			return sqlConnection.createStatement().execute(statement);
		}
		catch (final SQLException exception) {
			Logging.error("Couldn't execute query.", exception);
			return false;
		}
	}

	/**
	 * Executes a query on the local sqllite and returns the results.
	 *
	 * @param statement
	 *            the statement to execute
	 * @return a {@link Optional} containing a {@link ResultSet} or an empty {@link Optional}.
	 */
	public Optional<ResultSet> executeGetResult(final String statement) {
		try {
			return Optional.of(sqlConnection.prepareStatement(statement).executeQuery());
		}
		catch (final SQLException exception) {
			Logging.error("Failed to execute SQL query!", exception);
			return Optional.empty();
		}
	}
}
