package data;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import entities.SampServerSerializeable;
import logging.Logging;
import query.SampQuery;

// TODO(MSC) Improve, what i have made here is complete utter crap.
public class Favourites
{

	public static SampServer addServerToFavourites(final String address, final String port)
	{
		final SampServer server = new SampServer(address, port);
		try (final SampQuery query = new SampQuery(server.getAddress(), Integer.parseInt(server.getPort())))

		{
			final String[] serverInfo = query.getBasicServerInfo();

			if (Objects.nonNull(serverInfo))
			{
				server.setPlayers(Integer.parseInt(serverInfo[1]));
				server.setMaxPlayers(Integer.parseInt(serverInfo[2]));
				server.setHostname(serverInfo[3]);
				server.setMode(serverInfo[4]);
				server.setLanguage(serverInfo[6]);
			}

			final String[][] rules = query.getServersRules();

			if (Objects.nonNull(rules))
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
			}
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
		final Set<SampServer> existantData = getFavourites();

		boolean existant = false;

		for (final SampServer serverExistant : existantData)
		{
			if (server.getAddress().equals(serverExistant.getAddress()) && server.getPort().equals(serverExistant.getPort()))
			{
				existant = true;
				break;
			}
		}
		if (!existant)
		{
			final File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "favourites.xml");
			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			try
			{
				final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc;

				try
				{
					doc = dBuilder.parse(xmlFile);
					doc.getDocumentElement().normalize();
				}
				catch (final Exception e)
				{
					doc = dBuilder.newDocument();
				}

				final Element newServer = doc.createElement("server");

				newServer.setAttribute("ip", server.getAddress());
				newServer.setAttribute("port", server.getPort());
				newServer.setAttribute("hostname", server.getHostname());
				newServer.setAttribute("mode", server.getMode());
				newServer.setAttribute("language", server.getLanguage());
				newServer.setAttribute("lagcomp", server.getLagcomp());
				newServer.setAttribute("version", server.getVersion());
				newServer.setAttribute("maxplayers", server.getMaxPlayers().toString());
				newServer.setAttribute("players", server.getPlayers().toString());
				newServer.setAttribute("website", server.getWebsite());

				doc.getElementsByTagName("servers").item(0).appendChild(newServer);

				final TransformerFactory transformerFactory = TransformerFactory.newInstance();
				final Transformer transformer = transformerFactory.newTransformer();
				final DOMSource source = new DOMSource(doc);
				final StreamResult result = new StreamResult(xmlFile);
				transformer.transform(source, result);

			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Logging.logger.log(Level.WARNING, "Couldn't save Server to Favourites (Hostname " + server.getHostname() + " IP: " + server.getAddress() + " Port: " + server.getPort(), e);
			}
		}
	}

	public static void removeServerFromFavourites(final SampServer server)
	{
		final File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "favourites.xml");
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

		try
		{
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			// <server ip='" + ip + "' port='" + port + "'/>
			final NodeList list = doc.getElementsByTagName("server");

			for (int i = 0; i < list.getLength(); i++)
			{

				final Node node = list.item(i);
				final NamedNodeMap attr = node.getAttributes();
				final Node ipNode = attr.getNamedItem("ip");;
				final Node portNode = attr.getNamedItem("port");

				if (portNode.getTextContent().equals(server.getPort()) && ipNode.getTextContent().equals(server.getAddress()))
				{
					doc.getElementsByTagName("servers").item(0).removeChild(node);
				}
			}

			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			final DOMSource source = new DOMSource(doc);
			final StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);
		}
		catch (ParserConfigurationException | SAXException | IOException | TransformerException e)
		{
			e.printStackTrace();
		}
	}

	public static Set<SampServer> getFavourites()
	{
		final Set<SampServer> servers = new HashSet<>();

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