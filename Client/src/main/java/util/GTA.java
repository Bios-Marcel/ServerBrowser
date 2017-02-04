package util;

import java.awt.Desktop;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.apache.commons.lang3.StringUtils;

import data.PastUsernames;
import javafx.beans.property.SimpleStringProperty;
import windows.WinRegistry;

public class GTA
{
	private static SimpleStringProperty username = new SimpleStringProperty(retrieveUsername());

	public static SimpleStringProperty getUsername()
	{
		return username;
	}

	public static void setUsername(String newName)
	{
		try
		{
			username.set(newName);
			WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\SAMP", "PlayerName", newName);
		}
		catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
			System.out.println("Error writing username.");
		}
	}

	public static String retrieveUsername()
	{
		try
		{
			return WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\SAMP", "PlayerName");
		}
		catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
		{
			return "";
		}
	}

	public static String getGtaPath()
	{
		try
		{
			return WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\SAMP", "gta_sa_exe").replace("gta_sa.exe", "");
		}
		catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static String getInstalledVersion()
	{
		File file = new File(GTA.getGtaPath() + "samp.dll");

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

	private static boolean connectToServer(String ipAndPort)
	{
		try
		{
			setUsername(getUsername().get());
			Desktop d = Desktop.getDesktop();
			d.browse(new URI("samp://" + ipAndPort));
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static void connectToServerPerShell(String ipAndPort, String password)
	{
		PastUsernames.addPastUsernames(GTA.getUsername().get());
		try
		{
			ProcessBuilder builder = new ProcessBuilder(getGtaPath() + File.separator + "samp.exe ", ipAndPort, password);
			builder.directory(new File(getGtaPath()));
			builder.start();
		}
		catch (Exception e)
		{
			if (StringUtils.isEmpty(password))
			{
				connectToServer(ipAndPort);
			}
			else
			{
				e.printStackTrace();
			}
		}
	}

	public static void connectToServerPerShell(String ipAndPort)
	{
		connectToServerPerShell(ipAndPort, "");
	}
}
