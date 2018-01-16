package com.msc.serverbrowser.gui.views;

import com.msc.serverbrowser.Client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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

	private final TextArea chatLogTextArea;

	private final Button	clearLogsButton;
	private final Button	loadLogsButton;

	/**
	 * Initializes the whole view.
	 */
	public FilesView() {

		chatLogTextArea = new TextArea();
		chatLogTextArea.setEditable(false);

		clearLogsButton = new Button(Client.lang.getString("clear"));
		loadLogsButton = new Button(Client.lang.getString("reload"));

		final ButtonBar buttonBar = new ButtonBar();
		buttonBar.getButtons().addAll(loadLogsButton, clearLogsButton);

		final VBox chatLogsTabContent = new VBox(5.0, chatLogTextArea, buttonBar);
		VBox.setVgrow(chatLogTextArea, Priority.ALWAYS);

		chatLogsTab = new Tab(Client.lang.getString("chatlogs"), chatLogsTabContent);

		rootPane = new TabPane(chatLogsTab);
		rootPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
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
		chatLogTextArea.setText(content);
	}
}
