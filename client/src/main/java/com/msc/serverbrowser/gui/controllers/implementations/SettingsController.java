package com.msc.serverbrowser.gui.controllers.implementations;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.Views;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

/**
 * Defines the behaviour of the settings view and manages setting bindings.
 *
 * @author Marcel
 */
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
	private CheckBox	darkThemeCheckBox;
	@FXML
	private CheckBox	allowCloseSampCheckBox;
	@FXML
	private CheckBox	allowCloseGtaCheckBox;

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
		setupCheckBox(darkThemeCheckBox, Property.USE_DARK_THEME);
		darkThemeCheckBox.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			final Boolean rememberLastViewOld = ClientProperties.getPropertyAsBoolean(Property.REMEMBER_LAST_VIEW);
			ClientProperties.setProperty(Property.REMEMBER_LAST_VIEW, true);
			ClientProperties.setProperty(Property.LAST_VIEW, Views.SETTINGS.getId());

			Client.getInstance().loadUI();

			ClientProperties.setProperty(Property.REMEMBER_LAST_VIEW, rememberLastViewOld);
		});
		setupCheckBox(allowCloseSampCheckBox, Property.ALLOW_CLOSE_SAMP);
		setupCheckBox(allowCloseGtaCheckBox, Property.ALLOW_CLOSE_GTA);
	}

	/**
	 * Does a one way bidning of a {@link CheckBox} to a {@link Property}. Initially sets the value
	 * of the {@link CheckBox} acording to the {@link Property Properties} value. As soon as the
	 * {@link CheckBox} value changes, the {@link Property} value will also change.
	 *
	 * @param box
	 *            th {@link CheckBox}
	 * @param property
	 *            the {@link Property} that will be bound to the {@link CheckBox}
	 */
	private static void setupCheckBox(final CheckBox box, final Property property)
	{
		box.selectedProperty().set(ClientProperties.getPropertyAsBoolean(property));
		box.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			ClientProperties.setProperty(property, newValue);
		});
	}

	@Override
	public void onClose()
	{
		// Do nothing
	}
}