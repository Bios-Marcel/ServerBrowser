package com.msc.serverbrowser.gui.views;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Class containing the component hierarchy for the main view.
 * <p>
 * It contains the menu bar, the active view and the bottom bar.
 * </p>
 *
 * @author Marcel
 * @since 10.01.2018
 */
public class MainView {
	private final HBox rootPane;

	private final ToggleButton	menuItemServers;
	private final ToggleButton	menuItemUser;
	private final ToggleButton	menuItemVersion;
	private final ToggleButton	menuItemFiles;
	private final ToggleButton	menuItemSettings;

	private final ScrollPane contentScrollPane;

	private final Hyperlink	githubLink;
	private final Hyperlink	helpLink;
	private final Hyperlink	donateLink;

	private final HBox bottomBarCustom;

	private final Label			globalProgressLabel;
	private final ProgressBar	globalProgressBar;

	/**
	 * Initializes the whole view.
	 */
	public MainView() {
		rootPane = new HBox();
		rootPane.setPrefSize(800, 500);
		rootPane.getStyleClass().add("root-pane");

		final VBox menuContainer = new VBox();
		menuContainer.getStyleClass().add("tabPane");

		final ToggleGroup menuItemToggleGroup = new ToggleGroup();
		final String menuItemStyleClass = "MenuItem";

		menuItemServers = new ToggleButton("\uf0c9");
		menuItemServers.getStyleClass().add(menuItemStyleClass);
		menuItemUser = new ToggleButton("\uf007");
		menuItemUser.getStyleClass().add(menuItemStyleClass);
		menuItemVersion = new ToggleButton("\uf0ed");
		menuItemVersion.getStyleClass().add(menuItemStyleClass);
		menuItemFiles = new ToggleButton("\uf07b");
		menuItemFiles.getStyleClass().add(menuItemStyleClass);
		menuItemSettings = new ToggleButton("\uf013");
		menuItemSettings.getStyleClass().add(menuItemStyleClass);

		menuItemToggleGroup.getToggles().addAll(menuItemServers, menuItemUser, menuItemVersion, menuItemFiles, menuItemSettings);
		menuContainer.getChildren().addAll(menuItemServers, menuItemUser, menuItemVersion, menuItemFiles, menuItemSettings);

		final ScrollPane menuScrollPane = new ScrollPane(menuContainer);
		menuScrollPane.setFitToHeight(true);
		menuScrollPane.setFitToWidth(true);
		menuScrollPane.getStyleClass().add("tabScrollPane");

		final VBox mainContentPane = new VBox();
		HBox.setHgrow(mainContentPane, Priority.ALWAYS);
		contentScrollPane = new ScrollPane();
		contentScrollPane.setFitToHeight(true);
		contentScrollPane.setFitToWidth(true);
		contentScrollPane.getStyleClass().add("viewContent");
		VBox.setVgrow(contentScrollPane, Priority.ALWAYS);

		final HBox bottomBar = new HBox();
		bottomBar.getStyleClass().add("bottom-bar");

		githubLink = new Hyperlink("\uf09b");
		githubLink.getStyleClass().add("info-icon");
		githubLink.setTooltip(new Tooltip(Client.getString("openGithubTooltip")));
		githubLink.setFocusTraversable(false);
		helpLink = new Hyperlink("\uf059");
		helpLink.getStyleClass().add("info-icon");
		helpLink.setTooltip(new Tooltip(Client.getString("openGithubWikiTooltip")));
		helpLink.setFocusTraversable(false);
		donateLink = new Hyperlink(Client.getString("donate") + " \uf0d6");
		donateLink.getStyleClass().add("donate-button");
		donateLink.setTooltip(new Tooltip(Client.getString("openDonationPageTooltip")));
		donateLink.setMaxHeight(Double.MAX_VALUE);
		donateLink.setFocusTraversable(false);

		bottomBarCustom = new HBox();
		bottomBarCustom.getStyleClass().add("bottom-bar-custom");
		HBox.setHgrow(bottomBarCustom, Priority.ALWAYS);

		final HBox progressBarContainer = new HBox();
		progressBarContainer.getStyleClass().add("global-progress-bar-container");
		globalProgressLabel = new Label();
		globalProgressBar = new ProgressBar(0.0);
		progressBarContainer.getChildren().addAll(globalProgressLabel, globalProgressBar);

		bottomBar.getChildren().addAll(githubLink, helpLink, donateLink, bottomBarCustom, progressBarContainer);

		mainContentPane.getChildren().add(contentScrollPane);
		mainContentPane.getChildren().add(bottomBar);

		rootPane.getChildren().add(menuScrollPane);
		rootPane.getChildren().add(mainContentPane);
	}

