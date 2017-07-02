package com.msc.sampbrowser.query;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;

import com.msc.sampbrowser.util.Encoding;

/**
 * Provides Methods for retrieving information from a SA-MP Server.
 *
 * @author Marcel
 * @see <a href="http://wiki.sa-mp.com/wiki/Query_Mechanism">Wiki SA-MP - Query Mechanism</a>
 */
public class SampQuery implements AutoCloseable
{
	private static final String	PACKET_GET_SERVERINFO			= "i";
	private static final String	PACKET_GET_RULES				= "r";
	private static final String	PACKET_MIRROR_CHARACTERS		= "p0101";
	private static final String	PACKET_GET_BASIC_PLAYERINFO		= "c";
	private static final String	PACKET_GET_DETAILED_PLAYERINFO	= "d";

	private final DatagramSocket	socket;
	private final InetAddress		server;
	private final String			serverAddress;
	private final int				serverPort;

	/**
	 * Configures the socket and the address that will be used for doing the queries.
	 *
	 * @param serverAddress
	 *            hostname / ip
	 * @param serverPort
	 *            port
	 * @throws Exception
	 *             Thrown if the connection is closed unexpectedly / has never beenopened properly
	 */
	public SampQuery(final String serverAddress, final int serverPort, final int timeout) throws Exception
	{
		this.serverAddress = serverAddress;
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
	 * @throws Exception
	 */
	public SampQuery(final String serverAddress, final int serverPort) throws Exception
	{
		this(serverAddress, serverPort, 1250);
	}

	/**
	 * Returns whether a successful connection was made.
	 *
	 * @return boolean
	 */
	private void checkConnection() throws Exception
	{
		// TODO(MSC) Check if server deactivated querying, since this will only tell if the server
		// is online, but will still work with servers that have deactivated querying
		send(PACKET_MIRROR_CHARACTERS);
		final String reply = receive();
		// Removed the checks if the reply was valid, i think its not even necessary
		if (Objects.isNull(reply))
		{
			throw new Exception("Couldn't connect to Server");
		}
	}

	@Override
	public void close() throws Exception
	{
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
	public Optional<String[]> getBasicServerInfo()
	{
		if (send(PACKET_GET_SERVERINFO))
		{
			final byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				final ByteBuffer buffer = wrapReply(reply);
				final String[] serverInfo = new String[6];
				final String encoding = Encoding.getEncoding(reply).orElse(StandardCharsets.UTF_8.toString());

				// Password Yes / No
				final short password = buffer.get();
				serverInfo[0] = "" + password;

				// Players connected
				final short players = buffer.getShort();
				serverInfo[1] = "" + players;

				// Max Players
				final short maxPlayers = buffer.getShort();
				serverInfo[2] = "" + maxPlayers;

				// Hostname
				int len = buffer.getInt();
				final byte[] hostname = new byte[len];

				for (int i = 0; i < len; i++)
				{
					hostname[i] = buffer.get();
				}

				serverInfo[3] = Encoding.decodeUsingCharsetIfPossible(hostname, encoding);

				// Gamemode
				len = buffer.getInt();
				final byte[] gamemode = new byte[len];

				for (int i = 0; i < len; i++)
				{
					gamemode[i] = buffer.get();
				}
				serverInfo[4] = Encoding.decodeUsingCharsetIfPossible(gamemode, encoding);

				// Language
				len = buffer.getInt();
				final byte[] language = new byte[len];

				for (int i = 0; i < len; i++)
				{
					language[i] = buffer.get();
				}
				serverInfo[5] = Encoding.decodeUsingCharsetIfPossible(language, encoding);

				return Optional.of(serverInfo);
			}
		}
		return Optional.empty();
	}

	/**
	 * <p>
	 * Returns a two dimensional String array of basic player information.
	 * </p>
	 * <p>
	 * Every entry in the first dimension equals one player. If you were to retrieve one of those,
	 * the rest would look the following:
	 * </p>
	 * <code>
	 * String[] playerOne = basicInfo[0];<br>
	 * String name = playerOne[0] //playername<br>
	 * String score = playerOne[1] //score
	 * </code>
	 * 
	 * @return a two dimensional array containg the basic player information
	 * @see #getDetailedPlayerInfo()
	 */
	public Optional<String[][]> getBasicPlayerInfo()
	{
		if (send(PACKET_GET_BASIC_PLAYERINFO))
		{
			final byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				final ByteBuffer buffer = wrapReply(reply);

				final String[][] players = new String[buffer.getShort()][2];

				for (int i = 0; players.length > i; i++)
				{
					final byte len = buffer.get();
					final byte[] playerName = new byte[len];

					for (int j = 0; j < len; j++)
					{
						playerName[j] = buffer.get();
					}

					players[i][0] = new String(playerName);
					players[i][1] = "" + buffer.getInt();
				}
				return Optional.of(players);
			}

		}
		return Optional.empty();
	}

