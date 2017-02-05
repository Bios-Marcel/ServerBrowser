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
import java.util.Objects;
import java.util.StringTokenizer;

public class SampQuery
{

	private DatagramSocket	socket			= null;

	private InetAddress		server			= null;

	private String			serverAddress	= "";

	private int				serverPort		= 0;

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
		try
		{
			this.serverAddress = serverAddress;
			this.server = InetAddress.getByName(this.serverAddress);
			socket = new DatagramSocket();
			socket.setSoTimeout(350);
			this.serverPort = serverPort;
		}
		catch (UnknownHostException | SocketException e)
		{
			System.out.println("Error");
		}
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
		DatagramPacket packet = assemblePacket("i");

		if (Objects.nonNull(packet))
		{
			send(packet);
			byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				ByteBuffer buff = ByteBuffer.wrap(reply);
				buff.order(ByteOrder.LITTLE_ENDIAN);
				buff.position(11);
				String[] serverInfo = new String[7];

				// Password Yes / No
				short password = buff.get();
				serverInfo[0] = "" + password;

				// Players connected
				short players = buff.getShort();
				serverInfo[1] = "" + players;

				// Max Players
				short maxPlayers = buff.getShort();
				serverInfo[2] = "" + maxPlayers;

				// Hostname
				int len = buff.getInt();
				byte[] hostname = new byte[len];

				for (int i = 0; i < len; i++)
				{
					hostname[i] = buff.get();
				}
				serverInfo[3] = new String(hostname);

				// Gamemode
				len = buff.getInt();
				byte[] gamemode = new byte[len];

				for (int i = 0; i < len; i++)
				{
					gamemode[i] = buff.get();
				}
				serverInfo[4] = new String(gamemode);;

				// Map
				len = buff.getInt();
				byte[] map = new byte[len];

				for (int i = 0; i < len; i++)
				{
					map[i] = buff.get();
				}
				serverInfo[5] = new String(map);

				// Language
				len = buff.getInt();
				byte[] language = new byte[len];

				for (int i = 0; i < len; i++)
				{
					map[i] = buff.get();
				}
				serverInfo[6] = new String(language);

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
		DatagramPacket packet = assemblePacket("c");

		if (Objects.nonNull(packet))
		{
			send(packet);
			byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				ByteBuffer buff = ByteBuffer.wrap(reply);
				buff.order(ByteOrder.LITTLE_ENDIAN);
				buff.position(11);

				String[][] players = new String[buff.getShort()][2];

				for (int i = 0; players.length > i; i++)
				{
					try
					{
						byte len = buff.get();
						byte[] playerName = new byte[len];

						for (int j = 0; j < len; j++)
						{
							playerName[j] = buff.get();
						}

						players[i][0] = new String(playerName);
						players[i][1] = "" + buff.getInt();
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
		DatagramPacket packet = assemblePacket("d");

		if (Objects.nonNull(packet))
		{
			send(packet);
			byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				ByteBuffer buff = ByteBuffer.wrap(reply);
				buff.order(ByteOrder.LITTLE_ENDIAN);
				buff.position(11);

				String[][] players = new String[buff.getShort()][3];

				for (int i = 0; i < players.length; i++)
				{
					int len = buff.get();
					byte[] playerName = new byte[len];

					for (int j = 0; j < len; j++)
					{
						playerName[j] = buff.get();
					}

					players[i][0] = "" + buff.get();
					players[i][1] = new String(playerName);
					players[i][2] = "" + buff.getInt();
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
		DatagramPacket packet = assemblePacket("r");

		if (Objects.nonNull(packet))
		{
			send(packet);
			byte[] reply = receiveBytes();
			if (Objects.nonNull(reply))
			{
				ByteBuffer buff = ByteBuffer.wrap(reply);
				buff.order(ByteOrder.LITTLE_ENDIAN);
				buff.position(11);

				short ruleCount = buff.getShort();
				String[][] rules = new String[ruleCount][2];

				for (int i = 0; i < rules.length; i++)
				{
					int len = buff.get();
					byte[] ruleName = new byte[len];

					for (int j = 0; j < len; j++)
					{
						ruleName[j] = buff.get();
					}

					len = buff.get();
					byte[] ruleValue = new byte[len];

					for (int j = 0; j < len; j++)
					{
						ruleValue[j] = buff.get();
					}

					rules[i][0] = new String(ruleName);
					rules[i][1] = new String(ruleValue);
				}
				return rules;
			}
		}
		return null;
	}

	/**
	 * Returns the server's ping.
	 * 
	 * @return integer
	 */
	public long getPing()
	{
		long beforeSend = System.currentTimeMillis();
		send(assemblePacket("p0101"));
		receiveBytes();
		return System.currentTimeMillis() - beforeSend;
	}

	/**
	 * Returns whether a successful connection was made.
	 * 
	 * @return boolean
	 */
	public boolean connect()
	{
		// TODO(MSC) Check if server deactivated querying
		send(assemblePacket("p0101"));
		String reply = receive();
		return reply == null ? false : reply.substring(10).trim().equals("p0101");
	}

	/**
	 * Closes the connection.
	 */
	public void close()
	{
		socket.close();
	}

	private DatagramPacket assemblePacket(String type)
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

			byte[] data = packetData.getBytes("US-ASCII");

			DatagramPacket sendPacket = new DatagramPacket(data, data.length, server, serverPort);
			return sendPacket;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Sends a packet to te server
	 * 
	 * @param packet
	 *            that is supposed to be sent
	 */
	private void send(DatagramPacket packet)
	{
		try
		{
			socket.send(packet);
		}
		catch (IOException e)
		{
			// Do nothing
		}
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