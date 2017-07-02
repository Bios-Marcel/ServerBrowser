package com.msc.serverbrowser.logging;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.msc.serverbrowser.constants.Paths;

public class Logging
{
	private static Logger instance;

	/**
	 * @return {@link #instance}
	 */
	public static Logger logger()
	{
		if (Objects.isNull(instance))
		{
			init();
		}
		return instance;
	}

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
