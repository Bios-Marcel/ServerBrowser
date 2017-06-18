package com.msc.serverbrowser.gui.controllers.implementations;

import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

public class SettingsController implements ViewController
{
	@FXML
	private TitledPane	generalPane;
	@FXML
	private TextField	sampPathTextField;
	@FXML
	private CheckBox	notifyServerOnStartupCheckBox;
	@FXML
	private CheckBox	rememberLastViewCheckBox;

	@FXML
	private TitledPane	connectionPane;
	@FXML
	private CheckBox	askForUsernameOnConnectCheckBox;

	@Override
	public void initialize()
	{
		sampPathTextField.setText(ClientProperties.getPropertyAsString(Property.SAMP_PATH));
		sampPathTextField.textProperty().addListener(changed ->
		{
			final String newValue = sampPathTextField.getText();
			ClientProperties.setProperty(Property.SAMP_PATH, newValue.trim().isEmpty() ? null : newValue);
		});

		setupCheckBox(notifyServerOnStartupCheckBox, Property.NOTIFY_SERVER_ON_STARTUP);
		setupCheckBox(rememberLastViewCheckBox, Property.REMEMBER_LAST_VIEW);
		setupCheckBox(askForUsernameOnConnectCheckBox, Property.ASK_FOR_NAME_ON_CONNECT);
	}

	private static void setupCheckBox(final CheckBox box, final Property property)
	{
		box.selectedProperty().set(ClientProperties.getPropertyAsBoolean(property));
		box.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			ClientProperties.setProperty(property, newValue);
		});
	}
}