	/**
	 * <p>
	 * Returns a two dimensional String array of detailed player information.
	 * </p>
	 * <p>
	 * Every entry in the first dimension equals one player. If you were to retrieve one of those,
	 * the rest would look the following:
	 * </p>
	 * <code>
	 * String[] playerOne = basicInfo[0];<br>
	 * String id = playerOne[0] //playerid<br>
	 * String name = playerOne[1] //playername<br>
	 * String score = playerOne[2] //score
	 * </code>
	 * 
	 * @return a two dimensional array containg the detailed player information
	 * @see #getBasicPlayerInfo()
	 */
	public Optional<String[][]> getDetailedPlayerInfo()
	{
		if (send(PACKET_GET_DETAILED_PLAYERINFO))
		{
			final byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				final ByteBuffer buffer = wrapReply(reply);

				final String[][] players = new String[buffer.getShort()][3];

				for (int i = 0; i < players.length; i++)
				{
					final int len = buffer.get();
					final byte[] playerName = new byte[len];

					for (int j = 0; j < len; j++)
					{
						playerName[j] = buffer.get();
					}

					players[i][0] = "" + buffer.get();
					players[i][1] = new String(playerName);
					players[i][2] = "" + buffer.getInt();
				}
				return Optional.of(players);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns a Map containing all server rules. The Key is always the rules name.
	 *
	 * @return a Map containing all server rules
	 */
	public Optional<Map<String, String>> getServersRules()
	{
		if (send(PACKET_GET_RULES))
		{
			final byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				final ByteBuffer buffer = wrapReply(reply);
				final Map<String, String> rules = new HashMap<>();

				final short ruleCount = buffer.getShort();

				for (int i = 0; i < ruleCount; i++)
				{
					int len = buffer.get();
					final byte[] ruleName = new byte[len];

					for (int j = 0; j < len; j++)
					{
						ruleName[j] = buffer.get();
					}

					len = buffer.get();
					final byte[] ruleValue = new byte[len];

					for (int j = 0; j < len; j++)
					{
						ruleValue[j] = buffer.get();
					}

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
	private ByteBuffer wrapReply(final byte[] reply)
	{
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
	public long getPing()
	{
		final long beforeSend = System.currentTimeMillis();
		send(PACKET_MIRROR_CHARACTERS);
		receiveBytes();
		return System.currentTimeMillis() - beforeSend;
	}

	private Optional<DatagramPacket> assemblePacket(final String type)
	{
		try
		{
			final StringTokenizer tok = new StringTokenizer(serverAddress, ".");

			String packetData = "SAMP";

			while (tok.hasMoreTokens())
			{
				packetData += (char) Integer.parseInt(tok.nextToken());
			}

			packetData += (char) (serverPort & 0xFF);
			packetData += (char) (serverPort >> 8 & 0xFF);
			packetData += type;

			final byte[] data = packetData.getBytes(StandardCharsets.US_ASCII);

			final DatagramPacket sendPacket = new DatagramPacket(data, data.length, server, serverPort);
			return Optional.ofNullable(sendPacket);
		}
		catch (@SuppressWarnings("unused") final Exception exception)
		{
			return Optional.empty();
		}
	}

	/**
	 * Sends a packet to te server
	 *
	 * @param packet
	 *            that is supposed to be sent
	 */
	private boolean send(final String packetType)
	{
		final Optional<DatagramPacket> packet = assemblePacket(packetType);
		if (packet.isPresent())
		{
			try
			{
				socket.send(packet.get());
				return true;
			}
			catch (@SuppressWarnings("unused") final IOException exception)
			{
				return false;
			}
		}
		return false;
	}

	/**
	 * Reseives a package from the server
	 *
	 * @return the package data as a string
	 */
	private String receive()
	{
		final byte[] bytes = receiveBytes();
		if (Objects.nonNull(bytes))
		{
			return new String(bytes);
		}
		return null;
	}

	/**
	 * Reseives a package from the server
	 *
	 * @return the package data as a byte array or null on fail
	 */
	private byte[] receiveBytes()
	{
		try
		{
			// This is enough for at least 100 players information.
			final byte[] receivedData = new byte[14000];
			final DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			socket.receive(receivedPacket);
			return receivedPacket.getData();
		}
		catch (@SuppressWarnings("unused") final IOException exception)
		{
			return null;
		}
	}
}