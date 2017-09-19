package com.msc.serverbrowser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.msc.serverbrowser.data.SampServer;

/**
 * @author Marcel
 * @since 19.09.2017
 */
public final class ServerUtility
{
	private ServerUtility()
	{
		// Constructor to prevent instantiation
	}

	private static final String UNKNOWN = "Unknown";

	/**
	 * Retrieves servers from the SA-MP masterlist for the given version.
	 *
	 * @param version
	 *            to filter for
	 * @return List of {@link SampServer} instances
	 */
	public static List<SampServer> retrieveMasterlistServers(final String version)
	{
		final List<SampServer> servers = new ArrayList<>();
		try
		{
			final URLConnection openConnection = new URL("http://lists.sa-mp.com/" + version + "/servers").openConnection();
			openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			try (final BufferedReader in = new BufferedReader(new InputStreamReader(openConnection.getInputStream())))
			{
				in.lines().forEach(inputLine ->
				{
					final String[] data = inputLine.split(":");
					final SampServer server = new SampServer(data[0], Integer.parseInt(data[1]));
					if (!servers.contains(server))
					{
						servers.add(server);
					}
				});
			}
		}
		catch (@SuppressWarnings("unused") final IOException exception)
		{
			// Do nthn
		}

		return servers;
	}

	private static String readUrl(final String urlString) throws MalformedURLException, IOException
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlString).openStream())))
		{
			final StringBuffer buffer = new StringBuffer();

			reader.lines().forEach(buffer::append);

			return buffer.toString();
		}
	}

	public static List<SampServer> fetchServersFromSouthclaws() throws MalformedURLException, IOException
	{
		return fetchFromAPI("http://api.samp.southcla.ws/v1/servers");
	}

	private static List<SampServer> fetchFromAPI(final String apiAddress) throws MalformedURLException, IOException
	{
		final List<SampServer> servers = new ArrayList<>();
		final String json = readUrl(apiAddress);

		final JsonArray jsonArray = Json.parse(json).asArray();

		jsonArray.forEach(object ->
		{
			final JsonObject jsonServerData = object.asObject();
			final String address = jsonServerData.getString("ip", UNKNOWN);

			final String[] addressData = address.split(":");

			final SampServer server = new SampServer(addressData[0], address.contains(":") ? Integer.parseInt(addressData[1]) : 7777);

			server.setPlayers(jsonServerData.getInt("pc", 0));
			server.setMaxPlayers(jsonServerData.getInt("pm", 0));
			server.setMode(jsonServerData.getString("gm", UNKNOWN));
			server.setHostname(jsonServerData.getString("hn", UNKNOWN));
			server.setLanguage(jsonServerData.getString("la", UNKNOWN));

			servers.add(server);
		});

		return servers;
	}
}
