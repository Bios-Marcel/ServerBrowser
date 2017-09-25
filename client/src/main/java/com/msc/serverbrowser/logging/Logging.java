package com.msc.serverbrowser.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.msc.serverbrowser.constants.PathConstants;

/**
 * Initializes and holds the applications {@link Logger} instance and offers proxy methods.
 *
 * @author Marcel
 * @since 06.07.2017
 */
public final class Logging
{
	/**
	 * The Loggers Singleton instance.
	 */
	private static Logger instance;

	private Logging()
	{
		// Constructor to prevent instantiation
	}

	static
	{
		init();
	}

	private static void init()
	{
		instance = Logger.getAnonymousLogger();
		instance.setLevel(Level.INFO);
		try
		{
			final FileHandler filehandler = new FileHandler(PathConstants.SAMPEX_LOG);
			final SimpleFormatter formatter = new SimpleFormatter();
			filehandler.setFormatter(formatter);
			instance.addHandler(filehandler);
		}
		catch (SecurityException | IOException exception)
		{
			instance.log(Level.SEVERE, "Couldn't configure logger properly", exception);
		}
	}

	/**
	 * Log a message, with associated Throwable information.
	 * <p>
	 * If the logger is currently enabled for the given message level then the given arguments are
	 * stored in a LogRecord which is forwarded to all registered output handlers.
	 * <p>
	 * Note that the thrown argument is stored in the LogRecord thrown property, rather than the
	 * LogRecord parameters property. Thus it is processed specially by output Formatters and is not
	 * treated as a formatting parameter to the LogRecord message property.
	 * <p>
	 *
	 * @param logLevel
	 *            One of the message level identifiers, e.g., SEVERE
	 * @param message
	 *            The string message (or a key in the message catalog)
	 * @param throwable
	 *            Throwable associated with log message.
	 */
	public static void log(final Level logLevel, final String message, final Throwable throwable)
	{
		instance.log(logLevel, message, throwable);
	}

	/**
	 * Log a message, with no arguments.
	 * <p>
	 * If the logger is currently enabled for the given message level then the given message is
	 * forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param logLevel
	 *            One of the message level identifiers, e.g., SEVERE
	 * @param message
	 *            The string message (or a key in the message catalog)
	 */
	public static void log(final Level logLevel, final String message)
	{
		instance.log(logLevel, message);
	}

	/**
	 * Log an INFO message.
	 * <p>
	 * If the logger is currently enabled for the INFO message level then the given message is
	 * forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param message
	 *            The string message (or a key in the message catalog)
	 */
	public static void info(final String message)
	{
		instance.info(message);
	}
}
