package com.msc.sampbrowser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.msc.sampbrowser.entities.SampServer;

public class ServerUtil
{
	public static List<SampServer> getServers(final String version)
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

}
