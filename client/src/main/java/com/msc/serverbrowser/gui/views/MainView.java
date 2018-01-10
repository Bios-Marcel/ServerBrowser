package com.msc.serverbrowser.gui.views;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainView {
	private final HBox rootPane;
	
	public MainView() {
		rootPane = new HBox();
		rootPane.setPrefSize(480, 785);
		
		final VBox menuContainer = new VBox();
		
		final ScrollPane menuScrollPane = new ScrollPane(menuContainer);
		
		rootPane.getChildren().add(menuScrollPane);
	}
}
