package com.msc.serverbrowser.data;

import java.io.File;
import java.io.IOException;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.msc.sampbrowser.entities.SampServer;
import com.msc.sampbrowser.entities.SampServerBuilder;
import com.msc.sampbrowser.query.SampQuery;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.logging.Logging;

public class Favourites
{
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
				server.setLanguage(serverInfo[6]);
			});

			query.getServersRules().ifPresent(rules ->
			{
				server.setWebsite(rules.get("weburl"));
				server.setVersion(rules.get("version"));
			});
		}
		catch (final Exception exception)
		{
			Logging.logger().log(Level.WARNING, "Couldn't update Server info, server wills till be added to favourites.", exception);
			server.setHostname("Unknown");
			server.setLanguage("Unknown");
			server.setMode("Unknown");
			server.setWebsite("Unknown");
			server.setVersion("Unknown");
			server.setLagcomp("Unknown");
			server.setPlayers(0);
			server.setMaxPlayers(0);
		}

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
		if (!isFavourite(server))
		{
			String statement = "INSERT INTO favourite(hostname, ip, lagcomp, language, players, maxplayers, mode, port, version, website) VALUES (''{0}'', ''{1}'', ''{2}'', ''{3}'', {4}, {5}, ''{6}'', {7}, ''{8}'', ''{9}'');";
			statement = escapeFormat(statement, server.getHostname(), server.getAddress(), server.getLagcomp(), server.getLanguage(), server.getPlayers().toString(), server
					.getMaxPlayers().toString(), server.getMode(), server.getPort().toString(), server.getVersion(), server.getWebsite());
			SQLDatabase.getInstance().execute(statement);
		}
		else
		{
			Logging.logger().info("Server wasn't added, because it already is a favourite.");
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
					servers.add(new SampServerBuilder(resultSet.getString("ip"), resultSet.getInt("port"))
							.setHostname(resultSet.getString("hostname"))
							.setPlayers(resultSet.getInt("players"))
							.setMaxPlayers(resultSet.getInt("maxplayers"))
							.setMode(resultSet.getString("mode"))
							.setLanguage(resultSet.getString("language"))
							.setWebsite(resultSet.getString("website"))
							.setLagcomp(resultSet.getString("lagcomp"))
							.setVersion(resultSet.getString("version"))
							.build());
				}
			}
			catch (final SQLException e)
			{
				e.printStackTrace();
			}
		});

		return servers;
	}

	/**
	 * Returns a {@link List} of favourites from the old xml files.
	 *
	 * @return {@link List} of favourite servers
	 */
	public static List<SampServer> getFavouritesFromXML()
	{
		final List<SampServer> servers = new ArrayList<>();

		final File xmlFile = new File(PathConstants.SAMPEX_PATH + File.separator + "favourites.xml");
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

		try
		{
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			final NodeList list = doc.getElementsByTagName("server");

			for (int i = 0; i < list.getLength(); i++)
			{
				try
				{
					final Node node = list.item(i);
					final NamedNodeMap attr = node.getAttributes();
					final Node ipNode = attr.getNamedItem("ip");
					final Node hostnameNode = attr.getNamedItem("hostname");
					final Node modeNode = attr.getNamedItem("mode");
					final Node languageNode = attr.getNamedItem("language");
					final Node portNode = attr.getNamedItem("port");
					final Node versionNode = attr.getNamedItem("version");
					final Node lagcompNode = attr.getNamedItem("lagcomp");
					final Node websiteNode = attr.getNamedItem("website");
					final Node playersNode = attr.getNamedItem("players");
					final Node maxplayersNode = attr.getNamedItem("maxplayers");

					servers.add(new SampServerBuilder(ipNode.getTextContent(), Integer.parseInt(portNode.getTextContent()))
							.setHostname(hostnameNode.getTextContent())
							.setPlayers(Integer.parseInt(playersNode.getTextContent()))
							.setMaxPlayers(Integer.parseInt(maxplayersNode.getTextContent()))
							.setMode(modeNode.getTextContent())
							.setLanguage(languageNode.getTextContent())
							.setWebsite(websiteNode.getTextContent())
							.setLagcomp(lagcompNode.getTextContent())
							.setVersion(versionNode.getTextContent())
							.build());
				}
				catch (final NullPointerException e)
				{
					e.printStackTrace();
				}
			}

		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			e.printStackTrace();
		}

		return servers;
	}

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

			final int sc = buffer.getInt();
			for (int i = 0; i < sc; i++)
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
		catch (@SuppressWarnings("unused") final IOException exception)
		{
			return legacyFavourites;
		}
	}
}
