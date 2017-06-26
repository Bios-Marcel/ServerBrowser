package com.msc.serverbrowser;

import java.io.BufferedReader;
import java.io.Console;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.msc.sampbrowser.entities.SampServerSerializeable;
import com.msc.sampbrowser.entities.SampServerSerializeableBuilder;
import com.msc.sampbrowser.interfaces.DataServiceInterface;
import com.msc.sampbrowser.interfaces.UpdateServiceInterface;
import com.msc.sampbrowser.query.SampQuery;
import com.msc.serverbrowser.data.MySQLConnection;
import com.msc.serverbrowser.serviceImplementations.DataServiceServerImplementation;
import com.msc.serverbrowser.serviceImplementations.UpdateServiceServerImplementation;

public class Server
{
	private static final Logger logger = Logger.getLogger("Server");

	private static Registry registry;

	private static UpdateServiceInterface	updateService;
	private static UpdateServiceInterface	updateServiceStub;

	private static DataServiceInterface	dataService;
	private static DataServiceInterface	dataServiceStub;

	public static void main(final String[] args)
	{

		if (args.length == 0)
		{
			System.out.println("Usage: java -jar jar -u/-username [NAME]");
			System.out.println("To set a password using the arguments use -p/-password [password]");
			System.out.println("Optionally, append a -recreatedb/-updatedb to force updating the server list.");
		}
		else if (args.length >= 1)
		{
			boolean recreatedb = false;
			String username = null;
			String password = null;
			String database = "mp_server_browser";

			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-recreatedb") || args[i].equals("-updatedb"))
				{
					recreatedb = true;
				}
				else if (args[i].equals("-p") || args[i].equals("-password"))
				{
					if (args.length >= i + 2)
					{// Password was given
						password = args[i + 1];
					}
					else
					{// Empty password
						password = "";
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
				else if (args[i].equals("-d") || args[i].equals("-database"))
				{
					if (args.length >= i + 2)
					{
						database = args[i + 1];
					}
				}
			}

			if (Objects.isNull(username))
			{// Username patameter wasn't used
				System.out.println("Please enter a Username for your Database");
				System.exit(0);
			}

			if (Objects.isNull(password))
			{// Password parameter wasn't used
				final Console console = System.console();
				password = new String(console.readPassword("Enter your database password: "));
			}

			MySQLConnection.init(username, password, database);

			try
			{
				registry = LocateRegistry.createRegistry(1099);
				dataService = new DataServiceServerImplementation();
				updateService = new UpdateServiceServerImplementation();
				dataServiceStub = (DataServiceInterface) UnicastRemoteObject.exportObject(dataService, 0);
				updateServiceStub = (UpdateServiceInterface) UnicastRemoteObject.exportObject(updateService, 0);
				registry.rebind(DataServiceInterface.INTERFACE_NAME, dataServiceStub);
				registry.rebind(UpdateServiceInterface.INTERFACE_NAME, updateServiceStub);
				logger.log(Level.INFO, "RMI has been initialized.");
			}
			catch (final Exception exception)
			{
				logger.log(Level.SEVERE, "Couldn't initialize RMI", exception);
				System.exit(0);
			}

			if (recreatedb)
			{
				updateDBWithMasterlistContent();
			}

			setServerList();

			createCronJob();
		}

	}

	private static void setServerList()
	{
		DataServiceServerImplementation.clearList();

		try (final Statement statement = MySQLConnection.connect.createStatement();
				final ResultSet resultSet = statement
						.executeQuery("SELECT version, ip_address, id, CONVERT(port, SIGNED INTEGER) as portNumber, hostname, players, lagcomp, max_players, mode, version, weburl, language FROM internet_offline;");)
		{
			final List<SampServerSerializeable> servers = new ArrayList<>();

			while (resultSet.next())
			{
				final SampServerSerializeable server = new SampServerSerializeableBuilder()
						.setHostname(resultSet.getString("hostname"))
						.setAddress(resultSet.getString("ip_address"))
						.setPort(resultSet.getInt("portNumber"))
						.setPlayers(resultSet.getInt("players"))
						.setMaxPlayers(resultSet.getInt("max_players"))
						.setMode(resultSet.getString("mode"))
						.setLanguage(resultSet.getString("language"))
						.setLagcomp(resultSet.getString("lagcomp"))
						.setWebsite(resultSet.getString("weburl"))
						.setVersion(resultSet.getString("version"))
						.build();

				if (!servers.contains(server))
				{
					servers.add(server);
				}
			}

			DataServiceServerImplementation.addToServers(servers);

		}
		catch (final SQLException exception)
		{
			exception.printStackTrace();
		}
	}

	/**
	 * Creates a scheduled {@link TimerTask} that updates the server list every night at 12pm.
	 */
	private static void createCronJob()
	{
		// TODO(MSC) This shit kinda doesn't work i think :D
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 0);

		final TimerTask updateTask = new TimerTask() {
			@Override
			public void run()
			{
				updateDBWithMasterlistContent();
			}
		};

		final Timer timer = new Timer();
		final int wholeDayAsMillis = 1000 * 60 * 60 * 24;
		timer.schedule(updateTask, calendar.getTime(), wholeDayAsMillis);
	}

	private static void updateDBWithMasterlistContent()
	{
		logger.log(Level.INFO, "Starting to update Database.");

		if (MySQLConnection.truncate())
		{
			addToDatabaseFromList("http://monitor.sacnr.com/list/masterlist.txt");
			addToDatabaseFromList("http://monitor.sacnr.com/list/hostedlist.txt");
			addToDatabaseFromList("http://lists.sa-mp.com/0.3.7/servers");
			setServerList();
		}

		logger.log(Level.INFO, "Database update is over, check past message to see if it was successful.");
	}

	private static void addToDatabaseFromList(final String url)
	{
		try
		{
			final URLConnection openConnection = new URL(url).openConnection();
			// TODO(MSC) Is setting a specific User Agent necessary at all?
			openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			try (final BufferedReader in = new BufferedReader(new InputStreamReader(openConnection.getInputStream())))
			{
				String inputLine;
				while ((inputLine = in.readLine()) != null)
				{
					final String[] data = inputLine.split("[:]");

					try (final SampQuery query = new SampQuery(data[0], Integer.parseInt(data[1])))
					{
						final Optional<String[]> infoOptional = query.getBasicServerInfo();
						final Optional<Map<String, String>> serverRulesOptional = query.getServersRules();

						if (infoOptional.isPresent() && serverRulesOptional.isPresent())
						{

							final String[] info = infoOptional.get();
							final Map<String, String> serverRules = serverRulesOptional.get();

							final String lagcomp = serverRules.get("lagcomp");
							final String map = serverRules.get("map");
							final String version = serverRules.get("version");
							final String weather = serverRules.get("weather");
							final String weburl = serverRules.get("weburl");
							final String worldtime = serverRules.get("worldtime");

							final int players = Integer.parseInt(info[1]);
							final int maxPlayers = Integer.parseInt(info[2]);

							MySQLConnection.addServer(data[0], data[1], info[3], players, maxPlayers, info[4], info[5], lagcomp, map, version, weather, weburl, worldtime);
							logger.log(Level.INFO, "Added Server: " + inputLine);
						}
					}
					catch (final Exception exception)
					{
						logger.log(Level.SEVERE, "Failed to connect to Server: " + inputLine, exception
								);
					}
				}
			}
		}
		catch (final IOException exception)
		{
			logger.log(Level.SEVERE, "Failed to add data from server lists.", exception);
		}
	}
}
