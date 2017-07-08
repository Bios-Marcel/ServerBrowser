package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.util.logging.Level;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.Views;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

/**
 * Controller for the Main view, e.g. the view that contains the menu bar, the header and the loaded
 * view (Settings, Servers ...).
 *
 * @author Marcel
 */
public class MainController implements ViewController
{
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
	private ScrollPane	activeViewContainer;
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
		final String CLICKED_STYLE_CLASS = "clickedItem";

		menuItemFav.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemSettings.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemUser.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemAll.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemVersion.getStyleClass().remove(CLICKED_STYLE_CLASS);

		switch (view)
		{
			case VERSION_CHANGER:
			{
				menuItemVersion.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			}
			case USERNAME_CHANGER:
			{
				menuItemUser.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			}
			case SETTINGS:
			{
				menuItemSettings.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			}
			case SERVERS_FAV:
			{
				menuItemFav.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			}
			case SERVERS_ALL:
			{
				menuItemAll.getStyleClass().add(CLICKED_STYLE_CLASS);
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
			activeViewContainer.setContent(loader.load());
			activeViewContainer.getStylesheets().setAll(view.getStylesheetPath());
			Client.getInstance().getStage().setTitle(Client.APPLICATION_NAME + " - " + view.getTitle());
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
