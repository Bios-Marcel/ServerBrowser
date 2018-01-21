package com.msc.serverbrowser.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.StringUtility;

/**
 * Allows controller over server specific settings.
 * TODO(MSC) I could still improve the setter methods in case the table gets more fields.
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
	 */
	public static void setLastTimeJoinedForServer(final String ip, final Integer port, final Long lastTimeJoined) {
		final String statement = "INSERT OR REPLACE INTO serverconfig (ip, port, username, lastJoin ) values ("
				+ "''{0}''," // IP
				+ "{1}," // Port
				+ "(select username from serverconfig where ip=''{0}'' and port={1})," // Username
				+ "''{2}'');"; // lastTimeJoined
		final String filledStatement = StringUtility.escapeFormat(statement, ip, port, lastTimeJoined);
		SQLDatabase.getInstance().execute(filledStatement);
	}

	/**
	 * Sets the username to use when connect to to that specific server.
	 *
	 * @param ip server ip
	 * @param port server port
	 * @param username the username to be set
	 */
	public void setUsernameForServer(final String ip, final Integer port, final String username) {
		final String statement = "INSERT OR REPLACE INTO serverconfig (ip, port, username, lastJoin ) values ("
				+ "''{0}''," // IP
				+ "{1}," // Port
				+ "''{2}''," // Username
				+ "(select lastjoin from serverconfig where ip=''{0}'' and port={1}));"; // lastTimeJoined
		final String filledStatement = StringUtility.escapeFormat(statement, ip, port, username);
		SQLDatabase.getInstance().execute(filledStatement);
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
			try (final ResultSet resultSet = resultSetOpt.get()) {
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
	 * @param servers
	 */
	public static void initLastJoinData(final Collection<SampServer> servers) {
		servers.forEach(server -> {
			getLastJoinForServer(server.getAddress(), server.getPort())
					.ifPresent(server::setLastJoin);
		});
	}

	private static Optional<String> getStringOfField(final String ip, final Integer port, final String field) {
		final String statement = "SELECT " + field + " FROM serverconfig WHERE ip=''{0}'' and port={1} AND " + field + " IS NOT NULL;";
		final String filledStatement = StringUtility.escapeFormat(statement, ip, port);

		final Optional<ResultSet> resultSetOpt = SQLDatabase.getInstance().executeGetResult(filledStatement);

		if (resultSetOpt.isPresent()) {
			try (final ResultSet resultSet = resultSetOpt.get()) {
				if (resultSet.next()) {
					return Optional.of(resultSet.getString(field));
				}
			}
			catch (final SQLException exception) {
				Logging.error("Error while retrieving field: '" + field + " of server: " + ip + ":" + port, exception);
			}
		}

		return Optional.empty();
	}
}