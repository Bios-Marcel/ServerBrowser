package com.msc.serverbrowser.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import com.msc.serverbrowser.logging.Logging;

public class SQLDatabase
{
	private static final String DB_LOCATION = System.getProperty("user.home") + File.separator + "sampex" + File.separator + "samp.db";

	private Connection sqlConnection;

	private static SQLDatabase instance;

	public static SQLDatabase getInstance()
	{
		if (Objects.isNull(instance))
		{
			instance = new SQLDatabase();
		}
		return instance;
	}

	static
	{
		getInstance().init();
	}

	private void init()
	{
		try
		{
			sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + DB_LOCATION);

			try (final Statement statement = sqlConnection.createStatement())
			{
				final String createTableFavourites = "CREATE TABLE IF NOT EXISTS favourite (hostname text, ip text NOT NULL, lagcomp text, language text, players integer, maxplayers integer, mode text, port integer, version text, website text);";
				statement.execute(createTableFavourites);

				final String createTableUsernames = "CREATE TABLE IF NOT EXISTS username (id integer PRIMARY KEY, username text NOT NULL);";
				statement.execute(createTableUsernames);

				final String createTableSettings = "CREATE TABLE IF NOT EXISTS setting (id integer UNIQUE, value text);";
				statement.execute(createTableSettings);

				final String createTableServerHistory = "CREATE TABLE IF NOT EXISTS favourite (hostname text, ip text NOT NULL, lagcomp text, language text, players integer, maxplayers integer, mode text, port integer, version text, website text);";
				statement.execute(createTableServerHistory);
			}
		}
		catch (final SQLException exception)
		{
			Logging.logger.log(Level.SEVERE, "Error while initializing local Database connection.", exception);
		}
	}

	public boolean execute(final String statement)
	{
		try
		{
			return sqlConnection.createStatement().execute(statement);
		}
		catch (final SQLException exception)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't execute query.", exception);
			return false;
		}
	}

	public Optional<ResultSet> executeGetResult(final String statement)
	{
		try
		{
			return Optional.of(sqlConnection.prepareStatement(statement).executeQuery());
		}
		catch (final SQLException exception)
		{
			exception.printStackTrace();
			return Optional.empty();
		}
	}
}
