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
	 * @param args unused
	 * @throws IOException if problems occur while querying the samp servers api
	 */
	public static void main(final String[] args) throws IOException {
		final long start = System.currentTimeMillis();
		final List<SampServer> servers = ServerUtility.fetchServersFromSouthclaws();
		final long onlineServers = servers.stream().parallel().filter(server -> {
			try (SampQuery query = new SampQuery(server.getAddress(), server.getPort())) {
				return query.getBasicServerInfo().isPresent();
			}
			catch (@SuppressWarnings("unused") SocketException | UnknownHostException exception) {
				return false;
			}
		}).count();

		System.out.println("Out of " + servers.size() + " only " + onlineServers + " are online/have querying enabled.");
		System.out.println("Time spent: " + (System.currentTimeMillis() - start));
	}
}
