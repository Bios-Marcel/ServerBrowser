package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.util.logging.Level;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

/**
 * Controller for the Main view, e.g. the view that contains the menu bar, the
 * header and the loaded
 * view (Settings, Servers ...).
 *
 * @author Marcel
 */
public class MainController implements ViewController
{
	@FXML
	private StackPane	menuItemFav;
	@FXML
	private StackPane	menuItemAll;
	@FXML
	private StackPane	menuItemUser;
	@FXML
	private StackPane	menuItemVersion;
	@FXML
	private StackPane	menuItemFiles;
	@FXML
	private StackPane	menuItemSettings;

	@FXML
	private ScrollPane	activeViewContainer;
	private View		activeView;

	@FXML
	private Label		globalProgressLabel;
	@FXML
	private ProgressBar	globalProgressBar;

	@Override
	public void initialize()
	{
		if (ClientPropertiesController.getPropertyAsBoolean(Property.SAVE_LAST_VIEW))
		{
			loadView(View.valueOf(ClientPropertiesController.getPropertyAsInt(Property.LAST_VIEW)));
		}
		else
		{
			loadView(View.valueOf(ClientPropertiesController.getDefaultAsInt(Property.LAST_VIEW)));
		}

	}

	public DoubleProperty progressProperty()
	{
		return globalProgressBar.progressProperty();
	}

	public void setGlobalProgressText(final String text)
	{
		globalProgressLabel.setText(text);
	}

	@FXML
	private void onServersFavMenuItemClicked()
	{
		loadView(View.SERVERS_FAV);
	}

	@FXML
	private void onServersAllMenuItemClicked()
	{
		loadView(View.SERVERS_ALL);
	}

	@FXML
	private void onUsernameMenuItemClicked()
	{
		loadView(View.USERNAME_CHANGER);
	}

	@FXML
	private void onVersionMenuItemClicked()
	{
		loadView(View.VERSION_CHANGER);
	}

	@FXML
	private void onFileMenuItemClicked()
	{
		loadView(View.FILES);
	}

	@FXML
	private void onSettingsMenuItemClicked()
	{
		loadView(View.SETTINGS);
	}

	private void loadView(final View view)
	{
		final String CLICKED_STYLE_CLASS = "clickedItem";

		menuItemFav.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemSettings.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemUser.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemAll.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemVersion.getStyleClass().remove(CLICKED_STYLE_CLASS);
		menuItemFiles.getStyleClass().remove(CLICKED_STYLE_CLASS);

		switch (view)
		{
			case SERVERS_FAV:
				menuItemFav.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			case SERVERS_ALL:
				menuItemAll.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			case USERNAME_CHANGER:
				menuItemUser.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			case VERSION_CHANGER:
				menuItemVersion.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			case FILES:
				menuItemFiles.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			case SETTINGS:
				menuItemSettings.getStyleClass().add(CLICKED_STYLE_CLASS);
				break;
			default:
				throw new IllegalArgumentException("This View hasn't been implemented or is invalid: " + view);
		}

		loadFXML(view);
		activeView = view;
	}

	private void loadFXML(final View view)
	{
		try
		{
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(view.getFXMLPath()));
			loader.setController(view.getControllerType().newInstance());
			final Parent toLoad = loader.load();
			toLoad.getStylesheets().setAll(view.getStylesheetPath());
			activeViewContainer.setContent(toLoad);
			Client.getInstance().setTitle(Client.APPLICATION_NAME + " - " + view.getTitle());
		}
		catch (final IOException | InstantiationException | IllegalAccessException exception)
		{
			Logging.log(Level.SEVERE, "Couldn't load view.", exception);
		}
	}

	/**
	 * @return the current view
	 */
	public View getActiveView()
	{
		return activeView;
	}

	/**
	 * Reloads the current view.
	 */
	public void reloadView()
	{
		loadView(activeView);
	}

	@Override
	public void onClose()
	{
		ClientPropertiesController.setProperty(Property.LAST_VIEW, activeView.getId());
		Platform.exit(); // Make sure that the application doesnt stay open for some reason
	}
}
