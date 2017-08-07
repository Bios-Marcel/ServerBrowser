package com.msc.serverbrowser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.msc.serverbrowser.entities.SampServer;

public class ServerUtil
{
	public static List<SampServer> retrieveMasterlistServers(final String version)
	{
		final List<SampServer> servers = new ArrayList<>();
		try
		{
			final URLConnection openConnection = new URL("http://lists.sa-mp.com/" + version + "/servers").openConnection();
			openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			try (final BufferedReader in = new BufferedReader(new InputStreamReader(openConnection.getInputStream())))
			{
				String inputLine;
				while ((inputLine = in.readLine()) != null)
				{
					final String[] data = inputLine.split(":");
					final SampServer server = new SampServer(data[0], Integer.parseInt(data[1]));
					if (!servers.contains(server))
					{
						servers.add(server);
					}
				}
			}
		}
		catch (@SuppressWarnings("unused") final IOException exception)
		{
			// Do nthn
		}

		return servers;
	}

	private static String readUrl(final String urlString) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			final URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			final StringBuffer buffer = new StringBuffer();
			int read;
			final char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
			{
				buffer.append(chars, 0, read);
			}

			return buffer.toString();
		}
		finally
		{
			if (reader != null)
			{
				reader.close();
			}
		}
	}

	public static List<SampServer> retrieveAnnouncedServers() throws Exception
	{
		final List<SampServer> servers = new ArrayList<>();

		// try (final UTF8Reader reader = new UTF8Reader(new
		// URL("http://api.samp.southcla.ws/v1/servers").openStream()))
		// {
		// final JsonArray jsonArray = Json.parse(reader).asArray();
		// jsonArray.forEach(object ->
		// {
		// final JsonObject jsonServerData = object.asObject();
		// final String address = jsonServerData.getString("ip", "Unknown");
		//
		// final String[] addressData = address.split(":");
		//
		// final SampServer server = new SampServer(addressData[0],
		// address.contains(":") ? Integer.parseInt(addressData[1]) : 7777);
		//
		// server.setPlayers(jsonServerData.getInt("pc", 0));
		// server.setMaxPlayers(jsonServerData.getInt("pm", 0));
		// server.setMode(jsonServerData.getString("gm", "Unknown"));
		// server.setHostname(jsonServerData.getString("hn", "Unknown"));
		// server.setLanguage(jsonServerData.getString("la", "Unknown"));
		//
		// servers.add(server);
		// });

		return servers;
		// }
	}

	public static SampServer getServerInfo() throws Exception
	{
		final String json = readUrl("http://api.samp.southcla.ws/v1/server/ss.southcla.ws");

		final JsonObject jsonData = Json.parse(json).asObject();
		jsonData.forEach(shit ->
		{
			System.out.println(shit.getName());
			if (shit.getName().equals("ru"))
			{
				shit.getValue().asObject().forEach(value ->
				{
					System.out.println(value.getName() + ":" + value.getValue());
				});
			}
			else if (shit.getName().equals("pl"))
			{
				shit.getValue().asArray().forEach(value ->
				{
					System.out.println(value.asString());
				});
			}
		});

		return null;
	}
}
