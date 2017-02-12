package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import data.MySQLConnection;
import entities.SampServerSerializeable;
import interfaces.DataServiceInterface;
import logging.Logging;
import query.SampQuery;
import serviceImplementations.DataServiceServerImplementation;

public class ServerMain
{
	private static Registry				registry;

	private static DataServiceInterface	dataService;

	private static DataServiceInterface	stub;

	public static void main(final String[] args)
	{
		boolean recreatedb = false;
		String username = null;
		String password = null;

		if (args.length >= 1)
		{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-recreatedb") || args[i].equals("-updatedb"))
				{
					recreatedb = true;
				}
				// TODO(msc) Think about changing this into a passsword input request (would be saver)
				else if (args[i].equals("-p") || args[i].equals("-password"))
				{
					if (args.length >= i + 2)
					{
						password = args[i + 1];
					}
					else
					{
						System.out.println("You must enter a password to access the MySQL database.");
						System.exit(0);
					}
				}
				else if (args[i].equals("-u") || args[i].equals("-username"))
				{
					if (args.length >= i + 2)
					{
						username = args[i + 1];
					}
					else
					{
						System.out.println("You must enter a username to access the MySQL database.");
						System.exit(0);
					}
				}
			}
		}

		if (Objects.isNull(username) || Objects.isNull(password))
		{
			Logging.logger.log(Level.SEVERE, "Please enter a Username and a Password for your Database");
			System.exit(0);
		}

		MySQLConnection.init(username, password);

		try
		{
			registry = LocateRegistry.createRegistry(1099);
			dataService = new DataServiceServerImplementation();
			stub = (DataServiceInterface) UnicastRemoteObject.exportObject(dataService, 0);
			registry.rebind("DataServiceInterface", stub);
			Logging.logger.log(Level.INFO, "RMI has been initialized.");
		}
		catch (final Exception e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't initialize RMI", e);
			System.exit(0);
		}

		if (recreatedb)
		{
			updateDBWithMasterlistContent();
		}

		setServerList();

		createCronJob();

	}

	private static void setServerList()
	{
		try
		{
			DataServiceServerImplementation.clearList();

			final Set<String> ipAndPorts = new HashSet<>();

			Statement statement = MySQLConnection.connect.createStatement();
			// Result set get the result of the SQL query
			ResultSet resultSet = statement.executeQuery("SELECT version, ip_address, id, port, hostname, players, lagcomp, max_players, mode, version, weburl, language FROM internet_offline;");

			final Set<SampServerSerializeable> servers = new HashSet<>();

			while (resultSet.next())
			{
				final String ipAndPort = resultSet.getString("ip_address") + ":" + resultSet.getString("port");
				if (!ipAndPorts.contains(ipAndPort))
				{
					ipAndPorts.add(ipAndPort);

					final SampServerSerializeable server =
									new SampServerSerializeable(resultSet.getString("hostname"), resultSet.getString("ip_address"), resultSet.getString("port"), resultSet.getInt("players"),
													resultSet.getInt("max_players"), resultSet.getString("mode"), resultSet.getString("language"), resultSet.getString("lagcomp"),
													resultSet.getString("weburl"), resultSet.getString("version"));

					servers.add(server);
				}
			}

			statement = MySQLConnection.connect.createStatement();

			resultSet = statement.executeQuery("SELECT version, ip_address, id, port, hostname, players, lagcomp, max_players, mode, version, weburl, language FROM internet_online;");

			while (resultSet.next())
			{
				final String ipAndPort = resultSet.getString("ip_address") + ":" + resultSet.getString("port");
				if (!ipAndPorts.contains(ipAndPort))
				{
					ipAndPorts.add(ipAndPort);

					final SampServerSerializeable server =
									new SampServerSerializeable(resultSet.getString("hostname"), resultSet.getString("ip_address"), resultSet.getString("port"), resultSet.getInt("players"),
													resultSet.getInt("max_players"), resultSet.getString("mode"), resultSet.getString("language"), resultSet.getString("lagcomp"),
													resultSet.getString("weburl"), resultSet.getString("version"));

					servers.add(server);
				}
			}

			DataServiceServerImplementation.addToServers(servers);

		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
	}

	private static void createCronJob()
	{
		final TimerTask readSACNRMasterList = new TimerTask()
		{

			@Override
			public void run()
			{
				updateDBWithMasterlistContent();
			}
		};

		final Timer timer = new Timer();

		final Calendar calendar = Calendar.getInstance(Locale.GERMANY);
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		timer.schedule(readSACNRMasterList, calendar.getTime(), 7200000);
	}

	private static void updateDBWithMasterlistContent()
	{
		Logging.logger.log(Level.INFO, "Starting to update Database.");

		if (MySQLConnection.truncate())
		{
			addToDatabaseFromList("http://monitor.sacnr.com/list/masterlist.txt");
			addToDatabaseFromList("http://monitor.sacnr.com/list/hostedlist.txt");
		}

		Logging.logger.log(Level.INFO, "Database update is over, check past message to see if it was successful.");
	}

	private static void addToDatabaseFromList(final String url)
	{
		try
		{
			final URLConnection openConnection = new URL(url).openConnection();
			openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			try (final BufferedReader in = new BufferedReader(new InputStreamReader(openConnection.getInputStream())))
			{
				String inputLine;
				while ((inputLine = in.readLine()) != null)
				{
					final String[] data = inputLine.split("[:]");

					try (final SampQuery query = new SampQuery(data[0], Integer.parseInt(data[1])))
					{
						final String[] info = query.getBasicServerInfo();

						final String[][] infoMore = query.getServersRules();

						if (Objects.isNull(infoMore) || Objects.isNull(info))
						{
							continue;
						}

						String weburl = null;
						String lagcomp = null;
						String worldtime = null;
						String version = null;
						int weather = 0;

						for (int i = 0; infoMore.length > i; i++)
						{
							if (infoMore[i][0].equals("lagcomp"))
							{
								lagcomp = infoMore[i][1];
							}
							else if (infoMore[i][0].equals("weburl"))
							{
								weburl = infoMore[i][1];
							}
							else if (infoMore[i][0].equals("version"))
							{
								version = infoMore[i][1];
							}
							else if (infoMore[i][0].equals("worldtime"))
							{
								worldtime = infoMore[i][1];
							}
							else if (infoMore[i][0].equals("weather"))
							{
								weather = Integer.parseInt(infoMore[i][1]);
							}
						}

						MySQLConnection.addServer(data[0], data[1], info[3], Integer.parseInt(info[1]), Integer.parseInt(info[2]), info[4], info[6], lagcomp, info[5], version, weather, weburl,
										worldtime);
						Logging.logger.log(Level.INFO, "Added Server: " + inputLine);
					}
					catch (final Exception e)
					{
						Logging.logger.log(Level.SEVERE, "Failed to connect to Server: " + inputLine);
					}
				}
			}
		}
		catch (

		final IOException e)
		{
			Logging.logger.log(Level.SEVERE, "Failed to add data from server lists.", e);
		}
	}
}
