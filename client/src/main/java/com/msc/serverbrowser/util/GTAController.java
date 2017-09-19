package com.msc.serverbrowser.util;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;
import com.msc.serverbrowser.data.PastUsernames;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.SAMPVersion;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

/**
 * Contains utility methods for interacting with native samp stuff.
 *
 * @author Marcel
 */
public final class GTAController
{
	/**
	 * Holds the users username.
	 */
	public static StringProperty usernameProperty = new SimpleStringProperty(retrieveUsernameFromRegistry());

	private GTAController()
	{
		// Constructor to prevent instantiation
	}

	/**
	 * Writes the actual username (from registry) into the past usernames list and sets the new name
	 */
	public static void applyUsername()
	{
		if (!OSUtility.isWindows())
		{
			return;
		}

		killSamp();
		PastUsernames.addPastUsername(retrieveUsernameFromRegistry());
		try
		{
			WindowsRegistry.getInstance().writeStringValue(HKey.HKCU, "SOFTWARE\\SAMP", "PlayerName", usernameProperty.get());
		}
		catch (final RegistryException e)
		{
			Logging.log(Level.WARNING, "Couldn't set username.", e);
		}

	}

	// TODO Think of a better solution
	/**
	 * Returns the Username that samp has set in the registry.
	 *
	 * @return Username or "404 name not found"
	 */
	private static String retrieveUsernameFromRegistry()
	{
		if (!OSUtility.isWindows())
		{
			return "You are on Linux ;D";
		}

		try
		{
			return WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "PlayerName");
		}
		catch (final Exception exception)
		{
			Logging.log(Level.WARNING, "Couldn't retrieve Username from registry.", exception);
			return "404 Name not found";
		}
	}

	/**
	 * Returns the GTA path.
	 *
	 * @return {@link Optional} of GTA path or an empty {@link Optional} if GTA couldn't be found
	 */
	public static Optional<String> getGtaPath()
	{
		if (!OSUtility.isWindows())
		{
			return Optional.of("You are on Linux ;D");
		}

		final String property = ClientPropertiesController.getPropertyAsString(Property.SAMP_PATH);

		if (Objects.isNull(property) || property.isEmpty())
		{
			return Optional.empty();
		}
		return property.endsWith("\\") ? Optional.of(property) : Optional.of(property + "\\");
	}

	/**
	 * Should only be used if necessary.
	 *
	 * @return String of the GTA Path or null.
	 */
	public static String getGtaPathUnsafe()
	{
		if (!OSUtility.isWindows())
		{
			return "You are on Linux ;D";
		}

		try
		{
			return WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "gta_sa_exe").replace("gta_sa.exe", "");
		}
		catch (final RegistryException exception)
		{
			Logging.log(Level.WARNING, "Couldn't retrieve GTA path.", exception);
			return null;
		}
	}

	/**
	 * Returns the version number of the installed samp version, if samp and gta have been found.
	 *
	 * @return {@link Optional} of installed versions version number or an empty {@link Optional}
	 */
	public static Optional<SAMPVersion> getInstalledVersion()
	{
		if (!OSUtility.isWindows())
		{// OS not supported
			return Optional.empty();
		}

		final Optional<String> path = getGtaPath();
		if (!path.isPresent())
		{// GTA couldn't be found
			return Optional.empty();
		}

		final File file = new File(path.get() + "samp.dll");
		if (!file.exists())
		{// samp.dll doesn't exist, even though GTA is installed at this point.
			return Optional.empty();
		}

		/*
		 * Bad Practice, will cause an error if Kalcor decides to do a huge update someday :P
		 */
		switch ((int) file.length())
		{
			case 2199552:
				return Optional.of(SAMPVersion.ZeroThreeSeven);
			case 1093632:
				return Optional.of(SAMPVersion.ZeroThreeZ);
			case 2084864:
				return Optional.of(SAMPVersion.ZeroThreeX);
			case 1998848:
				return Optional.of(SAMPVersion.ZeroThreeE);
			case 2015232:
				return Optional.of(SAMPVersion.ZeroThreeD);
			case 1511424:
				return Optional.of(SAMPVersion.ZeroThreeC);
			case 610304:
				return Optional.of(SAMPVersion.ZeroThreeA);
			default:
				return Optional.empty();
		}

	}

	/**
	 * Connects to the given server (IP and Port) using an empty (no) password. Other than
	 * {@link GTAController#connectToServer(String)} and
	 * {@link GTAController#connectToServer(String, String)}, this method uses the
	 * <code>samp://</code> protocol to connect to make the samp launcher connect to the server.
	 *
	 * @param ipAndPort
	 *            the server to connect to
	 * @return true if it was most likely successful
	 */
	private static boolean connectToServerUsingProtocol(final String ipAndPort)
	{
		if (!OSUtility.isWindows())
		{
			return false;
		}

		try
		{
			Logging.log(Level.INFO, "Connecting using protocol.");
			final Desktop desktop = Desktop.getDesktop();

			if (desktop.isSupported(Action.BROWSE))
			{
				desktop.browse(new URI("samp://" + ipAndPort));
				return true;
			}
		}
		catch (final IOException | URISyntaxException exception)
		{
			Logging.log(Level.WARNING, "Error connecting to server.", exception);
		}

		return false;
	}

	/**
	 * Kills SA-MP using the command line.
	 */
	public static void killSamp()
	{
		kill("samp.exe");
	}

	/**
	 * Kills GTA using the command line.
	 */
	public static void killGTA()
	{
		kill("gta_sa.exe");
	}

	/**
	 * Kills a process with a given name.
	 *
	 * @param processName
	 *            the name that determines what processes will be killed
	 */
	private static void kill(final String processName)
	{
		if (!OSUtility.isWindows())
		{
			return;
		}

		try
		{
			Runtime.getRuntime().exec("taskkill /F /IM " + processName);
		}
		catch (final IOException exception)
		{
			Logging.log(Level.SEVERE, "Couldn't kill " + processName, exception);
		}
	}

	/**
	 * Connects to the given server (IP and Port) using the given password. Uses the commandline to
	 * open samp and connect to the server.
	 *
	 * @param ipAndPort
	 *            the server to connect to
	 * @param password
	 *            the password to use for connecting
	 */
	public static void connectToServer(final String ipAndPort, final String password)
	{
		if (ClientPropertiesController.getPropertyAsBoolean(Property.ALLOW_CLOSE_GTA))
		{
			killGTA();
		}

		final Optional<String> gtaPath = getGtaPath();
		if (gtaPath.isPresent())
		{
			try
			{
				Logging.log(Level.INFO, "Connecting using executeable.");
				final ProcessBuilder builder = new ProcessBuilder(gtaPath.get() + File.separator + "samp.exe ", ipAndPort, password);
				builder.directory(new File(gtaPath.get()));
				builder.start();
			}
			catch (final Exception exception)
			{
				if (Objects.isNull(password) || password.isEmpty())
				{
					connectToServerUsingProtocol(ipAndPort);
				}
				else
				{
					Logging.log(Level.WARNING, "Couldn't connect to server", exception);
				}
			}
		}
		else
		{
			new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.ERROR)
					.title("GTA couldn't be located")
					.message("If this isn't correct, please head to the settings view and manually enter your GTA path.")
					.animation(Animations.POPUP)
					.build().showAndDismiss(Duration.seconds(10));
		}
	}

	/**
	 * Connects to the given server (IP and Port) using an empty (no) password.
	 *
	 * @param ipAndPort
	 *            the server to connect to
	 * @see GTAController#connectToServer(String, String)
	 */
	public static void connectToServer(final String ipAndPort)
	{
		connectToServer(ipAndPort, "");
	}
}
