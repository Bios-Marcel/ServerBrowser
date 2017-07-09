package com.msc.serverbrowser.data;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.msc.serverbrowser.constants.PathConstants;

public class PastUsernames
{
	public static void addPastUsername(final String username)
	{
		if (!getPastUsernames().contains(username))
		{
			String statement = "INSERT INTO username (username) VALUES (''{0}'');";
			statement = MessageFormat.format(statement, username);
			SQLDatabase.getInstance().execute(statement);
		}
	}

	public static void removePastUsername(final String username)
	{
		String statement = "DELETE FROM username WHERE username = ''{0}'';";
		statement = MessageFormat.format(statement, username);
		SQLDatabase.getInstance().execute(statement);
	}

	public static List<String> getPastUsernames()
	{
		final List<String> usernames = new ArrayList<>();

		SQLDatabase.getInstance().executeGetResult("SELECT username FROM username;").ifPresent(resultSet ->
		{
			try
			{
				while (resultSet.next())
				{
					usernames.add(resultSet.getString("username"));
				}
			}
			catch (final SQLException e)
			{
				e.printStackTrace();
			}
		});

		return usernames;
	}

	public static Set<String> getPastUsernamesFromXML()
	{
		final Set<String> usernames = new HashSet<>();

		final File xmlFile = new File(PathConstants.SAMPEX_PATH + File.separator + "pastusernames.xml");
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

		try
		{
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
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
