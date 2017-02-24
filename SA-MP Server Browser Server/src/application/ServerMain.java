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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

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
			registry.rebind(DataServiceInterface.INTERFACE_NAME, stub);
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

			final Statement statement = MySQLConnection.connect.createStatement();

			// TODO(MSC) Convert field in database instead of casting.
			final ResultSet resultSet =
							statement.executeQuery(
											"SELECT version, ip_address, id, CONVERT(port, SIGNED INTEGER) as portNumber, hostname, players, lagcomp, max_players, mode, version, weburl, language FROM internet_offline;");

			final List<SampServerSerializeable> servers = new ArrayList<>();

			while (resultSet.next())
			{
				final SampServerSerializeable server =
								new SampServerSerializeable(resultSet.getString("hostname"), resultSet.getString("ip_address"), resultSet.getInt("portNumber"), resultSet.getInt("players"),
												resultSet.getInt("max_players"), resultSet.getString("mode"), resultSet.getString("language"), resultSet.getString("lagcomp"),
												resultSet.getString("weburl"), resultSet.getString("version"));
				if (!servers.contains(server))
				{
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
		try
		{
			final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();

			class UpdateJob implements Job
			{
				@Override
				public void execute(@SuppressWarnings("unused") final JobExecutionContext arg0) throws JobExecutionException
				{
					updateDBWithMasterlistContent();
				}
			}

			final JobDetail job = JobBuilder.newJob(UpdateJob.class).withIdentity("updateList", "updater").build();

			final Trigger trigger = TriggerBuilder.newTrigger().withIdentity("updateTrigger", "updater").startNow().withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(23, 59)).build();

			scheduler.scheduleJob(job, trigger);
		}
		catch (final SchedulerException e)
		{
			e.printStackTrace();
		}
	}

	private static void updateDBWithMasterlistContent()
	{
		Logging.logger.log(Level.INFO, "Starting to update Database.");

		if (MySQLConnection.truncate())
		{
			addToDatabaseFromList("http://monitor.sacnr.com/list/masterlist.txt");
			addToDatabaseFromList("http://monitor.sacnr.com/list/hostedlist.txt");
			setServerList();
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

					try (final SampQuery query = new SampQuery(data[0], Integer.parseInt(data[1]), 800))
					{
						final Optional<String[]> infoOptional = query.getBasicServerInfo();

						final Optional<String[][]> infoMoreOptional = query.getServersRules();

						if (infoOptional.isPresent() && infoMoreOptional.isPresent())
						{

							final String[] info = infoOptional.get();
							final String[][] infoMore = infoMoreOptional.get();

							String weburl = null;
							String lagcomp = null;
							String worldtime = null;
							String version = null;
							String map = null;
							int weather = 0;

							// TODO(MSC) Inspect data response of all server versions and remove loops if possible
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
								else if (infoMore[i][0].equals("mapname"))
								{
									map = infoMore[i][1];
								}
							}

							MySQLConnection.addServer(data[0], data[1], info[3], Integer.parseInt(info[1]), Integer.parseInt(info[2]), info[4], info[5], lagcomp, map, version, weather, weburl,
											worldtime);
							Logging.logger.log(Level.INFO, "Added Server: " + inputLine);
						}
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
