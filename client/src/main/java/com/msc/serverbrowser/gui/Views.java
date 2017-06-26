package com.msc.serverbrowser.gui;

import java.util.HashMap;
import java.util.Map;

import com.msc.serverbrowser.gui.controllers.implementations.ServerListAllController;
import com.msc.serverbrowser.gui.controllers.implementations.ServerListFavController;
import com.msc.serverbrowser.gui.controllers.implementations.SettingsController;
import com.msc.serverbrowser.gui.controllers.implementations.UsernameController;
import com.msc.serverbrowser.gui.controllers.implementations.VersionChangeController;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;

public enum Views
{
	SERVERS_FAV(1, "Servers | Favourites", ServerListFavController.class, "/com/msc/serverbrowser/views/ServerList.fxml", "/com/msc/serverbrowser/views/stylesheets/serverListStyle.css"),
	SERVERS_ALL(2, "Servers | All", ServerListAllController.class, "/com/msc/serverbrowser/views/ServerList.fxml", "/com/msc/serverbrowser/views/stylesheets/serverListStyle.css"),
	USERNAME_CHANGER(3, "Username Changer", UsernameController.class, "/com/msc/serverbrowser/views/Username.fxml", "/com/msc/serverbrowser/views/stylesheets/usernameStyle.css"),
	VERSION_CHANGER(4, "Version Changer", VersionChangeController.class, "/com/msc/serverbrowser/views/Version.fxml", "/com/msc/serverbrowser/views/stylesheets/versionStyle.css"),
	SETTINGS(5, "Settings", SettingsController.class, "/com/msc/serverbrowser/views/Settings.fxml", "/com/msc/serverbrowser/views/stylesheets/settingsStyle.css");

	private final int id;

	private final String title;

	private Class<? extends ViewController> necessaryController;

	private String	stylesheetPath;
	private String	fxmlPath;

	private final static Map<Integer, Views> idMapping = new HashMap<>();

	// Create Mapping in order to be able to find an enum value by simply providing its id.
	static
	{
		for (final Views view : Views.values())
		{
			Views.idMapping.put(view.getId(), view);
		}
	}

	private Views(final int id, final String title, final Class<? extends ViewController> necessaryController, final String fxmlPath,
			final String stylesheetPath)
	{
		this.id = id;
		this.title = title;
		this.necessaryController = necessaryController;
		this.stylesheetPath = stylesheetPath;
		this.fxmlPath = fxmlPath;
	}

	public int getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public Class<? extends ViewController> getControllerType()
	{
		return necessaryController;
	}

	public String getStylesheetPath()
	{
		return stylesheetPath;
	}

	public String getFXMLPath()
	{
		return fxmlPath;
	}

	public static Views valueOf(final int idToGet)
	{
		return Views.idMapping.get(idToGet);
	}
}
