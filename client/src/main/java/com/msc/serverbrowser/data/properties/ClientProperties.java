package com.msc.serverbrowser.data.properties;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import com.msc.serverbrowser.data.SQLDatabase;
import com.msc.serverbrowser.logging.Logging;

public class ClientProperties
{
	public static Integer getDefaultAsInt(final Property property)
	{
		checkDataType(property, Integer.class);

		return Integer.parseInt(property.defaultValue());
	}

	public static String getPropertyAsString(final Property property)
	{
		String value = property.defaultValue();

		String statement = "SELECT value FROM setting WHERE id = {0};";
		statement = MessageFormat.format(statement, property.id());
		final Optional<ResultSet> resultSetOptional = SQLDatabase.getInstance().executeGetResult(statement);
		if (resultSetOptional.isPresent())
		{
			try (final ResultSet resultSet = resultSetOptional.get();)
			{
				while (resultSet.next())
				{
					value = resultSet.getString("value");
				}
			}
			catch (final SQLException exception)
			{
				Logging.logger().log(Level.SEVERE, "Could not set the property as a String.", exception);
			}
		}

		return Objects.isNull(value) ? property.defaultValue() : value;

	}

	public static Integer getPropertyAsInt(final Property property)
	{
		checkDataType(property, Integer.class);

		Integer value = 0;

		final String originalValue = getPropertyAsString(property);

		try
		{
			value = Integer.parseInt(originalValue);
		}
		catch (final NumberFormatException e)
		{
			Logging.logger().log(Level.SEVERE, "Invalid property value, property: " + property, e);
		}

		return value;
	}

	public static Float getPropertyAsFloat(final Property property)
	{
		checkDataType(property, Float.class);

		Float value = 0.0F;

		final String originalValue = getPropertyAsString(property);

		try
		{
			value = Float.parseFloat(originalValue);
		}
		catch (final NumberFormatException e)
		{
			Logging.logger().log(Level.SEVERE, "Invalid property value, property: " + property, e);
		}

		return value;
	}

	public static Boolean getPropertyAsBoolean(final Property property)
	{
		checkDataType(property, Boolean.class);

		return Boolean.parseBoolean(getPropertyAsString(property));
	}

	private static void checkDataType(final Property property, final Class<?> datatype)
	{
		if (!property.datatype().equals(datatype))
		{
			throw new IllegalArgumentException("Datatype is " + datatype.getName() + " ; Expected: " + property.datatype().getName());
		}
	}

	public static void setProperty(final Property property, final Object value)
	{
		if (Objects.nonNull(value))
		{// Check will only be performed if its non null
			checkDataType(property, value.getClass());
		}
		String statement = null;
		if (Objects.isNull(value))
		{
			statement = "INSERT OR REPLACE INTO setting (id, value) VALUES({0}, NULL);";
			statement = MessageFormat.format(statement, property.id());
		}
		else
		{
			statement = "INSERT OR REPLACE INTO setting (id, value) VALUES({0}, ''{1}'');";
			statement = MessageFormat.format(statement, property.id(), value);
		}
		SQLDatabase.getInstance().execute(statement);
	}

	public static void setProperty(final Property property, final Float value)
	{
		nullCheck(value);
		setProperty(property, (Object) value);
	}

	public static void setProperty(final Property property, final Integer value)
	{
		nullCheck(value);
		setProperty(property, (Object) value);
	}

	public static void setProperty(final Property property, final Boolean value)
	{
		nullCheck(value);
		setProperty(property, (Object) value);
	}

	public static void setProperty(final Property property, final String value)
	{
		setProperty(property, (Object) value);
	}

	private static void nullCheck(final Object value)
	{
		if (Objects.isNull(value))
		{
			throw new IllegalArgumentException("Value can't be null.");
		}
	}

	public static void restorePropertyToDefault(final Property property)
	{
		setProperty(property, property.defaultValue());
	}
}