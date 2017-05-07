package gui;

import java.util.HashMap;
import java.util.Map;

import gui.controllers.implementations.ServerListAllController;
import gui.controllers.implementations.ServerListFavController;
import gui.controllers.implementations.UsernameController;
import gui.controllers.implementations.VersionChangeController;
import gui.controllers.interfaces.ViewController;

public enum Views
{
	SERVERS_FAV(1, "Servers | Favourites", ServerListFavController.class, "/views/ServerList.fxml", "/views/stylesheets/serverListStyle.css"),
	SERVERS_ALL(2, "Servers | All", ServerListAllController.class, "/views/ServerList.fxml", "/views/stylesheets/serverListStyle.css"),
	USERNAME_CHANGER(3, "Username Changer", UsernameController.class, "/views/Username.fxml", "/views/stylesheets/usernameStyle.css"),
	VERSION_CHANGER(4, "Version Changer", VersionChangeController.class, "/views/Version.fxml", "/views/stylesheets/versionStyle.css"),
	// Not set yet, since that view doesn't exist yet.
	SETTINGS(5, "Settings", null, null, null);

	private final int id;

	private final String title;

	private Class<? extends ViewController> necessaryController;

	private String	stylesheetPath;
	private String	fxmlPath;

	private final static Map<Integer, Views> idMapping = new HashMap<>();

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
