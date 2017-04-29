package logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging
{
	public static final Logger logger = Logger.getAnonymousLogger();

	static
	{
		logger.setLevel(Level.INFO);
		try
		{
			final FileHandler filehandler = new FileHandler(System.getProperty("user.home") + File.separator + "sampex" + File.separator
					+ "Log.log");
			final SimpleFormatter formatter = new SimpleFormatter();
			filehandler.setFormatter(formatter);
			logger.addHandler(filehandler);
		}
		catch (SecurityException | IOException e)
		{
			logger.log(Level.SEVERE, "Couldn't configure logger properly", e);
		}

	}
}
