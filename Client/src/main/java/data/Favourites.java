package data;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

public class Favourites
{

	public static void addServerToFavourites(SampServer server)
	{
		File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "favourites.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc;
			try
			{
				doc = dBuilder.parse(xmlFile);
				doc.getDocumentElement().normalize();
			}
			catch (Exception e)
			{
				doc = dBuilder.newDocument();
			}

			Element newServer = doc.createElement("server");

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

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);
		}
		catch (ParserConfigurationException | TransformerException e)
		{
			e.printStackTrace();
		}
	}

	public static void removeServerFromFavourites(SampServer server)
	{
		File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "favourites.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try
		{
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			// <server ip='" + ip + "' port='" + port + "'/>
			NodeList list = doc.getElementsByTagName("server");

			for (int i = 0; i < list.getLength(); i++)
			{

				Node node = list.item(i);
				NamedNodeMap attr = node.getAttributes();
				Node ipNode = attr.getNamedItem("ip");;
				Node portNode = attr.getNamedItem("port");

				if (portNode.getTextContent().equals(server.getPort()) && ipNode.getTextContent().equals(server.getAddress()))
				{
					doc.getElementsByTagName("servers").item(0).removeChild(node);
				}
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);
		}
		catch (ParserConfigurationException | SAXException | IOException | TransformerException e)
		{
			e.printStackTrace();
		}
	}

	public static Set<SampServer> getFavourites()
	{
		Set<SampServer> servers = new HashSet<>();

		File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "favourites.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		try
		{
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			NodeList list = doc.getElementsByTagName("server");

			for (int i = 0; i < list.getLength(); i++)
			{
				try
				{
					Node node = list.item(i);
					NamedNodeMap attr = node.getAttributes();
					Node ipNode = attr.getNamedItem("ip");
					Node hostnameNode = attr.getNamedItem("hostname");
					Node modeNode = attr.getNamedItem("mode");
					Node languageNode = attr.getNamedItem("language");
					Node portNode = attr.getNamedItem("port");
					Node versionNode = attr.getNamedItem("version");
					Node lagcompNode = attr.getNamedItem("lagcomp");
					Node websiteNode = attr.getNamedItem("website");
					Node playersNode = attr.getNamedItem("players");
					Node maxplayersNode = attr.getNamedItem("maxplayers");

					servers.add(new SampServer(new SampServerSerializeable(hostnameNode.getTextContent(), ipNode.getTextContent(), portNode.getTextContent(),
							Integer.parseInt(playersNode.getTextContent()), Integer.parseInt(maxplayersNode.getTextContent()), modeNode.getTextContent(), languageNode.getTextContent(),
							lagcompNode.getTextContent(), websiteNode.getTextContent(), versionNode.getTextContent())));
				}
				catch (NullPointerException e)
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