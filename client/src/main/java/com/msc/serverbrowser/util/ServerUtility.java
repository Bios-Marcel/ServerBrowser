package com.msc.serverbrowser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.StringUtility;

/**
 * @author Marcel
 * @since 19.09.2017
 */
public final class ServerUtility {
	/**
	 * The port, that every SA-MP server uses by default.
	 */
	public static final Integer DEFAULT_SAMP_PORT = 7777;

	private static final String UNKNOWN = "Unknown";

	private static final int	MAX_PORT	= 65535;
	private static final int	MIN_PORT	= 0;

	private static final int MAX_PAGES = 20;

	private ServerUtility() {
		// Constructor to prevent instantiation
	}

	/**
	 * Retrieves servers from the SA-MP masterlist for the given version.
	 *
	 * @param version to filter for
	 * @return List of {@link SampServer} instances
	 */
	public static List<SampServer> retrieveMasterlistServers(final String version) {
		final List<SampServer> servers = new ArrayList<>();
		try {
			final URLConnection openConnection = new URL("http://lists.sa-mp.com/" + version + "/servers").openConnection();
			openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			try (final BufferedReader in = new BufferedReader(new InputStreamReader(openConnection.getInputStream()))) {
				in.lines().forEach(inputLine -> {
					final String[] data = inputLine.split(":");
					final SampServer server = new SampServer(data[0], Integer.parseInt(data[1]));
					if (!servers.contains(server)) {
						servers.add(server);
					}
				});
			}
		}
		catch (final IOException exception) {
			Logging.error("Error retrieving servers from masterlist.", exception);
		}

		return servers;
	}

	private static String readUrl(final String urlString) throws MalformedURLException, IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlString).openStream()))) {
			final StringBuffer buffer = new StringBuffer();

			reader.lines().forEach(buffer::append);

			return buffer.toString();
		}
	}

	/**
	 * Queries Southclaws Rest API for servers.
	 *
	 * @return a {@link List} of {@link SampServer SampServers}
	 * @throws IOException when querying Southclaws server has failed
	 */
	public static List<SampServer> fetchServersFromSouthclaws() throws IOException {
		return fetchFromAPI("http://api.samp.southcla.ws/v2/servers");
	}

	private static List<SampServer> fetchFromAPI(final String apiAddress) throws MalformedURLException, IOException {
		final List<SampServer> servers = new ArrayList<>();

		// The pages are 1 indexed, so we start at 1 and go up to the given max amount of pages
		for (int page = 1; page < MAX_PAGES; page++) {
			final String json = readUrl(apiAddress + "?page=" + page);

			// In case the page equals null, we have already received all data.
			if (Objects.isNull(json) || "null".equalsIgnoreCase(json)) {
				break;
			}

			final JsonArray jsonArray = Json.parse(json).asArray();

			jsonArray.forEach(object -> {
				final JsonObject jsonServerData = object.asObject();
				final String[] addressData = jsonServerData.getString("ip", UNKNOWN).split(":");
				final int port = addressData.length == 2 ? Integer.parseInt(addressData[1]) : ServerUtility.DEFAULT_SAMP_PORT;
				final SampServer server = new SampServer(addressData[0], port);

				server.setPlayers(jsonServerData.getInt("pc", 0));
				server.setMaxPlayers(jsonServerData.getInt("pm", 0));
				server.setMode(jsonServerData.getString("gm", UNKNOWN));
				server.setHostname(jsonServerData.getString("hn", UNKNOWN));
				server.setLanguage(jsonServerData.getString("la", UNKNOWN));
				server.setVersion(jsonServerData.getString("vn", UNKNOWN));

				// If a server doesn't meet the following, it is invalid.
				if (!server.getHostname().isEmpty() || server.getPlayers() <= server.getMaxPlayers()) {
					servers.add(server);
				}
			});
		}

		return servers;
	}

	/**
	 * Validates the given port.
	 *
	 * @param portAsString the port to be validated
	 * @return true if it is an integer and between 0 and 65535
	 */
	public static boolean isPortValid(final String portAsString) {
		final int portNumber = StringUtility.parseInteger(portAsString).orElse(-1);
		return isPortValid(portNumber);
	}

	/**
	 * Validates the given port.
	 *
	 * @param portNumber the port to be validated
	 * @return true if it is between 0 and 65535
	 */
	public static boolean isPortValid(final Integer portNumber) {
		return portNumber >= MIN_PORT && portNumber <= MAX_PORT;
	}
}