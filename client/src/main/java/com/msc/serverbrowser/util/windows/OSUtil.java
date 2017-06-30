package com.msc.serverbrowser.util.windows;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

import com.msc.serverbrowser.logging.Logging;

public class OSUtil
{
	/**
	 * Preserved os name, since it won't change anyways and reading from a variable is faster.
	 */
	private final static String OS = System.getProperty("os.name").toLowerCase();

	/**
	 * @return true if the system is windows (most likely), otherwise false
	 */
	public static boolean isWindows()
	{
		return OS.startsWith("windows");
	}

	/**
	 * Opens a website using the default browser. It will automatically apply http:// infront of the
	 * url if not existant already.
	 *
	 * @param url
	 *            website to visit
	 */
	public static void browse(final String url)
	{
		final Desktop desktop = Desktop.getDesktop();
		final String fixedUrl = fixUrlIfNecessary(url);

		try
		{
			desktop.browse(URI.create(fixedUrl));
		}
		catch (final IOException exception)
		{
			Logging.logger().log(Level.WARNING, "Couldn't visit website '" + url + "' (" + fixedUrl + ").", exception);
		}
	}

	/**
	 * Puts <code>http://</code> infront of the url if not it already has <code>http://</code> or
	 * <code>https://</code> infront of it.
	 *
	 * @param url
	 *            the url to fix
	 * @return the fixed url or the original if there was no need to fix
	 */
	private static String fixUrlIfNecessary(final String url)
	{
		String website = url;
		if (!website.startsWith("http://") || !website.startsWith("https://"))
		{
			website = "http://" + website;
		}
		return website;
	}
}
