package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import logging.Logging;

public class MySQLConnection
{
	public static Connection connect = null;

	public static void init(String username, String password)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://localhost?useSSL=false&useUnicode=true&characterEncoding=UTF-8", username, password);
			connect.setCatalog("mp_server_browser");
			Logging.logger.log(Level.INFO, "Databank connection has been established.");

		}
		catch (ClassNotFoundException e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't load MySQL Driver.", e);
			System.exit(0);
		}
		catch (SQLException e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't connect to Database.", e);
			System.exit(0);
		}
	}

	public static boolean truncate()
	{
		try
		{
			connect.createStatement().executeUpdate("TRUNCATE TABLE internet_offline;");
			Logging.logger.log(Level.INFO, "Table has been successfully truncated.");
			return true;
		}
		catch (SQLException e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't truncate table.", e);
			return false;
		}
	}

	public static void addServer(String ip, String port, String hostname, int players, int max_players, String mode, String language, String lagcomp, String mapname, String version, int weather,
			String weburl, String worldtime)
	{
		try
		{
			// TODO(MSC) Fix Encoding problems and escaping
			Statement statement = connect.createStatement();
			statement.setEscapeProcessing(true);
			statement.executeUpdate("INSERT INTO internet_offline (ip_address, port, hostname, players, max_players, mode, language, lagcomp, mapname, version, weather, weburl, worldtime) VALUES("
					+ "'"
					+ ip
					+ "', '"
					+ port
					+ "', '"
					+ hostname
					+ "', "
					+ players
					+ ", "
					+ max_players
					+ ", '"
					+ mode
					+ "', '"
					+ language
					+ "', '"
					+ lagcomp
					+ "', '"
					+ mapname
					+ "', '"
					+ version
					+ "', "
					+ weather
					+ ", '"
					+ weburl
					+ "', '"
					+ worldtime
					+ "');");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}