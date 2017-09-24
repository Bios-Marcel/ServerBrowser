package com.msc.serverbrowser.data;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.samp.SampQuery;

/**
 * Contains static methods for setting and retrieving favourite servers
 *
 * @author Marcel
 */
public final class FavouritesController
{
	private static final String UNKNOWN = "Unknown";

	private FavouritesController()
	{
		// Constructor to prevent instantiation
	}

	/**
	 * Adds a new server to the favourites and downloads its information.
	 *
	 * @param address
	 *            the address of the server
	 * @param port
	 *            the port of the server
	 * @return the server object that was created
	 */
	public static SampServer addServerToFavourites(final String address, final Integer port)
	{
		final SampServer server = new SampServer(address, port);
		try (final SampQuery query = new SampQuery(server.getAddress(), server.getPort()))

		{
			query.getBasicServerInfo().ifPresent(serverInfo ->
			{
				server.setPlayers(Integer.parseInt(serverInfo[1]));
				server.setMaxPlayers(Integer.parseInt(serverInfo[2]));
				server.setHostname(serverInfo[3]);
				server.setMode(serverInfo[4]);
				server.setLanguage(serverInfo[5]);
			});

			query.getServersRules().ifPresent(rules ->
			{
				server.setWebsite(rules.get("weburl"));
				server.setVersion(rules.get("version"));
			});
		}
		catch (final SocketException | UnknownHostException exception)
		{
			Logging.log(Level.WARNING, "Error updating server information.", exception);
			server.setHostname(UNKNOWN);
			server.setLanguage(UNKNOWN);
			server.setMode(UNKNOWN);
			server.setWebsite(UNKNOWN);
			server.setVersion(UNKNOWN);
			server.setLagcomp(UNKNOWN);
			server.setPlayers(0);
			server.setMaxPlayers(0);
		}

		Logging.log(Level.INFO, "Adding server to favourites: " + server);
		addServerToFavourites(server);
		return server;
	}

	/**
	 * Adds a server to the favourites.
	 *
	 * @param server
	 *            the server to add to the favourites
	 */
	public static void addServerToFavourites(final SampServer server)
	{
		if (isFavourite(server))
		{
			Logging.info("Server wasn't added, because it already is a favourite.");
		}
		else
		{
			String statement = "INSERT INTO favourite(hostname, ip, lagcomp, language, players, maxplayers, mode, port, version, website) VALUES (''{0}'', ''{1}'', ''{2}'', ''{3}'', {4}, {5}, ''{6}'', {7}, ''{8}'', ''{9}'');";
			statement = escapeFormat(statement, server.getHostname(), server.getAddress(), server.getLagcomp(), server.getLanguage(), server.getPlayers().toString(), server
					.getMaxPlayers().toString(), server.getMode(), server.getPort().toString(), server.getVersion(), server.getWebsite());
			SQLDatabase.getInstance().execute(statement);
		}
	}

	/**
	 * Checks whether a server is favourite.
	 *
	 * @param server
	 *            server to check if it is a favourite
	 * @return true if it is, false otherwise
	 */
	public static boolean isFavourite(final SampServer server)
	{
		return getFavourites().contains(server);
	}

	/**
	 * Updates a servers info(data) in the database.
	 *
	 * @param server
	 *            the server to update
	 */
	public static void updateServerData(final SampServer server)
	{
		String statement = "UPDATE favourite SET hostname = ''{0}'', lagcomp = ''{1}'', language = ''{2}'', players = {3}, maxplayers = {4}, mode = ''{5}'', version = ''{6}'', website = ''{7}'' WHERE ip = ''{8}'' AND port = {9};";
		statement = escapeFormat(statement, server.getHostname(), server.getLagcomp(), server.getLanguage(), server.getPlayers().toString(), server.getMaxPlayers()
				.toString(), server.getMode(), server.getVersion(), server.getWebsite(), server.getAddress(), server.getPort().toString());
		SQLDatabase.getInstance().execute(statement);
	}

	private static String escapeFormat(final String string, final String... replacements)
	{
		final String[] replacementsNew = new String[replacements.length];
		for (int i = 0; i < replacements.length; i++)
		{
			final String replacementValue = replacements[i];
			if (Objects.nonNull(replacementValue))
			{
				replacementsNew[i] = replacementValue.replace("'", "''");
			}
		}
		return MessageFormat.format(string, (Object[]) replacementsNew);
	}

	/**
	 * Removes a server from favourites.
	 *
	 * @param server
	 *            the server to remove from favourites
	 */
	public static void removeServerFromFavourites(final SampServer server)
	{
		String statement = "DELETE FROM favourite WHERE ip = ''{0}'' AND port = ''{1}'';";
		statement = MessageFormat.format(statement, server.getAddress(), server.getPort().toString());
		SQLDatabase.getInstance().execute(statement);
	}

	/**
	 * Returns a {@link List} of favourite servers.
	 *
	 * @return {@link List} of favourite servers
	 */
	public static List<SampServer> getFavourites()
	{
		final List<SampServer> servers = new ArrayList<>();

		SQLDatabase.getInstance().executeGetResult("SELECT * FROM favourite;").ifPresent(resultSet ->
		{
			try
			{
				while (resultSet.next())
				{
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
			catch (final SQLException exception)
			{
				Logging.log(Level.SEVERE, "Error while retrieving favourites", exception);
			}
		});

		return servers;
	}

	/**
	 * @return the List of all SampServers that the legacy favourite file contains.
	 */
	public static List<SampServer> retrieveLegacyFavourites()
	{
		final List<SampServer> legacyFavourites = new ArrayList<>();

		try
		{
			final byte[] data = Files.readAllBytes(Paths.get(PathConstants.SAMP_USERDATA));
			final ByteBuffer buffer = ByteBuffer.wrap(data);
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			// Skiping trash at the beginning
			buffer.position(buffer.position() + 8);

			final int serverCount = buffer.getInt();
			for (int i = 0; i < serverCount; i++)
			{
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
		catch (final IOException exception)
		{
			Logging.log(Level.WARNING, "Error loading legacy favourites.", exception);
			return legacyFavourites;
		}
	}
}
