package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.util.logging.Level;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.gui.views.MainView;
import com.msc.serverbrowser.logging.Logging;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Font;

/**
 * Main {@link ViewController} for this application.
 *
 * @author Marcel
 * @since 10.01.2018
 */
public class MainController implements ViewController
{
	private View activeView;

	private final MainView mainView;

	/**
	 * @param mainView the main view to be used by this controller
	 */
	public MainController(final MainView mainView)
	{
		this.mainView = mainView;
		Font.loadFont(MainController.class.getResource("/com/msc/serverbrowser/fonts/FontAwesome.otf").toExternalForm(), 12);
		configureMenuItems();
		registerBottomBarHyperlinks();
	}

	private void configureMenuItems()
	{
		mainView.setMenuItemFavAction(__ -> onServersFavMenuItemClicked());
		mainView.setMenuItemAllAction(__ -> onServersAllMenuItemClicked());
		mainView.setMenuItemUsernameAction(__ -> onUsernameMenuItemClicked());
		mainView.setMenuItemVersionAction(__ -> onVersionMenuItemClicked());
		mainView.setMenuItemFilesAction(__ -> onFilesMenuItemClicked());
		mainView.setMenuItemSettingsAction(__ -> onSettingsMenuItemClicked());
	}

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

	private void registerBottomBarHyperlinks()
	{
		mainView.setGitHubHyperlink("https://github.com/Bios-Marcel/ServerBrowser");
		mainView.setHelpHyperlink("https://github.com/Bios-Marcel/ServerBrowser/wiki");
		mainView.setDonateHyperlink("https://github.com/Bios-Marcel/ServerBrowser#donate");
	}

	/**
	 * Adds nodes to the Clients bottom bar.
	 *
	 * @param nodes
	 *            the node that will be added
	 */
	public void addItemsToBottomBar(final Node... nodes)
	{
		mainView.addToBottomBar(nodes);
	}

	/**
	 * @return the progress {@link DoubleProperty} of the {@link #globalProgressBar}
	 */
	public DoubleProperty progressProperty()
	{
		return mainView.globalProgressProperty();
	}

	/**
	 * Sets the text infront of the global {@link ProgressBar} bar.
	 *
	 * @param text
	 *            the text tht appears infront of the global {@link ProgressBar}
	 */
	public void setGlobalProgressText(final String text)
	{
		mainView.setGlobalProgressBarText(text);
	}

	private void onServersFavMenuItemClicked()
	{
		loadView(View.SERVERS_FAV);
	}

	private void onServersAllMenuItemClicked()
	{
		loadView(View.SERVERS_ALL);
	}

	private void onUsernameMenuItemClicked()
	{
		loadView(View.USERNAME_CHANGER);
	}

	private void onVersionMenuItemClicked()
	{
		loadView(View.VERSION_CHANGER);
	}

	private void onFilesMenuItemClicked()
	{
		loadView(View.FILES);
	}

	private void onSettingsMenuItemClicked()
	{
		loadView(View.SETTINGS);
	}

	/**
	 * Loads a specific view.
	 *
	 * @param view
	 *            the view to be loaded
	 */
	public void loadView(final View view)
	{
		mainView.removeNodesFromBottomBar();
		mainView.selectMenuItemForView(view);
		loadFXML(view);
		activeView = view;
	}

	private void loadFXML(final View view)
	{
		try
		{
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(view.getFXMLPath()));
			loader.setResources(Client.lang);

			// Creating a new instance of the specified controller, controllers never have
			// constructor arguments, therefore this is supposedly fine.
			loader.setController(view.getControllerType().newInstance());
			final Parent toLoad = loader.load();
			toLoad.getStylesheets().setAll(view.getStylesheetPath());
			mainView.setActiveViewNode(toLoad);
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