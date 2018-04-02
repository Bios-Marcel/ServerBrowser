package serverbrowser.data.properties

import serverbrowser.data.SQLDatabase
import serverbrowser.logging.Logging
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

/**
 * Singleton object for retrieving and setting the applications properties.
 *
 * @author Marcel
 * @since 17.09.2017
 */
object ClientPropertiesController {

    /**
     * Retrieves a property, if the property-type is not supported, the [default value][Property.defaultValue] will be returned.
     *
     * @param property the requested [Property]
     * @return the value of the requested [Property] or its [default value][Property.defaultValue]
     */
    fun <T> getProperty(property: Property<T>): T {
        return when (property.defaultValue) {
            is String -> getPropertyAsString(property)
            is Double -> getPropertyAsString(property).toDouble()
            is Int -> getPropertyAsString(property).toInt()
            is Boolean -> getPropertyAsString(property).toBoolean()
            else -> property.defaultValue
        } as T
    }

    /**
     * Returns the [default value][Property.defaultValue] of a given [Property].
     *
     * @param property the given [Property]
     * @return the [default value][Property.defaultValue] of the given [Property]
     */
    fun <T> getDefaultProperty(property: Property<T>) = property.defaultValue

    /**
     * Retrieves a value for a specific key from [Property].
     *
     * @param property the key
     * @return the value for the given [Property]
     */
    private fun <T> getPropertyAsString(property: Property<T>): String {
        val statement = "SELECT value FROM setting WHERE id = " + property.id + ";"
        val resultSetOptional = SQLDatabase.executeGetResult(statement)
        if (resultSetOptional != null) {
            try {
                resultSetOptional.use { resultSet: ResultSet ->
                    if (resultSet.next()) {
                        return resultSet.getString("value") ?: property.defaultValue.toString()
                    }
                }
            } catch (exception: SQLException) {
                Logging.error("Could not set the property as a String.", exception)
            }

        }

        return property.defaultValue.toString()

    }

    /**
     * Saves a new value in the given [Property].
     *
     * @param property the [Property] of which the value shall be set
     * @param value the value which will be saved
     */
    fun <T> setProperty(property: Property<T>, value: T) {
        val query = "INSERT OR REPLACE INTO setting (id, value) VALUES(?, ?);"
        try {
            SQLDatabase.createPreparedStatement(query).use { statement ->
                statement.setInt(1, property.id)
                statement.setString(2, if (Objects.nonNull(value)) value.toString() else null)
                statement.execute()
            }
        } catch (exception: SQLException) {
            Logging.error("Error setting property: $property to value $value", exception)
        }

    }

    /**
     * Restores a property to its hardcoded default value.
     *
     * @param property the property restore its default
     */
    fun <T> restorePropertyToDefault(property: Property<T>) {
        setProperty(property, property.defaultValue)
    }
}
