package com.msc.serverbrowser.gui.controllers.interfaces;

/**
 * Interface that provides the minimal necessary methods for ViewController implementations.
 *
 * @author Marcel
 */
public interface ViewController {
	/**
	 * This method is called by the FXMLLoaderafter loading the FXML File.
	 */
	void initialize();
	
	/**
	 * This method is called, as soon as the View closes.
	 */
	void onClose();
}
