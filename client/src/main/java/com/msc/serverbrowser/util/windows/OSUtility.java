package com.msc.serverbrowser.util.windows;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.StringUtility;

/**
 * @author Marcel
 * @since 19.09.2017
 */
public final class OSUtility {
	/**
	 * Preserved os name, since it won't change anyways and reading from a variable is faster.
	 */
	private final static String OS = System.getProperty("os.name").toLowerCase();

	private OSUtility() {
		// Constructor to prevent instantiation
	}

	/**
	 * @return true if the system is windows (most likely), otherwise false
	 */
	public static boolean isWindows() {
		return OS.startsWith("windows");
	}

	/**
	 * Opens a website using the default browser. It will automatically apply http:// in front of the
	 * url if not existent already.
	 *
	 * @param urlAsString website to visit
	 */
	public static void browse(final String urlAsString) {
		try {
			final String fixedUrl = StringUtility.fixUrlIfNecessary(urlAsString);
			final URL url = new URL(fixedUrl);
			browse(new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef()));
		}
		catch (final IOException | URISyntaxException exception) {
			Logging.warn("Couldn't visit website '" + urlAsString + "'", exception);
		}
	}

	/**
	 * Opens a website using the default browser.
	 *
	 * @param uri Website which shall be visited
	 */
	public static void browse(final URI uri) {
		if (Desktop.isDesktopSupported()) {
			/*
			 * HACK Workaround for Unix, since the Desktop Class seems to freeze the application
			 * unless the call is threaded.
			 */
			new Thread(() -> {
				try {
					Desktop.getDesktop().browse(uri);
				}
				catch (final IOException exception) {
					Logging.warn("Couldn't visit website '" + uri + "'", exception);
				}
			}).start();
		}
	}
}
