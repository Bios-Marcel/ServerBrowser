package com.msc.serverbrowser.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
	 * Establishes the connection and creates the necessary tables if they don't
	 * exist.
	 */
	private void init() {
		try {
			sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + DB_LOCATION);

			try (final Statement statement = sqlConnection.createStatement()) {
				// TODO(MSC) favourites could be merged with serverconfig and use the
				// servercache
				final String createTableFavourites = "CREATE TABLE IF NOT EXISTS favourite (hostname TEXT, ip TEXT NOT NULL, lagcomp TEXT, language TEXT, players INTEGER, maxplayers integer, mode TEXT, port INTEGER, version TEXT, website TEXT);";
				statement.execute(createTableFavourites);

				// TODO SOON!
				// final String createTableServerCache = "CREATE TABLE IF NOT EXISTS servercache
				// (ip
				// TEXT NOT NULL, port INTEGER NOT NULL, hostname TEXT, language TEXT, players
				// INTEGER, maxplayers INTEGER, mode TEXT, version TEXT, PRIMARY KEY(ip,
				// port));";
				// statement.execute(createTableServerCache);

				final String createTableServerConfig = "CREATE TABLE IF NOT EXISTS serverconfig (ip TEXT NOT NULL, port INTEGER, username TEXT, lastJoin TEXT, PRIMARY KEY(ip, port));";
				statement.execute(createTableServerConfig);

				final String createTableUsernames = "CREATE TABLE IF NOT EXISTS username (id INTEGER PRIMARY KEY, username TEXT NOT NULL);";
				statement.execute(createTableUsernames);

				final String createTableSettings = "CREATE TABLE IF NOT EXISTS setting (id INTEGER PRIMARY KEY, value TEXT);";
				statement.execute(createTableSettings);
			}
		}
		catch (final SQLException exception) {
			Logging.error("Error while initializing local Database connection.", exception);
		}
	}

	/**
	 * @param query the query to be used for the {@link PreparedStatement}
	 * @return a {@link PreparedStatement} using the given query
	 * @throws SQLException if a database access error occurs or this method is called on a closed
	 *             connection
	 */
	public PreparedStatement createPreparedStatement(final String query) throws SQLException {
		return sqlConnection.prepareStatement(query);
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
	 * Executes a query on the local sqllite and returns the results. A
	 * {@link PreparedStatement} is created by using the given {@link String}.
	 *
	 * @param statement
	 *            the statement to execute
	 * @return a {@link Optional} containing a {@link ResultSet} or an empty
	 *         {@link Optional}.
	 */
	public Optional<ResultSet> executeGetResult(final String statement) {
		try {
			return executeGetResult(sqlConnection.prepareStatement(statement));
		}
		catch (final SQLException exception) {
			Logging.error("Failed to execute SQL query!", exception);
			return Optional.empty();
		}
	}

	/**
	 * Executes a query on the local sqllite and returns the results.
	 *
	 * @param statement
	 *            the statement to execute
	 * @return a {@link Optional} containing a {@link ResultSet} or an empty
	 *         {@link Optional}.
	 */
	public Optional<ResultSet> executeGetResult(final PreparedStatement statement) {
		try {
			return Optional.ofNullable(statement.executeQuery());
		}
		catch (final SQLException exception) {
			Logging.error("Failed to execute SQL query!", exception);
			return Optional.empty();
		}
	}
}
