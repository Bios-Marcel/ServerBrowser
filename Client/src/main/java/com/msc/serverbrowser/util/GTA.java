package com.msc.serverbrowser.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;
import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.PastUsernames;
import com.msc.serverbrowser.logging.Logging;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;

public class GTA
{
	private static StringProperty username = new SimpleStringProperty(retrieveUsernameFromRegistry());

	public static StringProperty usernameProperty()
	{
		return username;
	}

	/**
	 * Writes the actual username (from registry) into the past usernames list and sets the new name
	 */
	public static void applyUsername()
	{
		killSamp();
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

	public static Optional<String> getGtaPath()
	{
		try
		{
			return Optional.of(WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "gta_sa_exe").replace("gta_sa.exe", ""));
		}
		catch (final RegistryException e)
		{
			Logging.logger.log(Level.WARNING, "Couldn't retrieve GTA path.", e);
			return Optional.empty();
		}
	}

	public static Optional<String> getInstalledVersion()
	{
		String versionString = null;
		final Optional<String> path = getGtaPath();
		if (path.isPresent())
		{
			final File file = new File(path.get() + "samp.dll");

			/*
			 * Bad Practice, could potentionally cause an error if Kalcor decides to do a huge
			 * update someday :P
			 */
			switch ((int) file.length())
			{
				case 2199552:
					versionString = "0.3.7";
					break;
				case 1093632:
					versionString = "0.3z";
					break;
				case 2084864:
					versionString = "0.3x";
					break;
				case 1998848:
					versionString = "0.3e";
					break;
				case 2015232:
					versionString = "0.3d";
					break;
				case 1511424:
					versionString = "0.3c";
					break;
				case 610304:
					versionString = "0.3a";
					break;
			}
		}

		return Optional.ofNullable(versionString);
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

	public static void killSamp()
	{
		try
		{
			Runtime.getRuntime().exec("taskkill /F /IM samp.exe");
		}
		catch (final IOException exception)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't kill SAMP", exception);
		}
	}

	public static void connectToServer(final String ipAndPort, final String password)
	{
		applyUsername();
		final Optional<String> gtaPath = getGtaPath();
		if (gtaPath.isPresent())
		{
			try
			{
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
					Logging.logger.log(Level.WARNING, "Couldn't connect to server", exception);
				}
			}
		}
		else
		{
			final Alert alert = new Alert(AlertType.ERROR);
			Client.setAlertIcon(alert);
			alert.initOwner(Client.getInstance().getStage());
			alert.initModality(Modality.APPLICATION_MODAL);
			alert.setTitle("Connecting to server");
			alert.setHeaderText("GTA couldn't be located");
			alert.setContentText("It seems like your don't have GTA installed.");
			alert.showAndWait();
		}
	}

	public static void connectToServer(final String ipAndPort)
	{
		connectToServer(ipAndPort, "");
	}
}
