package com.msc.serverbrowser.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import org.eclipse.jdt.annotation.Nullable;

import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;
import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.PastUsernames;
import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.windows.OSUtil;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Contains utility methods for interacting with native samp stuff.
 *
 * @author Marcel
 */
public class GTA
{
	/**
	 * Holds the users username.
	 */
	public static StringProperty usernameProperty = new SimpleStringProperty(retrieveUsernameFromRegistry());

	/**
	 * Writes the actual username (from registry) into the past usernames list and sets the new name
	 */
	public static void applyUsername()
	{
		if (!OSUtil.isWindows())
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
			Logging.logger().log(Level.WARNING, "Couldn't set username.", e);
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
		if (!OSUtil.isWindows())
		{
			return "You are on Linux ;D";
		}

		try
		{
			return WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "PlayerName");
		}
		catch (final Exception exception)
		{
			Logging.logger().log(Level.WARNING, "Couldn't retrieve Username from registry.", exception);
			return "404 Name not found";
		}
	}

	/**
	 * Returns the GTA path.
	 *
	 * @return {@link Optional} of GTA path or an empty {@link Optional} if GTA couldn't be found
	 */
	@SuppressWarnings("null") // Can't be null
	public static Optional<String> getGtaPath()
	{
		if (!OSUtil.isWindows())
		{
			return Optional.of("You are on Linux ;D");
		}

		final String property = ClientProperties.getPropertyAsString(Property.SAMP_PATH);

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
	public static @Nullable String getGtaPathUnsafe()
	{
		if (!OSUtil.isWindows())
		{
			return "You are on Linux ;D";
		}

		try
		{
			return WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "gta_sa_exe").replace("gta_sa.exe", "");
		}
		catch (final RegistryException exception)
		{
			Logging.logger().log(Level.WARNING, "Couldn't retrieve GTA path.", exception);
			return null;
		}
	}

	/**
	 * Returns the version number of the installed samp version, if samp and gta have been found.
	 *
	 * @return {@link Optional} of installed versions version number or an empty {@link Optional}
	 */
	@SuppressWarnings("null")
	public static Optional<String> getInstalledVersion()
	{
		if (!OSUtil.isWindows())
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
				return Optional.of("0.3.7");
			case 1093632:
				return Optional.of("0.3z");
			case 2084864:
				return Optional.of("0.3x");
			case 1998848:
				return Optional.of("0.3e");
			case 2015232:
				return Optional.of("0.3d");
			case 1511424:
				return Optional.of("0.3c");
			case 610304:
				return Optional.of("0.3a");
			default:
				return Optional.empty();
		}

	}

	/**
	 * Connects to the given server (IP and Port) using an empty (no) password. Other than
	 * {@link GTA#connectToServer(String)} and {@link GTA#connectToServer(String, String)}, this
	 * method uses the <code>samp://</code> protocol to connect to make the samp launcher connect to
	 * the server.
	 *
	 * @param ipAndPort
	 *            the server to connect to
	 * @return true if it was most likely successful
	 */
	private static boolean connectToServerUsingProtocol(final String ipAndPort)
	{
		if (!OSUtil.isWindows())
		{
			return false;
		}

		Logging.logger().log(Level.INFO, "Connecting using protocol.");
		try
		{
			final Desktop d = Desktop.getDesktop();
			d.browse(new URI("samp://" + ipAndPort));
			return true;
		}
		catch (final Exception exception)
		{
			exception.printStackTrace();
			return false;
		}
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
		if (!OSUtil.isWindows())
		{
			return;
		}

		try
		{
			Runtime.getRuntime().exec("taskkill /F /IM " + processName);
		}
		catch (final IOException exception)
		{
			Logging.logger().log(Level.SEVERE, "Couldn't kill " + processName, exception);
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
		if (ClientProperties.getPropertyAsBoolean(Property.ALLOW_CLOSE_GTA))
		{
			killGTA();
		}

		final Optional<String> gtaPath = getGtaPath();
		if (gtaPath.isPresent())
		{
			try
			{
				Logging.logger().log(Level.INFO, "Connecting using executeable.");
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
					Logging.logger().log(Level.WARNING, "Couldn't connect to server", exception);
				}
			}
		}
		else
		{
			final Alert alert = new Alert(AlertType.ERROR);
			Client.setupDialog(alert);
			alert.setTitle("Connecting to server");
			alert.setHeaderText("GTA couldn't be located");
			alert.setContentText("It seems like you don't have GTA installed.");
			alert.showAndWait();
		}
	}

	/**
	 * Connects to the given server (IP and Port) using an empty (no) password.
	 *
	 * @param ipAndPort
	 *            the server to connect to
	 * @see GTA#connectToServer(String, String)
	 */
	public static void connectToServer(final String ipAndPort)
	{
		connectToServer(ipAndPort, "");
	}
}
