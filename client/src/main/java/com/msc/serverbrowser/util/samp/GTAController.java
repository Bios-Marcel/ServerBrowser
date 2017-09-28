package com.msc.serverbrowser.util.samp;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotification;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;
import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.PastUsernames;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.SAMPVersion;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.StringUtility;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextInputDialog;

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

		killSAMP();
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
		catch (final RegistryException exception)
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
			return Optional.empty();
		}

		final Optional<String> path = getGtaPathFromRegistry();
		if (path.isPresent())
		{
			return path;
		}

		final String property = ClientPropertiesController.getPropertyAsString(Property.SAMP_PATH);
		if (Objects.isNull(property) || property.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(property.endsWith(File.separator) ? property : property + File.separator);
	}

	/**
	 * Should only be used if necessary.
	 *
	 * @return String of the GTA Path or null.
	 */
	private static Optional<String> getGtaPathFromRegistry()
	{
		try
		{
			return Optional.ofNullable(WindowsRegistry.getInstance()
					.readString(HKey.HKCU, "SOFTWARE\\SAMP", "gta_sa_exe")
					.replace("gta_sa.exe", ""));
		}
		catch (final RegistryException exception)
		{
			Logging.log(Level.WARNING, "Couldn't retrieve GTA path.", exception);
			return Optional.empty();
		}
	}

	/**
	 * Returns the {@link SAMPVersion} value that represents the currently installed samp version.
	 *
	 * @return {@link Optional} of installed versions version number or an {@link Optional#empty()}
	 */
	public static Optional<SAMPVersion> getInstalledVersion()
	{
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
		return SAMPVersion.findVersionByDLLSize((int) file.length());
	}

	/**
	 * Connects to a server, depending on if it is passworded, the user will be asked to enter a
	 * password. If the server is not reachable the user can not connect.
	 *
	 * @param address
	 *            server address
	 * @param port
	 *            server port
	 */
	public static void tryToConnect(final String address, final Integer port)
	{
		try (final SampQuery query = new SampQuery(address, port))
		{
			final Optional<String[]> serverInfo = query.getBasicServerInfo();

			if (serverInfo.isPresent() && StringUtility.stringToBoolean(serverInfo.get()[0]))
			{
				final TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle("Connect to Server");
				dialog.setHeaderText("Enter the servers password (Leave empty if u think there is none).");

				final Optional<String> result = dialog.showAndWait();
				result.ifPresent(password -> GTAController.connectToServer(address, port, password));
			}
			else
			{
				GTAController.connectToServer(address, port, "");
			}
		}
		catch (final IOException exception)
		{
			Logging.log(Level.WARNING, "Couldn't connect to server.", exception);
			showCantConnectToServerError();
		}
	}

	private static boolean connectWithDLLInjection(final String address, final Integer port, final String password)
	{
		final ProcessBuilder builder = new ProcessBuilder();

		final Optional<String> path = getGtaPath();

		if (path.isPresent())
		{
			builder.directory(new File(path.get()));

			final List<String> arguments = new ArrayList<>();
			arguments.add(PathConstants.SAMP_CMD);
			arguments.add("-c");
			arguments.add("-h");
			arguments.add(address);
			arguments.add("-p");
			arguments.add(port.toString());
			arguments.add("-n");
			arguments.add(retrieveUsernameFromRegistry());
			if (Objects.nonNull(password) && !password.isEmpty())
			{
				arguments.add("-z");
				arguments.add(password);
			}
			builder.command(arguments);

			try
			{
				builder.start();
				return true;
			}
			catch (final IOException e)
			{
				Logging.log(Level.WARNING, "Error using sampcmd.exe", e);
			}
		}

		return false;
	}

	/**
	 * Shows a TrayNotification that states, that connecting to the server wasn't possible.
	 */
	public static void showCantConnectToServerError()
	{
		new TrayNotificationBuilder()
				.type(NotificationTypeImplementations.ERROR)
				.title("Can't connect to Server")
				.message("The address that you have entered, doesn't seem to be valid.")
				.animation(Animations.POPUP)
				.build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);
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
	public static void killSAMP()
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
	 * @param address
	 *            server address
	 * @param port
	 *            server port
	 * @param password
	 *            the password to use for connecting
	 */
	public static void connectToServer(final String address, final Integer port, final String password)
	{
		if (ClientPropertiesController.getPropertyAsBoolean(Property.ALLOW_CLOSE_GTA))
		{
			killGTA();
		}

		final Optional<String> gtaPath = getGtaPath();
		if (gtaPath.isPresent())
		{
			if (!connectWithDLLInjection(address, port, password))
			{
				final String ipAndPort = address + ":" + port;
				try
				{
					Logging.log(Level.INFO, "Connecting using executeable.");
					final ProcessBuilder builder = new ProcessBuilder(gtaPath.get() + File.separator + "samp.exe ", ipAndPort, password);
					builder.directory(new File(gtaPath.get()));
					builder.start();
				}
				catch (final IOException exception)
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
		}
		else
		{
			final TrayNotification trayNotification = new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.ERROR)
					.title("GTA couldn't be located")
					.message("Click here to locate your GTA path manually.")
					.animation(Animations.POPUP)
					.build();

			// TODO(MSC) Improve and try to focus component
			trayNotification.setOnMouseClicked(__ -> Client.getInstance().loadView(View.SETTINGS));

			trayNotification.showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);

		}
	}
}
