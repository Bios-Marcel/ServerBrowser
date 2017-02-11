package query;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;

import util.Encoding;

public class SampQuery
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
	 */
	public SampQuery(String serverAddress, int serverPort, int timeout)
	{
		try
		{
			this.serverAddress = serverAddress;
			this.server = InetAddress.getByName(this.serverAddress);
			socket = new DatagramSocket();
			socket.setSoTimeout(timeout);
			this.serverPort = serverPort;
		}
		catch (UnknownHostException | SocketException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Configures the socket and the address.
	 * 
	 * @param serverAddress
	 *            hostname / ip
	 * 
	 * @param serverPort
	 *            port
	 */
	public SampQuery(String serverAddress, int serverPort)
	{
		this(serverAddress, serverPort, 250);
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
	 */
	public String[] getBasicServerInfo()
	{
		if (send(PAKCET_GET_SERVERINFO))
		{
			byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				ByteBuffer buffer = wrapReply(reply);
				String[] serverInfo = new String[7];
				String encoding = Encoding.getEncoding(reply);

				// Password Yes / No
				short password = buffer.get();
				serverInfo[0] = "" + password;

				// Players connected
				short players = buffer.getShort();
				serverInfo[1] = "" + players;

				// Max Players
				short maxPlayers = buffer.getShort();
				serverInfo[2] = "" + maxPlayers;

				// Hostname
				int len = buffer.getInt();
				byte[] hostname = new byte[len];

				for (int i = 0; i < len; i++)
				{
					hostname[i] = buffer.get();
				}

				serverInfo[3] = Encoding.encodeUsingCharsetIfPossible(hostname, encoding);

				// Gamemode
				len = buffer.getInt();
				byte[] gamemode = new byte[len];

				for (int i = 0; i < len; i++)
				{
					gamemode[i] = buffer.get();
				}
				serverInfo[4] = Encoding.encodeUsingCharsetIfPossible(gamemode, encoding);

				// Map
				len = buffer.getInt();
				byte[] map = new byte[len];

				for (int i = 0; i < len; i++)
				{
					map[i] = buffer.get();
				}
				serverInfo[5] = Encoding.encodeUsingCharsetIfPossible(map, encoding);

				// Language
				len = buffer.getInt();
				byte[] language = new byte[len];

				for (int i = 0; i < len; i++)
				{
					map[i] = buffer.get();
				}
				serverInfo[6] = Encoding.encodeUsingCharsetIfPossible(language, encoding);

				return serverInfo;
			}
		}
		return null;
	}

	/**
	 * Returns a multidimensional String array of basic player information.
	 * 
	 * @return String[indexPlayer][indexData]:<br />
	 *         Index Data 0: playername<br />
	 *         Index Data 1: = score<br />
	 * @see getDetailedPlayers
	 */
	public String[][] getBasicPlayerInfo()
	{
		if (send(PACKET_GET_BASIC_PLAYERINFO))
		{
			byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				ByteBuffer buffer = wrapReply(reply);

				String[][] players = new String[buffer.getShort()][2];

				for (int i = 0; players.length > i; i++)
				{
					try
					{
						byte len = buffer.get();
						byte[] playerName = new byte[len];

						for (int j = 0; j < len; j++)
						{
							playerName[j] = buffer.get();
						}

						players[i][0] = new String(playerName);
						players[i][1] = "" + buffer.getInt();
					}
					catch (BufferUnderflowException e)
					{
						// TODO(MSC) Fix ...
					}
				}
				return players;
			}

		}
		return null;
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
	public String[][] getDetailedPlayerInfo()
	{
		if (send(PACKET_GET_DETAILED_PLAYERINFO))
		{
			byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				ByteBuffer buffer = wrapReply(reply);

				String[][] players = new String[buffer.getShort()][3];

				for (int i = 0; i < players.length; i++)
				{
					int len = buffer.get();
					byte[] playerName = new byte[len];

					for (int j = 0; j < len; j++)
					{
						playerName[j] = buffer.get();
					}

					players[i][0] = "" + buffer.get();
					players[i][1] = new String(playerName);
					players[i][2] = "" + buffer.getInt();
				}
				return players;
			}
		}
		return null;
	}

	/**
	 * Returns a multidimensional String array of server rules.
	 * 
	 * @return String[Rulename][Rulevalue]
	 */
	public String[][] getServersRules()
	{
		if (send(PACKET_GET_RULES))
		{
			byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				ByteBuffer buffer = wrapReply(reply);

				short ruleCount = buffer.getShort();
				String[][] rules = new String[ruleCount][2];

				for (int i = 0; i < rules.length; i++)
				{
					int len = buffer.get();
					byte[] ruleName = new byte[len];

					for (int j = 0; j < len; j++)
					{
						ruleName[j] = buffer.get();
					}

					len = buffer.get();
					byte[] ruleValue = new byte[len];

					for (int j = 0; j < len; j++)
					{
						ruleValue[j] = buffer.get();
					}

					rules[i][0] = new String(ruleName);
					rules[i][1] = new String(ruleValue);
				}

				return rules;
			}
		}
		return null;
	}

	private ByteBuffer wrapReply(byte[] reply)
	{
		ByteBuffer buffer = ByteBuffer.wrap(reply);
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
		long beforeSend = System.currentTimeMillis();
		send(PACKET_MIRROR_CHARACTERS);
		receiveBytes();
		return System.currentTimeMillis() - beforeSend;
	}

	/**
	 * Returns whether a successful connection was made.
	 * 
	 * @return boolean
	 */
	public boolean isConnected()
	{
		// TODO(MSC) Check if server deactivated querying, since this will only tell fi the server is online, but will still work with
		// deactivated quering
		send(PACKET_MIRROR_CHARACTERS);
		String reply = receive();
		// Removed the checks if the reply was valid, i think its not even necessary
		return Objects.isNull(reply) ? false : true;
	}

	/**
	 * Closes the connection.
	 */
	public void close()
	{
		socket.close();
	}

	private Optional<DatagramPacket> assemblePacket(String type)
	{
		try
		{
			StringTokenizer tok = new StringTokenizer(serverAddress, ".");

			String packetData = "SAMP";

			while (tok.hasMoreTokens())
			{
				packetData += (char) (Integer.parseInt(tok.nextToken()));
			}

			packetData += (char) (serverPort & 0xFF);
			packetData += (char) (serverPort >> 8 & 0xFF);
			packetData += type;

			byte[] data = packetData.getBytes(StandardCharsets.US_ASCII);

			DatagramPacket sendPacket = new DatagramPacket(data, data.length, server, serverPort);
			return Optional.ofNullable(sendPacket);
		}
		catch (Exception e)
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
	private boolean send(String packetType)
	{
		Optional<DatagramPacket> packet = assemblePacket(packetType);
		if (packet.isPresent())
		{
			try
			{
				socket.send(packet.get());
				return true;
			}
			catch (IOException e)
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
		byte[] bytes = receiveBytes();
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
			byte[] receivedData = new byte[3072];
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			socket.receive(receivedPacket);
			return receivedPacket.getData();
		}
		catch (IOException e)
		{
			return null;
		}
	}
}