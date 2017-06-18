package com.msc.serverbrowser.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLConnection
{
	private static final Logger logger = Logger.getLogger("Server");

	public static Connection connect = null;

	public static void init(final String username, final String password, final String database)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost?useSSL=false&useUnicode=true&characterEncoding=UTF-8", username, password);
			connect.setCatalog(database);
			logger.log(Level.INFO, "Databank connection has been established.");
		}
		catch (final ClassNotFoundException exception)
		{
			logger.log(Level.SEVERE, "Couldn't load MySQL Driver.", exception);
			System.exit(0);
		}
		catch (final SQLException exception)
		{
			logger.log(Level.SEVERE, "Couldn't connect to Database.", exception);
			System.exit(0);
		}
	}

	public static boolean truncate()
	{
		try
		{
			connect.createStatement().executeUpdate("TRUNCATE TABLE internet_offline;");
			logger.log(Level.INFO, "Table has been successfully truncated.");
			return true;
		}
		catch (final SQLException exception)
		{
			logger.log(Level.SEVERE, "Couldn't truncate table.", exception);
			return false;
		}
	}

	public static void addServer(final String ip, final String port, final String hostname, final int players, final int maxPlayers, final String mode,
			final String language, final String lagcomp, final String map, final String version, final int weather, final String weburl, final String time)
	{
		try
		{
			// TODO(MSC) Fix Encoding problems and escaping
			final Statement statement = connect.createStatement();
			statement.setEscapeProcessing(true);
			statement
					.executeUpdate("INSERT INTO internet_offline (ip_address, port, hostname, players, max_players, mode, language, lagcomp, mapname, version, weather, weburl, worldtime) VALUES("
							+ "'"
							+ ip
							+ "', '"
							+ port
							+ "', '"
							+ hostname
							+ "', "
							+ players
							+ ", "
							+ maxPlayers
							+ ", '"
							+ mode
							+ "', '"
							+ language
							+ "', '"
							+ lagcomp
							+ "', '"
							+ map
							+ "', '"
							+ version
							+ "', "
							+ weather
							+ ", '"
							+ weburl
							+ "', '"
							+ time
							+ "');");
		}
		catch (final SQLException exception)
		{
			logger.log(Level.WARNING, exception.getMessage(), exception);
		}
	}

	public static void addStatistic(final String country)
	{
		try
		{
			final Statement statement = connect.createStatement();
			statement.setEscapeProcessing(true);
			statement.executeUpdate("INSERT INTO statistics (Date, Country) VALUES('" + new Date().toString() + "', '" + country + "');");
		}
		catch (final SQLException exception)
		{
			logger.log(Level.WARNING, exception.getMessage(), exception);
		}
	}
}