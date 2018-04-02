package serverbrowser.logging

import serverbrowser.constants.PathConstants

import java.io.IOException
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

/**
 * Initializes and holds the applications [Logger] instance and offers proxy methods.
 *
 * @author Marcel
 * @since 06.07.2017
 */
object Logging {
    /**
     * The Loggers Singleton instance.
     */
    private var instance: Logger? = null

    init {
        init()
    }

    private fun init() {
        instance = Logger.getAnonymousLogger()
        instance!!.level = Level.INFO
        try {
            val filehandler = FileHandler(PathConstants.SAMPEX_LOG, true)
            val formatter = SimpleFormatter()
            filehandler.formatter = formatter
            instance!!.addHandler(filehandler)
        } catch (exception: SecurityException) {
            instance!!.log(Level.SEVERE, "Couldn't configure logger properly", exception)
        } catch (exception: IOException) {
            instance!!.log(Level.SEVERE, "Couldn't configure logger properly", exception)
        }

    }

    /**
     * Log a message, with associated Throwable information.
     *
     * @param logLevel One of the message level identifiers, e.g., SEVERE
     * @param message The string message (or a key in the message catalog)
     * @param throwable Throwable associated with log message.
     */
    private fun log(logLevel: Level, message: String, throwable: Throwable) {
        instance!!.log(logLevel, message, throwable)
    }

    /**
     * Log a message, with no arguments.
     *
     * @param logLevel One of the message level identifiers, e.g., SEVERE
     * @param message The string message (or a key in the message catalog)
     */
    private fun log(logLevel: Level, message: String) {
        instance!!.log(logLevel, message)
    }

    /**
     * @param message The string message (or a key in the message catalog)
     */
    @JvmStatic
    fun info(message: String) {
        log(Level.INFO, message)
    }

    /**
     * @param message The string message (or a key in the message catalog)
     * @param throwable the [Throwable] which has caused this logging action
     */
    fun info(message: String, throwable: Throwable) {
        log(Level.INFO, message, throwable)
    }

    /**
     * @param message The string message (or a key in the message catalog)
     */
    fun warn(message: String) {
        log(Level.WARNING, message)
    }

    /**
     * @param message The string message (or a key in the message catalog)
     * @param throwable the [Throwable] which has caused this logging action
     */
    fun warn(message: String, throwable: Throwable) {
        log(Level.WARNING, message, throwable)
    }

    /**
     * @param message The string message (or a key in the message catalog)
     */
    fun error(message: String) {
        log(Level.SEVERE, message)
    }

    /**
     * @param message The string message (or a key in the message catalog)
     * @param throwable the [Throwable] which has caused this logging action
     */
    fun error(message: String, throwable: Throwable) {
        log(Level.SEVERE, message, throwable)
    }
}// Constructor to prevent instantiation
