package query;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;

import util.Encoding;

public class SampQuery implements AutoCloseable
{

	private static final String	PAKCET_GET_SERVERINFO			= "i";

	private static final String	PACKET_GET_BASIC_PLAYERINFO		= "c";

	private static final String	PACKET_GET_DETAILED_PLAYERINFO	= "d";

	private static final String	PACKET_GET_RULES				= "r";

	private static final String	PACKET_MIRROR_CHARACTERS		= "p0101";

	private DatagramSocket		socket							= null;

	private InetAddress			server							= null;

	private String				serverAddress					= "";

	private int					serverPort						= 0;

	/**
	 * Configures the socket and the address.
	 * 
	 * @param serverAddress
	 *            hostname / ip
	 * 
	 * @param serverPort
	 *            port
	 * 
	 * @throws Exception
	 *             Thrown if the connection is closed unexpectedly / has never beenopened properly
	 */
	public SampQuery(final String serverAddress, final int serverPort, final int timeout) throws Exception
	{
		this.serverAddress = serverAddress;
		this.server = InetAddress.getByName(this.serverAddress);
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
	 * 
	 * @param serverPort
	 *            port
	 * @throws Exception
	 */
	public SampQuery(final String serverAddress, final int serverPort) throws Exception
	{
		this(serverAddress, serverPort, 250);
	}

	/**
	 * Returns whether a successful connection was made.
	 * 
	 * @return boolean
	 */
	private void checkConnection() throws Exception
	{
		// TODO(MSC) Check if server deactivated querying, since this will only tell fi the server is online, but will still work with
		// deactivated quering
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
	 * Returns a String array , containing information about the server.
	 * 
	 * @return String[]:<br />
	 *         Index 0: password (0 or 1)<br />
	 *         Index 1: players<br />
	 *         Index 2: maxplayers<br />
	 *         Index 3: hostname<br />
	 *         Index 4: gamemode<br />
	 *         Index 5: map<br />
	 *         Index 5: language
	 */
	public Optional<String[]> getBasicServerInfo()
	{
		if (send(PAKCET_GET_SERVERINFO))
		{
			final byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				final ByteBuffer buffer = wrapReply(reply);
				final String[] serverInfo = new String[6];
				final String encoding = Encoding.getEncoding(reply);

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

				serverInfo[3] = Encoding.encodeUsingCharsetIfPossible(hostname, encoding);

				// Gamemode
				len = buffer.getInt();
				final byte[] gamemode = new byte[len];

				for (int i = 0; i < len; i++)
				{
					gamemode[i] = buffer.get();
				}
				serverInfo[4] = Encoding.encodeUsingCharsetIfPossible(gamemode, encoding);

				// Language
				len = buffer.getInt();
				final byte[] language = new byte[len];

				for (int i = 0; i < len; i++)
				{
					language[i] = buffer.get();
				}
				serverInfo[5] = Encoding.encodeUsingCharsetIfPossible(language, encoding);

				return Optional.of(serverInfo);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns a multidimensional String array of basic player information.
	 * 
	 * @return String[indexPlayer][indexData]:<br />
	 *         Index Data 0: playername<br />
	 *         Index Data 1: = score<br />
	 * @see getDetailedPlayers
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
					try
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
					catch (final BufferUnderflowException e)
					{
						// TODO(MSC) Fix ...
					}
				}
				return Optional.of(players);
			}

		}
		return Optional.empty();
	}

	/**
	 * Returns a multidimensional String array of detailed player information.
	 * 
	 * @return String[][]:<br />
	 *         String[][0]:<br />
	 *         players[0] = playerid<br />
	 *         players[1] = playername<br />
	 *         players[2] = score<br />
	 * @see getBasicPlayers
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
	 * Returns a multidimensional String array of server rules.
	 * 
	 * @return String[Rulename][Rulevalue]
	 */
	public Optional<String[][]> getServersRules()
	{
		if (send(PACKET_GET_RULES))
		{
			final byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				final ByteBuffer buffer = wrapReply(reply);

				final short ruleCount = buffer.getShort();
				final String[][] rules = new String[ruleCount][2];

				for (int i = 0; i < rules.length; i++)
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

					rules[i][0] = new String(ruleName);
					rules[i][1] = new String(ruleValue);
				}

				return Optional.of(rules);
			}
		}
		return Optional.empty();
	}

	private ByteBuffer wrapReply(final byte[] reply)
	{
		final ByteBuffer buffer = ByteBuffer.wrap(reply);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(11); // Ignoring trash
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
				packetData += (char) (Integer.parseInt(tok.nextToken()));
			}

			packetData += (char) (serverPort & 0xFF);
			packetData += (char) (serverPort >> 8 & 0xFF);
			packetData += type;

			final byte[] data = packetData.getBytes(StandardCharsets.US_ASCII);

			final DatagramPacket sendPacket = new DatagramPacket(data, data.length, server, serverPort);
			return Optional.ofNullable(sendPacket);
		}
		catch (final Exception e)
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
			catch (final IOException e)
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
			final byte[] receivedData = new byte[3072];
			final DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			socket.receive(receivedPacket);
			return receivedPacket.getData();
		}
		catch (final IOException e)
		{
			return null;
		}
	}
}