package com.msc.serverbrowser.logging;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.msc.serverbrowser.constants.Paths;

/**
 * Initializes and holds the applications {@link Logger} instance
 *
 * @author Marcel
 * @since 06.07.2017
 */
public class Logging
{
	/**
	 * The Loggers Singleton instance.
	 */
	private static Logger instance;

	/**
	 * @return the Loggers singleton instance
	 */
	public static Logger logger()
	{
		if (Objects.isNull(instance))
		{
			init();
		}
		return instance;
	}

	@SuppressWarnings("null") // Anonysmus logger shouldn't be null
	private static void init()
	{
		instance = Logger.getAnonymousLogger();
		instance.setLevel(Level.INFO);
		try
		{
			final FileHandler filehandler = new FileHandler(Paths.SAMPEX_PATH + File.separator + "Log.log");
			final SimpleFormatter formatter = new SimpleFormatter();
			filehandler.setFormatter(formatter);
			instance.addHandler(filehandler);
		}
		catch (SecurityException | IOException exception)
		{
			instance.log(Level.SEVERE, "Couldn't configure logger properly", exception);
		}
	}
}
