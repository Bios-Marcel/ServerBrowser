package data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;

public class SQLDatabase
{
	private static final String	DB_LOCATION	= System.getProperty("user.home") + File.separator + "sampex" + File.separator + "samp.db";

	private static Connection	sqlConnection;

	public static void init()
	{
		try
		{
			sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + DB_LOCATION);

			if (Objects.nonNull(sqlConnection))
			{
				final Statement statement = sqlConnection.createStatement();

				System.out.println("Creating Tables if neccessary:");

				final String createTableFavourites =
								"CREATE TABLE IF NOT EXISTS favourite (hostname text, ip text NOT NULL, lagcomp text, language text, players integer, maxplayers integer, mode text, port integer, version text, website text);";
				statement.execute(createTableFavourites);

				final String createTableUsernames = "CREATE TABLE IF NOT EXISTS username (id integer PRIMARY KEY, username text NOT NULL);";
				statement.execute(createTableUsernames);

				final String createTableSettings = "CREATE TABLE IF NOT EXISTS setting (id integer UNIQUE, value text);";
				statement.execute(createTableSettings);
			}

		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static boolean execute(final String statement)
	{
		try
		{
			return sqlConnection.createStatement().execute(statement);
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static Optional<ResultSet> executeGetResult(final String statement)
	{
		try
		{
			return Optional.of(sqlConnection.prepareStatement(statement).executeQuery());
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
