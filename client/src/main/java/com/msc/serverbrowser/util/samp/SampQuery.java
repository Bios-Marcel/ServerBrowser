package com.msc.serverbrowser.util.samp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import com.msc.serverbrowser.data.entites.Player;
import com.msc.serverbrowser.util.basic.Encoding;

/**
 * Provides Methods for retrieving information from a SA-MP Server.
 *
 * @author Marcel
 * @see <a href="http://wiki.sa-mp.com/wiki/Query_Mechanism">Wiki SA-MP - Query Mechanism</a>
 */
public class SampQuery implements AutoCloseable {
	private static final char	PACKET_GET_SERVERINFO		= 'i';
	private static final char	PACKET_GET_RULES			= 'r';
	private static final char	PACKET_MIRROR_CHARACTERS	= 'p';
	private static final char	PACKET_GET_BASIC_PLAYERINFO	= 'c';

	private final DatagramSocket	socket;
	private final InetAddress		server;
	private final int				serverPort;

	/**
	 * Configures the socket and the address that will be used for doing the queries.
	 *
	 * @param serverAddress
	 *            hostname / ip
	 * @param serverPort
	 *            port
	 * @param timeout
	 *            the maximum time, that the socket tries connecting
	 * @throws SocketException
	 *             Thrown if the connection is closed unexpectedly / has never been opened properly
	 * @throws UnknownHostException
	 *             if the host is unknown
	 */
	public SampQuery(final String serverAddress, final int serverPort, final int timeout) throws SocketException, UnknownHostException {
		this.server = InetAddress.getByName(serverAddress);
		socket = new DatagramSocket();
		socket.setSoTimeout(timeout);
		this.serverPort = serverPort;
		checkConnection();
	}

	/**
	 * Configures the socket and the address.
	 *
	 * @param serverAddress
	 *            hostname / ip
	 * @param serverPort
	 *            port
	 * @throws SocketException
	 *             if the connection couldn't be established
	 * @throws UnknownHostException
	 *             if the host is unknown
	 */
	public SampQuery(final String serverAddress, final int serverPort) throws SocketException, UnknownHostException {
		this(serverAddress, serverPort, 2000);
	}

	/**
	 * Returns whether a successful connection was made.
	 */
	private void checkConnection() throws SocketException {
		// TODO(MSC) Check if server deactivated querying, since this will only tell if
		// the server
		// is online, but will still work with servers that have deactivated querying
		send(PACKET_MIRROR_CHARACTERS);
		final byte[] reply = receiveBytes();
		// Removed the checks if the reply was valid, i think its not even necessary
		if (Objects.isNull(reply)) {
			throw new SocketException("Couldn't connect to Server");
		}
	}

	@Override
	public void close() throws SocketException {
		socket.close();
	}

