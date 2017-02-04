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

public class PastUsernames
{
	public static void addPastUsernames(String username)
	{
		if (!getPastUsernames().contains(username))
		{
			File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "pastusernames.xml");
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

				Element newPastUsername = doc.createElement("username");

				newPastUsername.setAttribute("name", username);

				doc.getElementsByTagName("usernames").item(0).appendChild(newPastUsername);

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
	}

	public static void removePastUsername(String username)
	{
		File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "pastusernames.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try
		{
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			NodeList list = doc.getElementsByTagName("username");

			for (int i = 0; i < list.getLength(); i++)
			{
				Node node = list.item(i);
				NamedNodeMap attr = node.getAttributes();
				Node nameNode = attr.getNamedItem("name");;

				if (nameNode.getTextContent().equals(username))
				{
					doc.getElementsByTagName("usernames").item(0).removeChild(node);
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

	public static Set<String> getPastUsernames()
	{
		Set<String> usernames = new HashSet<>();

		File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "pastusernames.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		try
		{
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			NodeList list = doc.getElementsByTagName("username");

			for (int i = 0; i < list.getLength(); i++)
			{
				try
				{
					Node node = list.item(i);
					NamedNodeMap attr = node.getAttributes();
					Node name = attr.getNamedItem("name");
					usernames.add(name.getTextContent());
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

		return usernames;
	}
}