	/**
	 * Inserts the {@link Node} into the {@link #contentScrollPane}.
	 *
	 * @param node the {@link Node} to be inserted into the {@link #contentScrollPane}
	 */
	public void setActiveViewNode(final Node node) {
		contentScrollPane.setContent(node);
	}

	/**
	 * Adds {@link Node}s to the custom part of the BottomBar
	 *
	 * @param nodes the {@link Node}s to be added
	 */
	public void addToBottomBar(final Node... nodes) {
		bottomBarCustom.getChildren().addAll(nodes);
	}

	/**
	 * Removes all {@link Node}s that have been added to the {@link #bottomBarCustom}.
	 */
	public void removeNodesFromBottomBar() {
		bottomBarCustom.getChildren().clear();
	}

	/**
	 * Sets the text for the global {@link ProgressBar}.
	 *
	 * @param text the text to be set
	 */
	public void setGlobalProgressBarText(final String text) {
		globalProgressLabel.setText(text);
	}

	/**
	 * @return the {@link DoubleProperty ProgressProperty} for the {@link #globalProgressBar}
	 */
	public DoubleProperty globalProgressProperty() {
		return globalProgressBar.progressProperty();
	}

	/**
	 * Selects the proper menu item, depending on which {@link View} was given.
	 *
	 * @param view the {@link View} to select the menu item for
	 */
	public void selectMenuItemForView(final View view) {
		switch (view) {
			case SERVERS:
				menuItemServers.setSelected(true);
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
	}

	/**
	 * Sets the {@link EventHandler} to handle all {@link ActionEvent}s on the
	 * {@link #menuItemServers}.
	 *
	 * @param handler {@link EventHandler} to be set
	 */
	public void setMenuItemAllAction(final EventHandler<ActionEvent> handler) {
		menuItemServers.setOnAction(handler);
	}

	/**
	 * Sets the {@link EventHandler} to handle all {@link ActionEvent}s on the
	 * {@link #menuItemUser}.
	 *
	 * @param handler {@link EventHandler} to be set
	 */
	public void setMenuItemUsernameAction(final EventHandler<ActionEvent> handler) {
		menuItemUser.setOnAction(handler);
	}

	/**
	 * Sets the {@link EventHandler} to handle all {@link ActionEvent}s on the
	 * {@link #menuItemVersion}.
	 *
	 * @param handler {@link EventHandler} to be set
	 */
	public void setMenuItemVersionAction(final EventHandler<ActionEvent> handler) {
		menuItemVersion.setOnAction(handler);
	}

	/**
	 * Sets the {@link EventHandler} to handle all {@link ActionEvent}s on the
	 * {@link #menuItemFiles}.
	 *
	 * @param handler {@link EventHandler} to be set
	 */
	public void setMenuItemFilesAction(final EventHandler<ActionEvent> handler) {
		menuItemFiles.setOnAction(handler);
	}

	/**
	 * Sets the {@link EventHandler} to handle all {@link ActionEvent}s on the
	 * {@link #menuItemSettings}.
	 *
	 * @param handler {@link EventHandler} to be set
	 */
	public void setMenuItemSettingsAction(final EventHandler<ActionEvent> handler) {
		menuItemSettings.setOnAction(handler);
	}

	/**
	 * @return {@link #rootPane}
	 */
	public Parent getRootPane() {
		return rootPane;
	}

	/**
	 * Sets a browse action for the {@link #githubLink} using the given {@link String} as the URL.
	 *
	 * @param string the URL
	 */
	public void setGitHubHyperlink(final String string) {
		githubLink.setOnAction(__ -> OSUtility.browse(string));
	}

	/**
	 * Sets a browse action for the {@link #helpLink} using the given {@link String} as the URL.
	 *
	 * @param string the URL
	 */
	public void setHelpHyperlink(final String string) {
		helpLink.setOnAction(__ -> OSUtility.browse(string));
	}

	/**
	 * Sets a browse action for the {@link #donateLink} using the given {@link String} as the URL.
	 *
	 * @param string the URL
	 */
	public void setDonateHyperlink(final String string) {
		donateLink.setOnAction(__ -> OSUtility.browse(string));
	}
}
