package data.properties;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Level;

import data.SQLDatabase;
import logging.Logging;

public class ClientProperties
{
	public static String getPropertyAsString(final PropertyIds id)
	{
		String value = id.defaultValue();

		String statement = "SELECT value FROM setting WHERE id = {0};";
		statement = MessageFormat.format(statement, id.value());
		final Optional<ResultSet> resultSetOptional = SQLDatabase.executeGetResult(statement);
		if (resultSetOptional.isPresent())
		{
			try
			{
				final ResultSet resultSet = resultSetOptional.get();
				while (resultSet.next())
				{
					value = resultSet.getString("value");
				}
			}
			catch (final SQLException e)
			{
				e.printStackTrace();
			}
		}

		return value;

	}

	public static Integer getPropertyAsInt(final PropertyIds id)
	{
		Integer value = 0;

		final String originalValue = getPropertyAsString(id);

		try
		{
			value = Integer.parseInt(originalValue);
		}
		catch (final NumberFormatException e)
		{
			Logging.logger.log(Level.SEVERE, "Invalid property value, property: " + id, e);
		}

		return value;
	}

	public static Boolean getPropertyAsBoolean(final PropertyIds id)
	{
		return Boolean.parseBoolean(getPropertyAsString(id));
	}

	public static void setProperty(final PropertyIds id, final String value)
	{
		String statement = "INSERT OR REPLACE INTO setting (id, value) VALUES({0}, ''{1}'');";
		statement = MessageFormat.format(statement, id.value(), value);
		SQLDatabase.execute(statement);
	}

	public static void setProperty(final PropertyIds id, final Integer value)
	{
		setProperty(id, value.toString());
	}

	public static void setProperty(final PropertyIds id, final Boolean value)
	{
		setProperty(id, value.toString());
	}
}
