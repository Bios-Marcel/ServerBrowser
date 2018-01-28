package com.msc.serverbrowser.gui.views;

import com.msc.serverbrowser.Client;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
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

	private final WebView chatLogTextArea;

	private final Button	clearLogsButton;
	private final Button	loadLogsButton;

	private final BooleanProperty	showTimesIfAvailableProperty	= new SimpleBooleanProperty(false);
	private final BooleanProperty	showColorsProperty				= new SimpleBooleanProperty(false);
	private final StringProperty	lineFilterProperty				= new SimpleStringProperty("");

	/**
	 * Initializes the whole view.
	 */
	public FilesView() {

		chatLogTextArea = new WebView();

		clearLogsButton = new Button(Client.getString("clear"));
		loadLogsButton = new Button(Client.getString("reload"));

		final ButtonBar buttonBar = new ButtonBar();
		buttonBar.getButtons().addAll(loadLogsButton, clearLogsButton);

		final CheckBox showTimesCheckBox = new CheckBox(Client.getString("showTimestamps"));
		showTimesIfAvailableProperty.bind(showTimesCheckBox.selectedProperty());
		showTimesCheckBox.setAlignment(Pos.CENTER);
		showTimesCheckBox.setMaxHeight(Double.MAX_VALUE);

		final CheckBox showColorsCheckBox = new CheckBox(Client.getString("showChatlogColors"));
		showColorsProperty.bind(showColorsCheckBox.selectedProperty());
		showColorsCheckBox.setAlignment(Pos.CENTER);
		showColorsCheckBox.setMaxHeight(Double.MAX_VALUE);

		final TextField filterTextField = new TextField();
		filterTextField.setPromptText(Client.getString("enterFilterValue"));
		lineFilterProperty.bind(filterTextField.textProperty());

		final HBox optionCheckBoxes = new HBox(5.0, showColorsCheckBox, showTimesCheckBox, filterTextField);

		final VBox chatLogsTabContent = new VBox(5.0, chatLogTextArea, optionCheckBoxes, buttonBar);
		VBox.setVgrow(chatLogTextArea, Priority.ALWAYS);

		final Tab chatLogsTab = new Tab(Client.getString("chatlogs"), chatLogsTabContent);

		rootPane = new TabPane(chatLogsTab, new Tab("Wtf"));
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
	 * @return {@link #lineFilterProperty}
	 */
	public StringProperty getLineFilterProperty() {
		return lineFilterProperty;
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
