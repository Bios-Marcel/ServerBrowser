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
public final class Logging {
	/**
	 * The Loggers Singleton instance.
	 */
	private static Logger instance;

	private Logging() {
		// Constructor to prevent instantiation
	}

	static {
		init();
	}

	private static void init() {
		instance = Logger.getAnonymousLogger();
		instance.setLevel(Level.INFO);
		try {
			final FileHandler filehandler = new FileHandler(PathConstants.SAMPEX_LOG, true);
			final SimpleFormatter formatter = new SimpleFormatter();
			filehandler.setFormatter(formatter);
			instance.addHandler(filehandler);
		}
		catch (SecurityException | IOException exception) {
			instance.log(Level.SEVERE, "Couldn't configure logger properly", exception);
		}
	}

	/**
	 * Log a message, with associated Throwable information.
	 *
	 * @param logLevel One of the message level identifiers, e.g., SEVERE
	 * @param message The string message (or a key in the message catalog)
	 * @param throwable Throwable associated with log message.
	 */
	private static void log(final Level logLevel, final String message, final Throwable throwable) {
		instance.log(logLevel, message, throwable);
	}

	/**
	 * Log a message, with no arguments.
	 *
	 * @param logLevel One of the message level identifiers, e.g., SEVERE
	 * @param message The string message (or a key in the message catalog)
	 */
	private static void log(final Level logLevel, final String message) {
		instance.log(logLevel, message);
	}

	/**
	 * @param message The string message (or a key in the message catalog)
	 */
	public static void info(final String message) {
		log(Level.INFO, message);
	}

	/**
	 * @param message The string message (or a key in the message catalog)
	 * @param throwable the {@link Throwable} which has caused this logging action
	 */
	public static void info(final String message, final Throwable throwable) {
		log(Level.INFO, message, throwable);
	}

	/**
	 * @param message The string message (or a key in the message catalog)
	 */
	public static void warn(final String message) {
		log(Level.WARNING, message);
	}

	/**
	 * @param message The string message (or a key in the message catalog)
	 * @param throwable the {@link Throwable} which has caused this logging action
	 */
	public static void warn(final String message, final Throwable throwable) {
		log(Level.WARNING, message, throwable);
	}

	/**
	 * @param message The string message (or a key in the message catalog)
	 */
	public static void error(final String message) {
		log(Level.SEVERE, message);
	}

	/**
	 * @param message The string message (or a key in the message catalog)
	 * @param throwable the {@link Throwable} which has caused this logging action
	 */
	public static void error(final String message, final Throwable throwable) {
		log(Level.SEVERE, message, throwable);
	}
}
