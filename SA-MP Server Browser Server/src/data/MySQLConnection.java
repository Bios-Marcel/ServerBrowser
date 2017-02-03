package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLConnection
{
	public static Connection connect = null;

	public static void init(String password)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://localhost?useSSL=false&useUnicode=true&characterEncoding=UTF-8", "root", password);
			connect.setCatalog("mp_server_browser");
		}
		catch (SQLException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public static void truncate()
	{
		try
		{
			connect.createStatement().executeUpdate("TRUNCATE TABLE internet_offline;");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static void addServer(String ip, String port, String hostname, int players, int max_players, String mode, String language, String lagcomp, String mapname, String version, int weather,
			String weburl, String worldtime)
	{
		try
		{
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