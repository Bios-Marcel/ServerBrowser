package com.msc.serverbrowser.data;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.samp.SampQuery;

/**
 * Contains static methods for setting and retrieving favourite servers
 *
 * @author Marcel
 */
public final class FavouritesController {
	private static final String UNKNOWN = "Unknown";

	private FavouritesController() {
		// Constructor to prevent instantiation
	}

	/**
	 * Adds a new server to the favourites and downloads its information.
	 *
	 * @param address the address of the server
	 * @param port the port of the server
	 * @return the server object that was created
	 */
	public static SampServer addServerToFavourites(final String address, final Integer port) {
		final SampServer server = new SampServer(address, port);
		try (SampQuery query = new SampQuery(address, port))

		{
			query.getBasicServerInfo().ifPresent(serverInfo -> {
				server.setPlayers(Integer.parseInt(serverInfo[1]));
				server.setMaxPlayers(Integer.parseInt(serverInfo[2]));
				server.setHostname(serverInfo[3]);
				server.setMode(serverInfo[4]);
				server.setLanguage(serverInfo[5]);
			});

			query.getServersRules().ifPresent(rules -> {
				server.setWebsite(rules.get("weburl"));
				server.setVersion(rules.get("version"));
			});
		}
		catch (final SocketException | UnknownHostException exception) {
			Logging.warn("Error updating server information.", exception);
			server.setHostname(UNKNOWN);
			server.setLanguage(UNKNOWN);
			server.setMode(UNKNOWN);
			server.setWebsite(UNKNOWN);
			server.setVersion(UNKNOWN);
			server.setLagcomp(UNKNOWN);
			server.setPlayers(0);
			server.setMaxPlayers(0);
		}

		Logging.info("Adding server to favourites: " + server);
		addServerToFavourites(server);
		return server;
	}

	/**
	 * Adds a server to the favourites.
	 *
	 * @param server the server to add to the favourites
	 * @return true if the action was a success, otherwise false
	 */
	public static boolean addServerToFavourites(final SampServer server) {
		if (isFavourite(server)) {
			Logging.info("Server wasn't added, because it already is a favourite.");
		}
		else {
			final String query = "INSERT INTO favourite(hostname, ip, lagcomp, language, players, maxplayers, mode, port, version, website) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
			try (PreparedStatement statement = SQLDatabase.getInstance().createPreparedStatement(query)) {
				statement.setString(1, server.getHostname());
				statement.setString(2, server.getAddress());
				statement.setString(3, server.getLagcomp());
				statement.setString(4, server.getLanguage());
				statement.setInt(5, server.getPlayers());
				statement.setInt(6, server.getMaxPlayers());
				statement.setString(7, server.getMode());
				statement.setInt(8, server.getPort());
				statement.setString(9, server.getVersion());
				statement.setString(10, server.getWebsite());

				return statement.execute();
			}
			catch (final SQLException exception) {
				Logging.error("Error while adding server to favourites.", exception);
			}
		}

		return false;
	}

	/**
	 * Checks whether a server is favourite.
	 *
	 * @param server server to check if it is a favourite
	 * @return true if it is, false otherwise
	 */
	public static boolean isFavourite(final SampServer server) {
		return getFavourites().contains(server);
	}

	/**
	 * Updates a servers info(data) in the database.
	 *
	 * @param server the server to update
	 * @return true if the action was a success, otherwise false
	 */
	public static boolean updateServerData(final SampServer server) {
		final String query = "UPDATE favourite SET hostname = ?, lagcomp = ?, language = ?, players = ?, maxplayers = ?, mode = ?, version = ?, website = ? WHERE ip = ? AND port = ?;";
		try (PreparedStatement statement = SQLDatabase.getInstance().createPreparedStatement(query)) {
			statement.setString(1, server.getHostname());
			statement.setString(2, server.getLagcomp());
			statement.setString(3, server.getLanguage());
			statement.setInt(4, server.getPlayers());
			statement.setInt(5, server.getMaxPlayers());
			statement.setString(6, server.getMode());
			statement.setString(7, server.getVersion());
			statement.setString(8, server.getWebsite());
			statement.setString(9, server.getAddress());
			statement.setInt(10, server.getPort());

			return statement.execute();
		}
		catch (final SQLException exception) {
			Logging.error("Error while updaing server in favourites.", exception);
			return false;
		}
	}

	/**
	 * Removes a server from favourites.
	 *
	 * @param server the server to remove from favourites
	 * @return true if the action was a success, otherwise false
	 */
	public static boolean removeServerFromFavourites(final SampServer server) {
		final String query = "DELETE FROM favourite WHERE ip = ? AND port = ?;";

		try (PreparedStatement statement = SQLDatabase.getInstance().createPreparedStatement(query)) {

			statement.setString(1, server.getAddress());
			statement.setInt(2, server.getPort());

			return statement.execute();
		}
		catch (final SQLException exception) {
			Logging.error("Error while deleting server from favourites.", exception);
			return false;
		}
	}

	/**
	 * Returns a {@link List} of favourite servers.
	 *
	 * @return {@link List} of favourite servers
	 */
	public static List<SampServer> getFavourites() {
		final List<SampServer> servers = new ArrayList<>();

		SQLDatabase.getInstance().executeGetResult("SELECT * FROM favourite;").ifPresent(resultSet -> {
			try {
				while (resultSet.next()) {
					final SampServer server = new SampServer(resultSet.getString("ip"), resultSet.getInt("port"));
					server.setHostname(resultSet.getString("hostname"));
					server.setPlayers(resultSet.getInt("players"));
					server.setMaxPlayers(resultSet.getInt("maxplayers"));
					server.setMode(resultSet.getString("mode"));
					server.setLanguage(resultSet.getString("language"));
					server.setWebsite(resultSet.getString("website"));
					server.setLagcomp(resultSet.getString("lagcomp"));
					server.setVersion(resultSet.getString("version"));
					servers.add(server);
				}
			}
			catch (final SQLException exception) {
				Logging.error("Error while retrieving favourites", exception);
			}
		});

		return servers;
	}

	/**
	 * TODO (Marcel 13.01.2018) I am still not using this ... why
	 *
	 * @return the List of all SampServers that the legacy favourite file contains.
	 */
	public static List<SampServer> retrieveLegacyFavourites() {
		final List<SampServer> legacyFavourites = new ArrayList<>();

		try {
			final byte[] data = Files.readAllBytes(Paths.get(PathConstants.SAMP_USERDATA));
			final ByteBuffer buffer = ByteBuffer.wrap(data);
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			// Skiping trash at the beginning
			buffer.position(buffer.position() + 8);

			final int serverCount = buffer.getInt();
			for (int i = 0; i < serverCount; i++) {
				final byte[] ipBytes = new byte[buffer.getInt()];
				buffer.get(ipBytes);
				final String ip = new String(ipBytes, StandardCharsets.US_ASCII);

				final int port = buffer.getInt();

				/* Skip unimportant stuff */
				int skip = buffer.getInt(); // Hostname
				buffer.position(buffer.position() + skip);
				skip = buffer.getInt(); // Rcon pw
				buffer.position(buffer.position() + skip);
				skip = buffer.getInt(); // Server pw
				buffer.position(buffer.position() + skip);

				legacyFavourites.add(new SampServer(ip, port));
			}

			return legacyFavourites;
		}
		catch (final IOException exception) {
			Logging.warn("Error loading legacy favourites.", exception);
			return legacyFavourites;
		}
	}
}
