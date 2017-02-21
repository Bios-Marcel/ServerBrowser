package data;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import entities.SampServerSerializeable;
import logging.Logging;
import query.SampQuery;

public class Favourites
{
	public static SampServer addServerToFavourites(final String address, final String port)
	{
		final SampServer server = new SampServer(address, port);
		try (final SampQuery query = new SampQuery(server.getAddress(), Integer.parseInt(server.getPort())))

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
				for (int i = 0; rules.length > i; i++)
				{
					if (rules[i][0].equals("weburl"))
					{
						server.setWebsite(rules[i][1]);
					}
					else if (rules[i][0].equals("version"))
					{
						server.setVersion(rules[i][1]);
					}
				}
			});
		}
		catch (final Exception e)
		{
			Logging.logger.log(Level.WARNING, "Couldn't update Server info, server wills till be added to favourites.");
			server.setHostname("Unknown");
			server.setLanguage("Unknown");
			server.setMode("Unknown");
			server.setWebsite("Unknown");
			server.setVersion("Unknown");
			server.setPlayers(0);
			server.setMaxPlayers(0);
		}

		addServerToFavourites(server);
		return server;
	}

	public static void addServerToFavourites(final SampServer server)
	{
		if (!getFavourites().contains(server))
		{
			String statement =
							"INSERT INTO favourite(hostname, ip, lagcomp, language, players, maxplayers, mode, port, version, website) VALUES (''{0}'', ''{1}'', ''{2}'', ''{3}'', {4}, {5}, ''{6}'', ''{7}'', ''{8}'', ''{9}'');";
			statement =
							MessageFormat.format(statement, server.getHostname(), server.getAddress(), server.getLagcomp(), server.getLanguage(), server.getPlayers(), server.getMaxPlayers(),
											server.getMode(), server.getPort(), server.getVersion(), server.getWebsite());
			SQLDatabase.execute(statement);
		}
	}

	public static void removeServerFromFavourites(final SampServer server)
	{
		String statement = "DELETE FROM favourite WHERE ip = ''{0}'' AND port = ''{1}'';";
		statement = MessageFormat.format(statement, server.getAddress(), server.getPort());
		SQLDatabase.execute(statement);
	}

	public static List<SampServer> getFavourites()
	{
		final List<SampServer> servers = new ArrayList<>();

		SQLDatabase.executeGetResult("SELECT * FROM favourite;").ifPresent(resultSet ->
		{
			try
			{
				while (resultSet.next())
				{
					servers.add(new SampServer(new SampServerSerializeable(resultSet.getString("hostname"), resultSet.getString("ip"), resultSet.getInt("port") + "", resultSet.getInt("players"), resultSet.getInt("maxplayers"),
									resultSet.getString("mode"), resultSet.getString("language"), resultSet.getString("lagcomp"), resultSet.getString("website"), resultSet.getString("version"))));
				}
			}
			catch (final SQLException e)
			{
				e.printStackTrace();
			}
		});

		return servers;
	}

	public static List<SampServer> getFavouritesFromXML()
	{
		final List<SampServer> servers = new ArrayList<>();

		final File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "favourites.xml");
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

					servers.add(new SampServer(new SampServerSerializeable(hostnameNode.getTextContent(), ipNode.getTextContent(), portNode.getTextContent(),
									Integer.parseInt(playersNode.getTextContent()), Integer.parseInt(maxplayersNode.getTextContent()), modeNode.getTextContent(), languageNode.getTextContent(),
									lagcompNode.getTextContent(), websiteNode.getTextContent(), versionNode.getTextContent())));
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
}