	/**
	 * Returns a String array, containing information about the server.
	 *
	 * @return String[]:<br />
	 *         Index 0: password (0 or 1)<br />
	 *         Index 1: players<br />
	 *         Index 2: maxplayers<br />
	 *         Index 3: hostname<br />
	 *         Index 4: gamemode<br />
	 *         Index 5: language
	 */
	public Optional<String[]> getBasicServerInfo() {
		if (send(PACKET_GET_SERVERINFO)) {
			final byte[] reply = receiveBytes();
			if (Objects.nonNull(reply)) {
				final ByteBuffer buffer = wrapReply(reply);
				final String[] serverInfo = new String[6];
				final String encoding = Encoding.getEncoding(reply).orElse(StandardCharsets.UTF_8.toString());

				// Password Yes / No
				final short password = buffer.get();
				serverInfo[0] = String.valueOf(password);

				// Players connected
				final short players = buffer.getShort();
				serverInfo[1] = String.valueOf(players);

				// Max Players
				final short maxPlayers = buffer.getShort();
				serverInfo[2] = String.valueOf(maxPlayers);

				// add hostname, gamemode and language
				for (int valueIndex = 3; valueIndex < 6; valueIndex++) {
					final int len = buffer.getInt();
					final byte[] value = new byte[len];
					IntStream.range(0, len).forEach(j -> value[j] = buffer.get());
					serverInfo[valueIndex] = Encoding.decodeUsingCharsetIfPossible(value, encoding);
				}

				return Optional.of(serverInfo);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns an {@link Optional} of a {@link List} of {@link Player} objects, containing all
	 * players on the server.
	 *
	 * @return an {@link Optional} containg a {@link List} of {@link Player Players} or an empty
	 *         {@link Optional} incase the query failed.
	 */
	public Optional<List<Player>> getBasicPlayerInfo() {
		List<Player> players = null;

		if (send(PACKET_GET_BASIC_PLAYERINFO)) {
			final byte[] reply = receiveBytes();
			if (Objects.nonNull(reply)) {
				final ByteBuffer buffer = wrapReply(reply);
				final int numberOfPlayers = buffer.getShort();
				players = new ArrayList<>();

				for (int i = 0; i < numberOfPlayers; i++) {
					final byte len = buffer.get();
					final byte[] playerName = new byte[len];
					IntStream.range(0, len).forEach(j -> playerName[j] = buffer.get());
					players.add(new Player(new String(playerName), buffer.getInt()));
				}
			}
		}
		return Optional.ofNullable(players);
	}

	/**
	 * Returns a Map containing all server rules. The Key is always the rules name.
	 *
	 * @return a Map containing all server rules
	 */
	public Optional<Map<String, String>> getServersRules() {
		if (send(PACKET_GET_RULES)) {
			final byte[] reply = receiveBytes();
			if (Objects.nonNull(reply)) {
				final ByteBuffer buffer = wrapReply(reply);
				final Map<String, String> rules = new HashMap<>();

				final short ruleCount = buffer.getShort();

				for (int i = 0; i < ruleCount; i++) {
					// fill string for rule name
					int len = buffer.get();
					final byte[] ruleName = new byte[len];
					IntStream.range(0, len).forEach(j -> ruleName[j] = buffer.get());

					// fill string for rule value
					len = buffer.get();
					final byte[] ruleValue = new byte[len];
					IntStream.range(0, len).forEach(j -> ruleValue[j] = buffer.get());

					rules.put(new String(ruleName), new String(ruleValue));
				}
				return Optional.of(rules);
			}
		}
		return Optional.empty();
	}

	/**
	 * <p>
	 * Wraps the received bytes in a {@link ByteBuffer} for easier usage.
	 * </p>
	 * Contents of the byte array:
	 * <ul>
	 * <li>Byte 0 - 3: "SAMP"</li>
	 * <li>Byte 4 - 7: IP</li>
	 * <li>Byte 8 - 9: Port</li>
	 * <li>Byte 10: Message Type</li>
	 * <li>Byte 11+: Data</li>
	 * </ul>
	 * <p>
	 * Because the Data contains multiple informations that we do not care for as of now, we are
	 * setting the byte buffers initial position to eleven.
	 * </p>
	 *
	 * @param the
	 *            byte array to be wrapped
	 * @return the {@link ByteBuffer} that wraps the byte array
	 */
	private static ByteBuffer wrapReply(final byte[] reply) {
		final ByteBuffer buffer = ByteBuffer.wrap(reply);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(11);
		return buffer;
	}

	/**
	 * Returns the server's ping.
	 *
	 * @return ping
	 */
	public long getPing() {
		final long beforeSend = System.currentTimeMillis();
		send(PACKET_MIRROR_CHARACTERS);
		receiveBytes();
		return System.currentTimeMillis() - beforeSend;
	}

	private DatagramPacket assemblePacket(final char type) {
		final StringTokenizer tok = new StringTokenizer(server.getHostAddress(), ".");
		final StringBuffer packetData = new StringBuffer("SAMP");

		while (tok.hasMoreTokens()) {// The splitted parts of the ip will be parsed into ints and
										// casted into characters
			packetData.append((char) Integer.parseInt(tok.nextToken()));
		}

		/*
		 * At this point the buffer contains something like 'SAMPx!2.' where each character after
		 * 'SAMP' is a part of the ip address
		 */

		packetData.append((char) (serverPort & 0xFF)).append((char) (serverPort >> 8 & 0xFF)).append(type);

		if (type == PACKET_MIRROR_CHARACTERS) {

			/**
			 * Applying random bytes for the server to mirror them back.
			 * TODO Currently those bytes aren't reused to check if the server did everything
			 * correctly.
			 */

			final Random random = ThreadLocalRandom.current();
			final byte[] toMirror = new byte[4];
			random.nextBytes(toMirror);
			packetData.append(new String(toMirror, StandardCharsets.US_ASCII));
		}

		final byte[] data = packetData.toString().getBytes(StandardCharsets.US_ASCII);
		return new DatagramPacket(data, data.length, server, serverPort);
	}

	/**
	 * Sends a packet to te server
	 *
	 * @param packet
	 *            that is supposed to be sent
	 */
	private boolean send(final char packetType) {
		try {
			final DatagramPacket packet = assemblePacket(packetType);
			socket.send(packet);
			return true;
		}
		catch (@SuppressWarnings("unused") final IOException exception) {
			return false;
		}
	}

	/**
	 * Reseives a package from the server
	 *
	 * @return the package data as a byte array or null on fail
	 */
	private byte[] receiveBytes() {
		try {
			// This is enough for at least 100 players information.
			final byte[] receivedData = new byte[14000];
			final DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			socket.receive(receivedPacket);
			return receivedPacket.getData();
		}
		catch (@SuppressWarnings("unused") final IOException exception) {
			return null;
		}
	}
}