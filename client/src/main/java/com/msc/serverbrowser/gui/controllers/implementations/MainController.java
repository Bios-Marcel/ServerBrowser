package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.util.logging.Level;

import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.Views;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController implements ViewController
{
	private static final String	MENUITEM_SELECTED_COLOR		= "-fx-background-color: #1F5FAE;";
	private static final String	MENUITEM_UNSELECTED_COLOR	= "-fx-background-color: #538ED7;";

	@FXML
	private Label headerTitle;

	@FXML
	private StackPane	menuItemFav;
	@FXML
	private StackPane	menuItemAll;
	@FXML
	private StackPane	menuItemUser;
	@FXML
	private StackPane	menuItemVersion;
	@FXML
	private StackPane	menuItemSettings;

	@FXML
	private StackPane	activeViewContainer;
	private Views		activeView;

	@Override
	public void initialize()
	{
		if (ClientProperties.getPropertyAsBoolean(Property.REMEMBER_LAST_VIEW))
		{
			loadView(Views.valueOf(ClientProperties.getPropertyAsInt(Property.LAST_VIEW)));
		}
		else
		{
			loadView(Views.valueOf(ClientProperties.getDefaultAsInt(Property.LAST_VIEW)));
		}
	}

	@FXML
	private void onServersFavMenuItemClicked()
	{
		loadView(Views.SERVERS_FAV);
	}

	@FXML
	private void onServersAllMenuItemClicked()
	{
		loadView(Views.SERVERS_ALL);
	}

	@FXML
	private void onUsernameMenuItemClicked()
	{
		loadView(Views.USERNAME_CHANGER);
	}

	@FXML
	private void onVersionMenuItemClicked()
	{
		loadView(Views.VERSION_CHANGER);
	}

	@FXML
	private void onSettingsMenuItemClicked()
	{
		loadView(Views.SETTINGS);
	}

	private void loadView(final Views view)
	{
		menuItemFav.setStyle(MENUITEM_UNSELECTED_COLOR);
		menuItemSettings.setStyle(MENUITEM_UNSELECTED_COLOR);
		menuItemUser.setStyle(MENUITEM_UNSELECTED_COLOR);
		menuItemAll.setStyle(MENUITEM_UNSELECTED_COLOR);
		menuItemVersion.setStyle(MENUITEM_UNSELECTED_COLOR);

		switch (view)
		{
			case VERSION_CHANGER:
			{
				menuItemVersion.setStyle(MENUITEM_SELECTED_COLOR);
				break;
			}
			case USERNAME_CHANGER:
			{
				menuItemUser.setStyle(MENUITEM_SELECTED_COLOR);
				break;
			}
			case SETTINGS:
			{
				menuItemSettings.setStyle(MENUITEM_SELECTED_COLOR);
				break;
			}
			case SERVERS_FAV:
			{
				menuItemFav.setStyle(MENUITEM_SELECTED_COLOR);
				break;
			}
			case SERVERS_ALL:
			{
				menuItemAll.setStyle(MENUITEM_SELECTED_COLOR);
				break;
			}
		}

		loadFXML(view);
		activeView = view;
	}

	private void loadFXML(final Views view)
	{
		try
		{
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(view.getFXMLPath()));
			loader.setController(view.getControllerType().newInstance());
			activeViewContainer.getChildren().clear();
			activeViewContainer.getChildren().add(loader.load());
			activeViewContainer.getStylesheets().setAll(view.getStylesheetPath());
			headerTitle.setText(view.getTitle());
		}
		catch (final IOException | InstantiationException | IllegalAccessException exception)
		{
			Logging.logger().log(Level.SEVERE, "Couldn't load view.", exception);
		}
	}

	@Override
	public void onClose()
	{
		ClientProperties.setProperty(Property.LAST_VIEW, activeView.getId());
	}
}
