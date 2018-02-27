package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.gui.views.FilesView;
import com.msc.serverbrowser.gui.views.MainView;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.OptionalUtility;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;

/**
 * Main {@link ViewController} for this application.
 *
 * @author Marcel
 * @since 10.01.2018
 */
public class MainController implements ViewController {
	private View activeView;

	private final MainView mainView;

	private ViewController activeSubViewController;

	/**
	 * @param mainView
	 *            the view to be used by this controller
	 */
	public MainController(final MainView mainView) {
		this.mainView = mainView;
		Font.loadFont(MainController.class.getResource("/com/msc/serverbrowser/fonts/FontAwesome.otf").toExternalForm(), 12);
		configureMenuItems();
		registerBottomBarHyperlinks();
		if (Client.isDevelopmentModeActivated()) {
			registerDevShortcuts();
		}
	}

	private void registerDevShortcuts() {
		mainView.getRootPane().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			if (event.isControlDown() && event.getCode() == KeyCode.D) {
				final boolean currentValue = ClientPropertiesController.getPropertyAsBoolean(Property.USE_DARK_THEME);
				ClientPropertiesController.setProperty(Property.USE_DARK_THEME, !currentValue);
				Client.getInstance().applyTheme();
				Client.getInstance().reloadViewIfLoaded(getActiveView());
			}
		});
	}

	private void configureMenuItems() {
		mainView.setMenuItemAllAction(__ -> onServersMenuItemClicked());
		mainView.setMenuItemUsernameAction(__ -> onUsernameMenuItemClicked());
		mainView.setMenuItemVersionAction(__ -> onVersionMenuItemClicked());
		mainView.setMenuItemFilesAction(__ -> onFilesMenuItemClicked());
		mainView.setMenuItemSettingsAction(__ -> onSettingsMenuItemClicked());
	}

	@Override
	public void initialize() {
		if (ClientPropertiesController.getPropertyAsBoolean(Property.SAVE_LAST_VIEW)) {
			final View view = View.valueOf(ClientPropertiesController.getPropertyAsInt(Property.LAST_VIEW)).orElse(View.SERVERS);
			loadView(view);
		}
		else {
			loadView(View.valueOf(ClientPropertiesController.getDefaultAsInt(Property.LAST_VIEW)).get());
		}

	}

	private void registerBottomBarHyperlinks() {
		mainView.setGitHubHyperlink("https://github.com/Bios-Marcel/ServerBrowser");
		mainView.setHelpHyperlink("https://github.com/Bios-Marcel/ServerBrowser/wiki");
		mainView.setDonateHyperlink("https://github.com/Bios-Marcel/ServerBrowser#donate");
	}

	/**
	 * Adds nodes to the Clients bottom bar.
	 *
	 * @param nodes the node that will be added
	 */
	public void addItemsToBottomBar(final Node... nodes) {
		mainView.addToBottomBar(nodes);
	}

	/**
	 * @return the progress {@link DoubleProperty} of the {@link ProgressBar} which resides in the
	 *         {@link MainView}
	 */
	public DoubleProperty progressProperty() {
		return mainView.globalProgressProperty();
	}

	/**
	 * Sets the text infront of the global {@link ProgressBar} bar.
	 *
	 * @param text the text tht appears infront of the global {@link ProgressBar}
	 */
	public void setGlobalProgressText(final String text) {
		mainView.setGlobalProgressBarText(text);
	}

	private void onServersMenuItemClicked() {
		loadView(View.SERVERS);
	}

	private void onUsernameMenuItemClicked() {
		loadView(View.USERNAME_CHANGER);
	}

	private void onVersionMenuItemClicked() {
		loadView(View.VERSION_CHANGER);
	}

	private void onFilesMenuItemClicked() {
		loadView(View.FILES);
	}

	private void onSettingsMenuItemClicked() {
		loadView(View.SETTINGS);
	}

	/**
	 * Loads a specific view.
	 *
	 * @param view
	 *            the view to be loaded
	 */
	public void loadView(final View view) {

		if (Objects.nonNull(activeSubViewController)) {
			activeSubViewController.onClose();
		}

		mainView.removeNodesFromBottomBar();

		final Parent loadedNode;

		if (view == View.FILES) {
			loadedNode = loadFilesView();
		}
		else {
			loadedNode = loadFXML(view);
		}

		initViewData(view, loadedNode);
		activeView = view;
	}

	private Parent loadFilesView() {
		final FilesView filesView = new FilesView();
		activeSubViewController = new FilesController(filesView);
		return filesView.getRootPane();
	}

	private Parent loadFXML(final View view) {
		try {
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(view.getFXMLPath()));
			loader.setResources(Client.getLangaugeResourceBundle());

			// Creating a new instance of the specified controller, controllers never have
			// constructor arguments, therefore this is supposedly fine.
			activeSubViewController = view.getControllerType().newInstance();
			loader.setController(activeSubViewController);
			return loader.load();
		}
		catch (final IOException | InstantiationException | IllegalAccessException exception) {
			Logging.error("Couldn't load view.", exception);
		}

		return new Label("Error loading view.");
	}

	private void initViewData(final View view, final Parent loadedNode) {
		loadedNode.getStylesheets().setAll(view.getStylesheetPath());
		mainView.selectMenuItemForView(view);
		mainView.setActiveViewNode(loadedNode);
		Client.getInstance().setTitle(Client.APPLICATION_NAME + " - " + view.getTitle());
	}

	/**
	 * @return the current view
	 */
	public View getActiveView() {
		return activeView;
	}

	/**
	 * Reloads the current view.
	 */
	public void reloadView() {
		loadView(activeView);
	}

	/**
	 * Returns an {@link Optional} of the current {@link ViewController} and tries casting it into
	 * {@link SettingsController}.
	 *
	 * @return {@link Optional} of {@link #activeSubViewController} or empty
	 */
	public Optional<SettingsController> getSettingsController() {
		return OptionalUtility.cast(activeSubViewController);
	}

	@Override
	public void onClose() {
		ClientPropertiesController.setProperty(Property.LAST_VIEW, activeView.getId());
		Platform.exit(); // Make sure that the application doesnt stay open for some reason
	}
}