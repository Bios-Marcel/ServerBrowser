package com.msc.serverbrowser.gui.controllers.implementations;

import java.util.Properties;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.LegacySAMPSettings;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.Views;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

/**
 * Defines the behaviour of the settings view and manages setting bindings.
 *
 * @author Marcel
 */
public class SettingsController implements ViewController
{
	// General Settings
	@FXML
	private TextField	sampPathTextField;
	@FXML
	private CheckBox	notifyServerOnStartupCheckBox;
	@FXML
	private CheckBox	rememberLastViewCheckBox;

	// Appearance Settings
	@FXML
	private CheckBox darkThemeCheckBox;

	// Permission Settings
	@FXML
	private CheckBox	allowCloseSampCheckBox;
	@FXML
	private CheckBox	allowCloseGtaCheckBox;

	// Update Settings
	@FXML
	private CheckBox showChangelogCheckBox;

	// SA-MP Settings
	@FXML
	private Spinner<Integer>	fpsLimitSpinner;
	@FXML
	private Spinner<Integer>	pageSizeSpinner;
	@FXML
	private CheckBox			audioproxyCheckBox;
	@FXML
	private CheckBox			timestampsCheckBox;
	@FXML
	private CheckBox			multicoreCheckbox;
	@FXML
	private CheckBox			audioMsgOffCheckBox;

	// Connection Settings
	@FXML
	private CheckBox askForUsernameOnConnectCheckBox;

	@Override
	public void initialize()
	{
		// General Properties
		sampPathTextField.setText(ClientProperties.getPropertyAsString(Property.SAMP_PATH));
		sampPathTextField.textProperty().addListener(changed ->
		{
			final String newValue = sampPathTextField.getText();
			ClientProperties.setProperty(Property.SAMP_PATH, newValue.trim().isEmpty() ? null : newValue);
		});
		setupCheckBox(notifyServerOnStartupCheckBox, Property.NOTIFY_SERVER_ON_STARTUP);
		setupCheckBox(rememberLastViewCheckBox, Property.REMEMBER_LAST_VIEW);

		// Connection Properties
		// setupCheckBox(askForUsernameOnConnectCheckBox, Property.ASK_FOR_NAME_ON_CONNECT);

		// Appearance Properties
		setupCheckBox(darkThemeCheckBox, Property.USE_DARK_THEME);
		darkThemeCheckBox.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			final Boolean rememberLastViewOld = ClientProperties.getPropertyAsBoolean(Property.REMEMBER_LAST_VIEW);
			ClientProperties.setProperty(Property.REMEMBER_LAST_VIEW, true);
			ClientProperties.setProperty(Property.LAST_VIEW, Views.SETTINGS.getId());

			Client.getInstance().loadUI();

			ClientProperties.setProperty(Property.REMEMBER_LAST_VIEW, rememberLastViewOld);
		});

		// Permission Properties
		setupCheckBox(allowCloseSampCheckBox, Property.ALLOW_CLOSE_SAMP);
		setupCheckBox(allowCloseGtaCheckBox, Property.ALLOW_CLOSE_GTA);

		// Update Properties
		setupCheckBox(showChangelogCheckBox, Property.SHOW_CHANGELOG_AFTER_UPDATE);

		// SA-MP properties
		final Properties legacyProperties = LegacySAMPSettings.getLegacyProperties().orElse(new Properties());
		initLegacySettings(legacyProperties);

		fpsLimitSpinner.valueProperty().addListener(changed -> changeLegacyIntegerSetting(LegacySAMPSettings.FPS_LIMIT, fpsLimitSpinner));
		pageSizeSpinner.valueProperty().addListener(changed -> changeLegacyIntegerSetting(LegacySAMPSettings.PAGE_SIZE, pageSizeSpinner));

		multicoreCheckbox.setOnAction(action -> changeLegacyBooleanSetting(LegacySAMPSettings.MULTICORE, multicoreCheckbox));
		audioMsgOffCheckBox.setOnAction(action -> changeLegacyBooleanSetting(LegacySAMPSettings.AUDIO_MESSAGE_OFF, audioMsgOffCheckBox));
		audioproxyCheckBox.setOnAction(action -> changeLegacyBooleanSetting(LegacySAMPSettings.AUDIO_PROXY_OFF, audioproxyCheckBox));
		timestampsCheckBox.setOnAction(action -> changeLegacyBooleanSetting(LegacySAMPSettings.TIMESTAMP, timestampsCheckBox));

	}

	private void changeLegacyBooleanSetting(final String key, final CheckBox checkBox)
	{
		final Properties latestOrNewProperties = LegacySAMPSettings.getLegacyProperties().orElse(new Properties());
		latestOrNewProperties.put(key, checkBox.isSelected() ? "1" : "0");
		LegacySAMPSettings.save(latestOrNewProperties);
		initLegacySettings(latestOrNewProperties);
	}

	private void changeLegacyIntegerSetting(final String key, final Spinner<Integer> spinner)
	{
		final Properties latestOrNewProperties = LegacySAMPSettings.getLegacyProperties().orElse(new Properties());
		latestOrNewProperties.put(key, spinner.getValue().toString());
		LegacySAMPSettings.save(latestOrNewProperties);
		initLegacySettings(latestOrNewProperties);
	}

	private void initLegacySettings(final Properties legacyProperties)
	{
		final boolean multicore = legacyProperties.getProperty(LegacySAMPSettings.MULTICORE, "1").equals("1") ? true : false;
		final boolean audioMsgOff = legacyProperties.getProperty(LegacySAMPSettings.AUDIO_MESSAGE_OFF, "0").equals("1") ? true : false;
		final boolean audioProxyOff = legacyProperties.getProperty(LegacySAMPSettings.AUDIO_PROXY_OFF, "0").equals("1") ? true : false;
		final boolean timestamp = legacyProperties.getProperty(LegacySAMPSettings.TIMESTAMP, "0").equals("1") ? true : false;

		final int fpsLimit = Integer.parseInt(legacyProperties.getProperty(LegacySAMPSettings.FPS_LIMIT, "50"));
		fpsLimitSpinner.getValueFactory().setValue(fpsLimit);
		final int pageSize = Integer.parseInt(legacyProperties.getProperty(LegacySAMPSettings.PAGE_SIZE, "50"));
		pageSizeSpinner.getValueFactory().setValue(pageSize);

		multicoreCheckbox.setSelected(multicore);
		audioMsgOffCheckBox.setSelected(audioMsgOff);
		audioproxyCheckBox.setSelected(audioProxyOff);
		timestampsCheckBox.setSelected(timestamp);
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