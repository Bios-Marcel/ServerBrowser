package com.msc.serverbrowser.data.properties;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import com.msc.serverbrowser.data.SQLDatabase;
import com.msc.serverbrowser.logging.Logging;

/**
 * @author Marcel
 * @since 17.09.2017
 */
public final class ClientPropertiesController {
	private ClientPropertiesController() {
		// Constructor to prevent instantiation
	}

	/**
	 * Returns a properties default as an integer if the datatype is correct.
	 *
	 * @param property
	 *            the property to return its default valeu as an integer
	 * @return the default of the property as integer
	 */
	public static Integer getDefaultAsInt(final Property property) {
		checkDataType(property, Integer.class);

		return Integer.parseInt(property.defaultValue());
	}

	/**
	 * Retrieves a value for a specific key from {@link Property}.
	 *
	 * @param property
	 *            the key
	 * @return the value for the given {@link Property}
	 */
	public static String getPropertyAsString(final Property property) {
		final String statement = "SELECT value FROM setting WHERE id = " + property.getId() + ";";
		final Optional<ResultSet> resultSetOptional = SQLDatabase.getInstance().executeGetResult(statement);
		if (resultSetOptional.isPresent()) {
			try (final ResultSet resultSet = resultSetOptional.get()) {
				if (resultSet.next()) {
					return Optional
									.ofNullable(resultSet.getString("value"))
									.orElse(property.defaultValue());
				}
			} catch (final SQLException exception) {
				Logging.log(Level.SEVERE, "Could not set the property as a String.", exception);
			}
		}

		return property.defaultValue();

	}

	/**
	 * Retrieves a value for a specific key from {@link Property}.
	 *
	 * @param property
	 *            the key
	 * @return the value for the given {@link Property}
	 */
	public static Integer getPropertyAsInt(final Property property) {
		checkDataType(property, Integer.class);

		final String originalValue = getPropertyAsString(property);

		return Integer.parseInt(originalValue);
	}

	/**
	 * Retrieves a value for a specific key from {@link Property}.
	 *
	 * @param property
	 *            the key
	 * @return the value for the given {@link Property}
	 */
	public static Float getPropertyAsFloat(final Property property) {
		checkDataType(property, Float.class);

		final String originalValue = getPropertyAsString(property);

		return Float.parseFloat(originalValue);
	}

	/**
	 * Retrieves a value for a specific key from {@link Property}.
	 *
	 * @param property
	 *            the key
	 * @return the value for the given {@link Property}
	 */
	public static Boolean getPropertyAsBoolean(final Property property) {
		checkDataType(property, Boolean.class);

		return Boolean.parseBoolean(getPropertyAsString(property));
	}

	private static void checkDataType(final Property property, final Class<?> datatype) {
		if (!property.datatype().equals(datatype)) {
			throw new IllegalArgumentException("Datatype is " + datatype.getName() + " ; Expected: " + property.datatype().getName());
		}
	}

	/**
	 * Sets a value for a specific key from {@link Property}.
	 *
	 * @param property
	 *            the key
	 * @param value
	 *            the value that will be set
	 */
	public static void setProperty(final Property property, final Object value) {
		setProperty(property, value, false);
	}

	private static void setProperty(final Property property, final Object value, final boolean omitCheck) {
		if (Objects.nonNull(value) && !omitCheck) {// Check will only be performed if its non null
			checkDataType(property, value.getClass());
		}
		String statement = null;
		if (Objects.isNull(value)) {
			statement = "INSERT OR REPLACE INTO setting (id, value) VALUES({0}, NULL);";
			statement = MessageFormat.format(statement, property.getId());
		} else {
			statement = "INSERT OR REPLACE INTO setting (id, value) VALUES({0}, ''{1}'');";
			statement = MessageFormat.format(statement, property.getId(), value);
		}
		SQLDatabase.getInstance().execute(statement);
	}

	/**
	 * Sets a value as an {@link Float} for a specific key from {@link Property}.
	 *
	 * @param property
	 *            the key
	 * @param value
	 *            the value that will be set
	 */
	public static void setProperty(final Property property, final Float value) {
		nullCheck(value);
		setProperty(property, (Object) value);
	}

	/**
	 * Sets a value as an {@link Integer} for a specific key from {@link Property}.
	 *
	 * @param property
	 *            the key
	 * @param value
	 *            the value that will be set
	 */
	public static void setProperty(final Property property, final Integer value) {
		nullCheck(value);
		setProperty(property, (Object) value);
	}

	/**
	 * Sets a value as a {@link Boolean} for a specific key from {@link Property}.
	 *
	 * @param property
	 *            the key
	 * @param value
	 *            the value that will be set
	 */
	public static void setProperty(final Property property, final Boolean value) {
		nullCheck(value);
		setProperty(property, (Object) value);
	}

	/**
	 * Sets a value as a {@link String} for a specific key from {@link Property}.
	 *
	 * @param property
	 *            the key
	 * @param value
	 *            the value that will be set
	 */
	public static void setProperty(final Property property, final String value) {
		setProperty(property, (Object) value);
	}

	private static void nullCheck(final Object value) {
		if (Objects.isNull(value)) {
			throw new IllegalArgumentException("Value can't be null.");
		}
	}

	/**
	 * Restores a property to its hardcoded default value.
	 *
	 * @param property
	 *            the property restopre its default
	 */
	public static void restorePropertyToDefault(final Property property) {
		setProperty(property, property.defaultValue(), true);
	}
}