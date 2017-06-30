package com.msc.serverbrowser.logging;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.msc.serverbrowser.constants.Paths;

public class Logging extends Logger
{
	private static Logging instance;

	/**
	 * @return {@link #instance}
	 */
	public static Logging logger()
	{
		if (Objects.isNull(instance))
		{
			instance = new Logging();
			instance.init();
		}
		return instance;
	}

	private Logging()
	{
		super("SAMP-Logger", null);
	}

	private void init()
	{
		instance.setLevel(Level.INFO);
		try
		{
			final FileHandler filehandler = new FileHandler(Paths.SAMPEX_PATH + File.separator + "Log.log");
			final SimpleFormatter formatter = new SimpleFormatter();
			filehandler.setFormatter(formatter);
			instance.addHandler(filehandler);
		}
		catch (SecurityException | IOException e)
		{
			instance.log(Level.SEVERE, "Couldn't configure logger properly", e);
		}
	}
}
