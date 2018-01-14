package com.msc.serverbrowser.gui.controllers.interfaces;

/**
 * Interface that provides the minimal necessary methods for ViewController implementations.
 *
 * @author Marcel
 */
public interface ViewController {
	/**
	 * This method is called by the FXMLLoader after loading the FXML File.
	 */
	default void initialize() {
		// Do nothing by default
	}

	/**
	 * This method is called, as soon as the View closes.
	 */
	default void onClose() {
		// Do nothing by default
	}
}
