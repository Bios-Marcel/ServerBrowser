package logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging
{
	public static final Logger logger = Logger.getAnonymousLogger();

	static
	{
		logger.setLevel(Level.INFO);
	}
}
