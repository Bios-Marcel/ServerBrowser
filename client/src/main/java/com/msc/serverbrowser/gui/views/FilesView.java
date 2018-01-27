package com.msc.serverbrowser.gui.views;

import com.msc.serverbrowser.Client;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

/**
 * View for interacting with SA-MP files.
 * <p>
 * Contains:
 * <ul>
 * <li>chatlog viewer</li>
 * </ul>
 * </p>
 *
 * @author Marcel
 * @since 14.01.2018
 */
public class FilesView {
	private final TabPane rootPane;

	private final Tab chatLogsTab;

	private final WebView chatLogTextArea;

	private final Button	clearLogsButton;
	private final Button	loadLogsButton;

	private final BooleanProperty showTimesIfAvailableProperty = new SimpleBooleanProperty(false);

	private final BooleanProperty showColorsProperty = new SimpleBooleanProperty(false);

	/**
	 * Initializes the whole view.
	 */
	public FilesView() {

		chatLogTextArea = new WebView();

		clearLogsButton = new Button(Client.getString("clear"));
		loadLogsButton = new Button(Client.getString("reload"));

		final ButtonBar buttonBar = new ButtonBar();
		buttonBar.getButtons().addAll(loadLogsButton, clearLogsButton);

		// TODO Localize
		final CheckBox showTimesCheckBox = new CheckBox("Show times if available");
		showTimesIfAvailableProperty.bind(showTimesCheckBox.selectedProperty());
		final CheckBox showColorsCheckBox = new CheckBox("Show colors");
		showColorsProperty.bind(showColorsCheckBox.selectedProperty());

		final HBox optionCheckBoxes = new HBox(5.0, showColorsCheckBox, showTimesCheckBox);

		final VBox chatLogsTabContent = new VBox(5.0, chatLogTextArea, optionCheckBoxes, buttonBar);
		VBox.setVgrow(chatLogTextArea, Priority.ALWAYS);

		chatLogsTab = new Tab(Client.getString("chatlogs"), chatLogsTabContent);

		rootPane = new TabPane(chatLogsTab);
		rootPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
	}

	/**
	 * @return {@link #showTimesIfAvailableProperty}
	 */
	public BooleanProperty getShowTimesIfAvailableProperty() {
		return showTimesIfAvailableProperty;
	}

	/**
	 * @return {@link #showColorsProperty}
	 */
	public BooleanProperty getShowColorsProperty() {
		return showColorsProperty;
	}

	/**
	 * @return {@link #rootPane}
	 */
	public TabPane getRootPane() {
		return rootPane;
	}

	/**
	 * @param eventHandler the {@link ActionEvent} handler to be set
	 */
	public void setClearChatLogsButtonAction(final EventHandler<ActionEvent> eventHandler) {
		clearLogsButton.setOnAction(eventHandler);
	}

	/**
	 * @param eventHandler the {@link ActionEvent} handler to be set
	 */
	public void setLoadChatLogsButtonAction(final EventHandler<ActionEvent> eventHandler) {
		loadLogsButton.setOnAction(eventHandler);
	}

	/**
	 * Sets the text inside of the {@link #chatLogTextArea}.
	 *
	 * @param content the content to be set
	 */
	public void setChatLogTextAreaContent(final String content) {
		chatLogTextArea.getEngine().loadContent(content, "text/html");
	}
}
