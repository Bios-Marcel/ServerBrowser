package com.msc.serverbrowser.data.properties;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

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
	 * @param property the property to return its default valeu as an integer
	 * @return the default of the property as integer
	 */
	public static Integer getDefaultAsInt(final Property property) {
		checkDataType(property, Integer.class);

		return Integer.parseInt(property.getDefaultValue());
	}

	/**
	 * Retrieves a value for a specific key from {@link Property}.
	 *
	 * @param property the key
	 * @return the value for the given {@link Property}
	 */
	public static String getPropertyAsString(final Property property) {
		final String statement = "SELECT value FROM setting WHERE id = " + property.getId() + ";";
		final Optional<ResultSet> resultSetOptional = SQLDatabase.getInstance().executeGetResult(statement);
		if (resultSetOptional.isPresent()) {
			try (ResultSet resultSet = resultSetOptional.get()) {
				if (resultSet.next()) {
					return Optional
							.ofNullable(resultSet.getString("value"))
							.orElse(property.getDefaultValue());
				}
			}
			catch (final SQLException exception) {
				Logging.error("Could not set the property as a String.", exception);
			}
		}

		return property.getDefaultValue();

	}

	/**
	 * Retrieves a value for a specific key from {@link Property}.
	 *
	 * @param property the key
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
	 * @param property the key
	 * @return the value for the given {@link Property}
	 */
	public static Double getPropertyAsDouble(final Property property) {
		checkDataType(property, Double.class);

		final String originalValue = getPropertyAsString(property);

		return Double.parseDouble(originalValue);
	}

	/**
	 * Retrieves a value for a specific key from {@link Property}.
	 *
	 * @param property the key
	 * @return the value for the given {@link Property}
	 */
	public static Boolean getPropertyAsBoolean(final Property property) {
		checkDataType(property, Boolean.class);

		return Boolean.parseBoolean(getPropertyAsString(property));
	}

	private static void checkDataType(final Property property, final Class<?> datatype) {
		if (!property.getDatatype().equals(datatype)) {
			throw new IllegalArgumentException("Datatype is " + datatype.getName() + " ; Expected: " + property.getDatatype().getName());
		}
	}

	/**
	 * Sets a value for a specific key from {@link Property}.
	 *
	 * @param property the key
	 * @param value the value that will be set
	 */
	private static void setProperty(final Property property, final Object value) {
		setProperty(property, value, false);
	}

	private static void setProperty(final Property property, final Object value, final boolean omitCheck) {
		if (Objects.nonNull(value) && !omitCheck) {// Check will only be performed if its non null
			checkDataType(property, value.getClass());
		}

		final String query = "INSERT OR REPLACE INTO setting (id, value) VALUES(?, ?);";
		try (PreparedStatement statement = SQLDatabase.getInstance().createPreparedStatement(query)) {
			statement.setInt(1, property.getId());
			statement.setString(2, Objects.nonNull(value) ? value.toString() : null);

			statement.execute();
		}
		catch (final SQLException exception) {
			Logging.error("Error setting property: " + property + " to value " + value, exception);
		}
	}

	/**
	 * Sets a value as an {@link Double} for a specific key from {@link Property}.
	 *
	 * @param property the key
	 * @param value the value that will be set
	 */
	public static void setProperty(final Property property, final Double value) {
		nullCheck(value);
		setProperty(property, (Object) value);
	}

	/**
	 * Sets a value as an {@link Integer} for a specific key from {@link Property}.
	 *
	 * @param property the key
	 * @param value the value that will be set
	 */
	public static void setProperty(final Property property, final Integer value) {
		nullCheck(value);
		setProperty(property, (Object) value);
	}

	/**
	 * Sets a value as a {@link Boolean} for a specific key from {@link Property}.
	 *
	 * @param property the key
	 * @param value the value that will be set
	 */
	public static void setProperty(final Property property, final Boolean value) {
		nullCheck(value);
		setProperty(property, (Object) value);
	}

	/**
	 * Sets a value as a {@link String} for a specific key from {@link Property}.
	 *
	 * @param property the key
	 * @param value the value that will be set
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
	 * @param property the property restopre its default
	 */
	public static void restorePropertyToDefault(final Property property) {
		setProperty(property, property.getDefaultValue(), true);
	}
}