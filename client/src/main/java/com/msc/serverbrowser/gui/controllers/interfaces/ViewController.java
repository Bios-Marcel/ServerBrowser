package com.msc.serverbrowser.gui.controllers.interfaces;

public interface ViewController
{
	/**
	 * This method is called by the FXMLLoaderafter loading the FXML File.
	 */
	void initialize();

	/**
	 * This method is called, as soon as the View closes.
	 */
	void onClose();
}
