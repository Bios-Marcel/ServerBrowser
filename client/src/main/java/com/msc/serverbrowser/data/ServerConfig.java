package com.msc.serverbrowser.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.logging.Logging;

/**
 * Allows controller over server specific settings. TODO(MSC) I could still improve the setter
 * methods in case the table gets more fields.
 *
 * @author marcel
 * @since Jan 17, 2018
 */
public final class ServerConfig {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ServerConfig() {
		// Constructor to prevent instantiation.
	}

	/**
	 * Saves the last time a server has been joined.
	 *
	 * @param ip ip
	 * @param port port
	 * @param lastTimeJoined lastTimeJoined
	 * @return true if the action was a success, otherwise false
	 */
	public static boolean setLastTimeJoinedForServer(final String ip, final Integer port, final long lastTimeJoined) {
		final String query = "INSERT OR REPLACE INTO serverconfig (ip, port, username, lastJoin ) values (" + "?," // IP
				+ "?," // Port
				+ "(select username from serverconfig where ip=? and port=?)," // Username
				+ "?);"; // lastTimeJoined
		try (PreparedStatement statement = SQLDatabase.getInstance().createPreparedStatement(query)) {
			statement.setString(1, ip);
			statement.setInt(2, port);
			statement.setString(3, ip);
			statement.setInt(4, port);
			statement.setLong(5, lastTimeJoined);

			return statement.execute();
		}
		catch (final SQLException exception) {
			Logging.error("Error while setting last join time.", exception);
			return false;
		}
	}

	/**
	 * Sets the username to use when connect to to that specific server.
	 *
	 * @param ip server ip
	 * @param port server port
	 * @param username the username to be set
	 * @return true if the action was a success, otherwise false
	 */
	public boolean setUsernameForServer(final String ip, final Integer port, final String username) {
		final String query = "INSERT OR REPLACE INTO serverconfig (ip, port, username, lastJoin ) values (" + "?," // IP
				+ "?," // Port
				+ "?," // Username
				+ "(select lastjoin from serverconfig where ip=? and port=?));"; // lastTimeJoined
		try (PreparedStatement statement = SQLDatabase.getInstance().createPreparedStatement(query)) {
			statement.setString(1, ip);
			statement.setInt(2, port);
			statement.setString(3, username);
			statement.setString(4, ip);
			statement.setInt(5, port);

			return statement.execute();
		}
		catch (final SQLException exception) {
			Logging.error("Error while setting username.", exception);
			return false;
		}
	}

	/**
	 * Returns the username to use for a specific server or an empty {@link Optional} in case no
	 * username has been set.
	 *
	 * @param ip server ip
	 * @param port server port
	 * @return An {@link Optional} containing the to be used username or empty
	 */
	public Optional<String> getUsernameForServer(final String ip, final Integer port) {
		return getStringOfField(ip, port, "username");
	}

	/**
	 * Returns the username to use for a specific server or an empty {@link Optional} in case no
	 * username has been set.
	 *
	 * @param ip server ip
	 * @param port server port
	 * @return An {@link Optional} containing the to be used username or empty
	 */
	public static Optional<Long> getLastJoinForServer(final String ip, final Integer port) {
		return getStringOfField(ip, port, "lastJoin").map(Long::parseLong);
	}

	/**
	 * Returns a list of all {@link SampServer}s which have a lastJoin date, the returned data
	 * doesn't contain any other data whatsoever (hostname and so on).
	 *
	 * @return a {@link List} of all previously joined {@link SampServer}s
	 */
	public static List<SampServer> getLastJoinedServers() {
		final String statement = "SELECT ip, port, lastJoin FROM serverconfig WHERE lastJoin IS NOT NULL;";
		final Optional<ResultSet> resultSetOpt = SQLDatabase.getInstance().executeGetResult(statement);

		final List<SampServer> servers = new ArrayList<>();
		if (resultSetOpt.isPresent()) {
			try (ResultSet resultSet = resultSetOpt.get()) {
				while (resultSet.next()) {
					final SampServer server = new SampServer(resultSet.getString("ip"), resultSet.getInt("port"));
					server.setLastJoin(Long.parseLong(resultSet.getString("lastJoin")));
					servers.add(server);
				}
			}
			catch (final SQLException exception) {
				Logging.error("Error while retrieving previously joined servers.", exception);
			}
		}

		return servers;
	}

	/**
	 * Fills a {@link Collection} of servers with their last join date.
	 *
	 * @param servers servers to inject their last join date into
	 */
	public static void initLastJoinData(final Collection<SampServer> servers) {
		servers.forEach(server -> getLastJoinForServer(server.getAddress(), server.getPort()).ifPresent(server::setLastJoin));
	}

	private static Optional<String> getStringOfField(final String ip, final Integer port, final String field) {
		final String query = "SELECT " + field + " FROM serverconfig WHERE ip=? and port=? AND " + field + " IS NOT NULL;";

		try (PreparedStatement statement = SQLDatabase.getInstance().createPreparedStatement(query)) {

			statement.setString(1, ip);
			statement.setInt(2, port);

			final Optional<ResultSet> resultSetOpt = SQLDatabase.getInstance().executeGetResult(statement);

			if (resultSetOpt.isPresent()) {
				try (ResultSet resultSet = resultSetOpt.get()) {
					if (resultSet.next()) {
						return Optional.of(resultSet.getString(field));
					}
				}
				catch (final SQLException exception) {
					Logging.error("Error while retrieving field: '" + field + " of server: " + ip + ":" + port, exception);
				}
			}
		}
		catch (final SQLException exception) {
			Logging.error("Error getting field from server config.", exception);
		}

		return Optional.empty();
	}
}