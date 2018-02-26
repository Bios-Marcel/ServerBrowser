package com.msc.serverbrowser;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.util.ServerUtility;
import com.msc.serverbrowser.util.samp.SampQuery;

/**
 * Queries southclaws api, giving some information about how many servers are online/have querying
 * enabled.
 *
 * @author Marcel
 * @since 21.01.2018
 */
public final class PrintServerStatistics {
	private PrintServerStatistics() {
		// Private constructor to prevent instantiation
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		final List<SampServer> servers = ServerUtility.fetchServersFromSouthclaws();
		final int onlineServers = servers.stream().mapToInt(server -> {
			try (SampQuery query = new SampQuery(server.getAddress(), server.getPort())) {
				return query.getBasicPlayerInfo().isPresent() ? 1 : 0;
			}
			catch (@SuppressWarnings("unused") SocketException | UnknownHostException e) {
				return 0;
			}
		}).sum();

		System.out.println("Out of " + servers.size() + " only " + onlineServers + " are online/have querying enabled.");
	}
}
