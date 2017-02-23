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
		checkDataType(id, Integer.class);

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

	public static Float getPropertyAsFloat(final PropertyIds id)
	{
		checkDataType(id, Float.class);

		Float value = 0.0F;

		final String originalValue = getPropertyAsString(id);

		try
		{
			value = Float.parseFloat(originalValue);
		}
		catch (final NumberFormatException e)
		{
			Logging.logger.log(Level.SEVERE, "Invalid property value, property: " + id, e);
		}

		return value;
	}

	public static Boolean getPropertyAsBoolean(final PropertyIds id)
	{
		checkDataType(id, Boolean.class);

		return Boolean.parseBoolean(getPropertyAsString(id));
	}

	private static void checkDataType(final PropertyIds id, final Class<?> datatype)
	{
		if (!id.datatype().equals(datatype))
		{
			throw new IllegalArgumentException("Datatype is " + datatype.getName() + " ; Expected: " + id.datatype().getName());
		}
	}

	public static void setProperty(final PropertyIds id, final Object value)
	{
		checkDataType(id, value.getClass());

		String statement = "INSERT OR REPLACE INTO setting (id, value) VALUES({0}, ''{1}'');";
		statement = MessageFormat.format(statement, id.value(), value);
		SQLDatabase.execute(statement);
	}

	public static void setProperty(final PropertyIds id, final Float value)
	{
		setProperty(id, (Object) value);
	}

	public static void setProperty(final PropertyIds id, final Integer value)
	{
		setProperty(id, (Object) value);
	}

	public static void setProperty(final PropertyIds id, final Boolean value)
	{
		setProperty(id, (Object) value);
	}

	public static void setProperty(final PropertyIds id, final String value)
	{
		setProperty(id, (Object) value);
	}
}
