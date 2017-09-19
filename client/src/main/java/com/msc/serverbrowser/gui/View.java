package com.msc.serverbrowser.gui;

import java.util.HashMap;
import java.util.Map;

import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.gui.controllers.implementations.FilesController;
import com.msc.serverbrowser.gui.controllers.implementations.ServerListAllController;
import com.msc.serverbrowser.gui.controllers.implementations.ServerListFavController;
import com.msc.serverbrowser.gui.controllers.implementations.SettingsController;
import com.msc.serverbrowser.gui.controllers.implementations.UsernameController;
import com.msc.serverbrowser.gui.controllers.implementations.VersionChangeController;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;

/**
 * Holds all the important information related to every single view.
 *
 * @author Marcel
 */
@SuppressWarnings("javadoc")
public enum View
{
	SERVERS_FAV(1, "Servers | Favourites", ServerListFavController.class, PathConstants.VIEW_PATH + "ServerList.fxml", PathConstants.STYLESHEET_PATH + "serverListStyle.css"),
	SERVERS_ALL(2, "Servers | All", ServerListAllController.class, PathConstants.VIEW_PATH + "ServerList.fxml", PathConstants.STYLESHEET_PATH + "serverListStyle.css"),
	USERNAME_CHANGER(3, "Username Changer", UsernameController.class, PathConstants.VIEW_PATH + "Username.fxml", PathConstants.STYLESHEET_PATH + "usernameStyle.css"),
	VERSION_CHANGER(4, "Version Changer", VersionChangeController.class, PathConstants.VIEW_PATH + "Version.fxml", PathConstants.STYLESHEET_PATH + "versionStyle.css"),
	SETTINGS(5, "Settings", SettingsController.class, PathConstants.VIEW_PATH + "Settings.fxml", PathConstants.STYLESHEET_PATH + "settingsStyle.css"),
	FILES(6, "Files", FilesController.class, PathConstants.VIEW_PATH + "Files.fxml", PathConstants.STYLESHEET_PATH + "filesStyle.css");

	private final int id;

	private final String title;

	private Class<? extends ViewController> controllerType;

	private String stylesheetPath;

	private String fxmlPath;

	private final static Map<Integer, View> idMapping = new HashMap<>();

	// Create Mapping in order to be able to find an enum value by simply providing its id.
	static
	{
		for (final View view : View.values())
		{
			View.idMapping.put(view.getId(), view);
		}
	}

	private View(final int id, final String title, final Class<? extends ViewController> controllerType, final String fxmlPath,
			final String stylesheetPathCss)
	{
		this.id = id;
		this.title = title;
		this.controllerType = controllerType;
		this.stylesheetPath = stylesheetPathCss;
		this.fxmlPath = fxmlPath;
	}

	/**
	 * @return ID of the View
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return The Views title that will be used in the titleBar of the application
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @return the {@link ViewController} Type to be used for this View
	 */
	public Class<? extends ViewController> getControllerType()
	{
		return controllerType;
	}

	/**
	 * @return the path to the css stylesheet of this View
	 */
	public String getStylesheetPath()
	{
		return stylesheetPath;
	}

	/**
	 * @return the path to the FMXL file of this View
	 */
	public String getFXMLPath()
	{
		return fxmlPath;
	}

	/**
	 * Returns the Enum Value that has the given id.
	 *
	 * @param idToGet
	 *            id to check against
	 * @return the found Enum Value
	 */
	public static View valueOf(final int idToGet)
	{
		return View.idMapping.get(idToGet);
	}
}
