package util;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;

import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;

import data.PastUsernames;
import javafx.beans.property.SimpleStringProperty;
import logging.Logging;

public class GTA
{
	private static SimpleStringProperty username = new SimpleStringProperty(retrieveUsernameFromRegistry());

	public static SimpleStringProperty usernameProperty()
	{
		return username;
	}

	/**
	 * Writes the actual username (from registry) into the past usernames list and sets the new name
	 */
	public static void applyUsername()
	{
		PastUsernames.addPastUsername(retrieveUsernameFromRegistry());
		try
		{
			WindowsRegistry.getInstance().writeStringValue(HKey.HKCU, "SOFTWARE\\SAMP", "PlayerName", usernameProperty().get());
		}
		catch (final RegistryException e)
		{
			Logging.logger.log(Level.WARNING, "Couldn't set username.", e);
		}

	}

	private static String retrieveUsernameFromRegistry()
	{
		try
		{
			return WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "PlayerName");
		}
		catch (final RegistryException e)
		{
			Logging.logger.log(Level.WARNING, "Couldn't retrieve Username from registry.", e);
			return "";
		}
	}

	public static String getGtaPath()
	{
		try
		{
			return WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "gta_sa_exe").replace("gta_sa.exe", "");
		}
		catch (final RegistryException e)
		{
			Logging.logger.log(Level.WARNING, "Couldn't retrieve GTA path.", e);
			return null;
		}
	}

	public static String getInstalledVersion()
	{
		final File file = new File(GTA.getGtaPath() + "samp.dll");

		switch ((int) file.length())
		{
			case 2199552:
			{
				return "0.3.7";
			}
			case 1093632:
			{
				return "0.3z";
			}
			case 2084864:
			{
				return "0.3x";
			}
			case 1998848:
			{
				return "0.3e";
			}
			case 2015232:
			{
				return "0.3d";
			}
			case 1511424:
			{
				return "0.3c";
			}
			case 610304:
			{
				return "0.3a";
			}
		}

		return "Unknown Version";
	}

	private static boolean connectToServerUsingProtocol(final String ipAndPort)
	{
		try
		{
			final Desktop d = Desktop.getDesktop();
			d.browse(new URI("samp://" + ipAndPort));
			return true;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static void connectToServer(final String ipAndPort, final String password)
	{
		applyUsername();
		try
		{
			final ProcessBuilder builder = new ProcessBuilder(getGtaPath() + File.separator + "samp.exe ", ipAndPort, password);
			builder.directory(new File(getGtaPath()));
			builder.start();
		}
		catch (final Exception e)
		{
			if (StringUtils.isEmpty(password))
			{
				connectToServerUsingProtocol(ipAndPort);
			}
			else
			{
				Logging.logger.log(Level.WARNING, "Couldn't connect to server", e);
			}
		}
	}

	public static void connectToServer(final String ipAndPort)
	{
		connectToServer(ipAndPort, "");
	}
}
