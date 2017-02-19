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

// TODO(MSC) Improve, what i have made here is complete utter crap.
public class PastUsernames
{
	public static void addPastUsername(final String username)
	{
		if (!getPastUsernames().contains(username))
		{
			final File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "pastusernames.xml");
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

				final Element newPastUsername = doc.createElement("username");

				newPastUsername.setAttribute("name", username);

				doc.getElementsByTagName("usernames").item(0).appendChild(newPastUsername);

				final TransformerFactory transformerFactory = TransformerFactory.newInstance();
				final Transformer transformer = transformerFactory.newTransformer();
				final DOMSource source = new DOMSource(doc);
				final StreamResult result = new StreamResult(xmlFile);
				transformer.transform(source, result);
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void removePastUsername(final String username)
	{
		final File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "pastusernames.xml");
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

		try
		{
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			final NodeList list = doc.getElementsByTagName("username");

			for (int i = 0; i < list.getLength(); i++)
			{
				final Node node = list.item(i);
				final NamedNodeMap attr = node.getAttributes();
				final Node nameNode = attr.getNamedItem("name");;

				if (nameNode.getTextContent().equals(username))
				{
					doc.getElementsByTagName("usernames").item(0).removeChild(node);
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

	public static Set<String> getPastUsernames()
	{
		final Set<String> usernames = new HashSet<>();

		final File xmlFile = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "pastusernames.xml");
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		try
		{
			dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			final NodeList list = doc.getElementsByTagName("username");

			for (int i = 0; i < list.getLength(); i++)
			{
				try
				{
					final Node node = list.item(i);
					final NamedNodeMap attr = node.getAttributes();
					final Node name = attr.getNamedItem("name");
					usernames.add(name.getTextContent());
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

		return usernames;
	}
}
