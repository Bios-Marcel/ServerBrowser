package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.util.logging.Level;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/**
 * Controller for the Main view, e.g. the view that contains the menu bar, the header and the loaded
 * view (Settings, Servers ...).
 *
 * @author Marcel
 */
public class MainController implements ViewController {
	@FXML
	private ToggleButton	menuItemFav;
	@FXML
	private ToggleButton	menuItemAll;
	@FXML
	private ToggleButton	menuItemUser;
	@FXML
	private ToggleButton	menuItemVersion;
	@FXML
	private ToggleButton	menuItemFiles;
	@FXML
	private ToggleButton	menuItemSettings;
	
	@FXML
	private ScrollPane	activeViewContainer;
	private View		activeView;
	
	@FXML
	private Hyperlink	hyperlinkGitHub;
	@FXML
	private Hyperlink	hyperlinkHelp;
	@FXML
	private Hyperlink	hyperlinkDonate;
	
	@FXML
	private HBox bottomBarCustom;
	
	@FXML
	private Label		globalProgressLabel;
	@FXML
	private ProgressBar	globalProgressBar;
	
	@FXML
	private ToggleGroup menuItemToggleGroup;

	@Override
	public void initialize() {
		Font.loadFont(MainController.class.getResource("/com/msc/serverbrowser/fonts/FontAwesome.otf").toExternalForm(), 12);

		setMenuItemIcons();
		
		hyperlinkGitHub.setText("\uf09b");
		hyperlinkHelp.setText("\uf059");
		// hyperlinkDonate.setText("\uf0d6");
		hyperlinkDonate.setText("Donate \uf0d6");
		hyperlinkGitHub.setTooltip(new Tooltip(Client.lang.getString("openGithubTooltip")));
		hyperlinkHelp.setTooltip(new Tooltip(Client.lang.getString("openGithubWikiTooltip")));
		hyperlinkDonate.setTooltip(new Tooltip(Client.lang.getString("openDonationPageTooltip")));
		
		if (ClientPropertiesController.getPropertyAsBoolean(Property.SAVE_LAST_VIEW)) {
			loadView(View.valueOf(ClientPropertiesController.getPropertyAsInt(Property.LAST_VIEW)));
		} else {
			loadView(View.valueOf(ClientPropertiesController.getDefaultAsInt(Property.LAST_VIEW)));
		}
		
	}
	
	private void setMenuItemIcons() {
		menuItemFav.setText("\uf005");
		menuItemAll.setText("\uf0c9");
		menuItemUser.setText("\uf007");
		menuItemVersion.setText("\uf0ed");
		menuItemFiles.setText("\uf07b");
		menuItemSettings.setText("\uf013");
	}
	
	@SuppressWarnings("static-method") // Can't be static because of FXML injection
	@FXML
	private void openGitHub() {
		OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser");
	}
	
	@SuppressWarnings("static-method") // Can't be static because of FXML injection
	@FXML
	private void openHelp() {
		OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser/wiki");
	}

	@SuppressWarnings("static-method") // Can't be static because of FXML injection
	@FXML
	private void openDonationPage() {
		OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser#donate");
	}
	
	/**
	 * Adds nodes to the Clients bottom bar.
	 *
	 * @param nodes
	 *            the node that will be added
	 */
	public void addItemsToBottomBar(final Node... nodes) {
		bottomBarCustom.getChildren().addAll(nodes);
	}
	
	/**
	 * @return the progress {@link DoubleProperty} of the {@link #globalProgressBar}
	 */
	public DoubleProperty progressProperty() {
		return globalProgressBar.progressProperty();
	}
	
	/**
	 * Sets the text infront of the global {@link ProgressBar} bar.
	 *
	 * @param text
	 *            the text tht appears infront of the global {@link ProgressBar}
	 */
	public void setGlobalProgressText(final String text) {
		globalProgressLabel.setText(text);
	}
	
	@FXML
	private void onServersFavMenuItemClicked() {
		loadView(View.SERVERS_FAV);
	}
	
	@FXML
	private void onServersAllMenuItemClicked() {
		loadView(View.SERVERS_ALL);
	}
	
	@FXML
	private void onUsernameMenuItemClicked() {
		loadView(View.USERNAME_CHANGER);
	}
	
	@FXML
	private void onVersionMenuItemClicked() {
		loadView(View.VERSION_CHANGER);
	}
	
	@FXML
	private void onFileMenuItemClicked() {
		loadView(View.FILES);
	}
	
	@FXML
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
		bottomBarCustom.getChildren().clear();
		
		switch (view) {
			case SERVERS_FAV:
				menuItemFav.setSelected(true);
				break;
			case SERVERS_ALL:
				menuItemAll.setSelected(true);
				break;
			case USERNAME_CHANGER:
				menuItemUser.setSelected(true);
				break;
			case VERSION_CHANGER:
				menuItemVersion.setSelected(true);
				break;
			case FILES:
				menuItemFiles.setSelected(true);
				break;
			case SETTINGS:
				menuItemSettings.setSelected(true);
				break;
			default:
				throw new IllegalArgumentException("This View hasn't been implemented or is invalid: " + view);
		}
		
		loadFXML(view);
		activeView = view;
	}
	
	private void loadFXML(final View view) {
		try {
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(view.getFXMLPath()));
			loader.setResources(Client.lang);
			
			// Creating a new instance of the specified controller, controllers never have
			// constructor arguments, therefore this is supposedly fine.
			loader.setController(view.getControllerType().newInstance());
			final Parent toLoad = loader.load();
			toLoad.getStylesheets().setAll(view.getStylesheetPath());
			activeViewContainer.setContent(toLoad);
			Client.getInstance().setTitle(Client.APPLICATION_NAME + " - " + view.getTitle());
		} catch (final IOException | InstantiationException | IllegalAccessException exception) {
			Logging.log(Level.SEVERE, "Couldn't load view.", exception);
		}
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
	
	@Override
	public void onClose() {
		ClientPropertiesController.setProperty(Property.LAST_VIEW, activeView.getId());
		Platform.exit(); // Make sure that the application doesnt stay open for some reason
	}
}
