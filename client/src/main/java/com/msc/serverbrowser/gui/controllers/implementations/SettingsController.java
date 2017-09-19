package com.msc.serverbrowser.gui.controllers.implementations;

import java.util.Properties;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.LegacySettings;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.Views;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.util.StringUtil;

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
	@FXML
	private CheckBox			disableHeadMoveCheckBox;
	@FXML
	private CheckBox			imeCheckBox;
	@FXML
	private CheckBox			directModeCheckBox;
	@FXML
	private CheckBox			noNameTagStatusCheckBox;

	// TODO(MSC) Connection Settings
	// @FXML
	// private CheckBox askForUsernameOnConnectCheckBox;

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
		// setupCheckBox(askForUsernameOnConnectCheckBox,
		// Property.ASK_FOR_NAME_ON_CONNECT);

		// Appearance Properties
		setupCheckBox(darkThemeCheckBox, Property.USE_DARK_THEME);
		darkThemeCheckBox.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			final Boolean rememberLastViewOld = ClientProperties.getPropertyAsBoolean(Property.REMEMBER_LAST_VIEW);
			ClientProperties.setProperty(Property.REMEMBER_LAST_VIEW, true);
			ClientProperties.setProperty(Property.LAST_VIEW, Views.SETTINGS.getId());

			Client.getInstance().reloadUI();

			ClientProperties.setProperty(Property.REMEMBER_LAST_VIEW, rememberLastViewOld);
		});

		// Permission Properties
		setupCheckBox(allowCloseSampCheckBox, Property.ALLOW_CLOSE_SAMP);
		setupCheckBox(allowCloseGtaCheckBox, Property.ALLOW_CLOSE_GTA);

		// Update Properties
		setupCheckBox(showChangelogCheckBox, Property.CHANGELOG_ENABLED);

		// SA-MP properties
		final Properties legacyProperties = LegacySettings.getLegacyProperties().orElse(new Properties());
		initLegacySettings(legacyProperties);

		fpsLimitSpinner.valueProperty().addListener(changed -> changeLegacyIntegerSetting(LegacySettings.FPS_LIMIT, fpsLimitSpinner));
		pageSizeSpinner.valueProperty().addListener(changed -> changeLegacyIntegerSetting(LegacySettings.PAGE_SIZE, pageSizeSpinner));

		multicoreCheckbox.setOnAction(action -> changeLegacyBooleanSetting(LegacySettings.MULTICORE, multicoreCheckbox));
		audioMsgOffCheckBox.setOnAction(action -> changeLegacyBooleanSetting(LegacySettings.AUDIO_MESSAGE_OFF, audioMsgOffCheckBox));
		audioproxyCheckBox.setOnAction(action -> changeLegacyBooleanSetting(LegacySettings.AUDIO_PROXY_OFF, audioproxyCheckBox));
		timestampsCheckBox.setOnAction(action -> changeLegacyBooleanSetting(LegacySettings.TIMESTAMP, timestampsCheckBox));
		disableHeadMoveCheckBox.setOnAction(action -> changeLegacyBooleanSetting(LegacySettings.DISABLE_HEAD_MOVE, disableHeadMoveCheckBox));
		imeCheckBox.setOnAction(action -> changeLegacyBooleanSetting(LegacySettings.IME, imeCheckBox));
		directModeCheckBox.setOnAction(action -> changeLegacyBooleanSetting(LegacySettings.DIRECT_MODE, directModeCheckBox));
		noNameTagStatusCheckBox.setOnAction(action -> changeLegacyBooleanSetting(LegacySettings.NO_NAME_TAG_STATUS, noNameTagStatusCheckBox));
	}

	private void changeLegacyBooleanSetting(final String key, final CheckBox checkBox)
	{
		changeLegacyBooleanSetting(key, checkBox.isSelected());
	}

	private void changeLegacyIntegerSetting(final String key, final Spinner<Integer> spinner)
	{
		changeLegacyIntegerSetting(key, spinner.getValue());
	}

	private void changeLegacyBooleanSetting(final String key, final Boolean value)
	{
		final Properties latestOrNewProperties = LegacySettings.getLegacyProperties().orElse(new Properties());
		latestOrNewProperties.put(key, value ? "1" : "0");
		LegacySettings.save(latestOrNewProperties);
		initLegacySettings(latestOrNewProperties);
	}

	private void changeLegacyIntegerSetting(final String key, final Integer value)
	{
		final Properties latestOrNewProperties = LegacySettings.getLegacyProperties().orElse(new Properties());
		latestOrNewProperties.put(key, value.toString());
		LegacySettings.save(latestOrNewProperties);
		initLegacySettings(latestOrNewProperties);
	}

	private void initLegacySettings(final Properties legacyProperties)
	{
		final boolean multicore = legacyProperties.getProperty(LegacySettings.MULTICORE, LegacySettings.MULTICORE_DEFAULT).equals("1") ? true : false;
		final boolean audioMsgOff = legacyProperties.getProperty(LegacySettings.AUDIO_MESSAGE_OFF, LegacySettings.AUDIO_MESSAGE_OFF_DEFAULT).equals("1") ? true : false;
		final boolean audioProxyOff = legacyProperties.getProperty(LegacySettings.AUDIO_PROXY_OFF, LegacySettings.AUDIO_PROXY_OFF_DEFAULT).equals("1") ? true : false;
		final boolean timestamp = legacyProperties.getProperty(LegacySettings.TIMESTAMP, LegacySettings.TIMESTAMP_DEFAULT).equals("1") ? true : false;

		final boolean disableHeadMove = legacyProperties.getProperty(LegacySettings.DISABLE_HEAD_MOVE, LegacySettings.DISABLE_HEAD_MOVE_DEFAULT).equals("1") ? true : false;
		final boolean ime = legacyProperties.getProperty(LegacySettings.IME, LegacySettings.IME_DEFAULT).equals("1") ? true : false;
		final boolean noNameTagStatus = legacyProperties.getProperty(LegacySettings.NO_NAME_TAG_STATUS, LegacySettings.NO_NAME_TAG_STATUS_DEFAULT).equals("1") ? true
				: false;
		final boolean directMode = legacyProperties.getProperty(LegacySettings.DIRECT_MODE, LegacySettings.DIRECT_MODE_DEFAULT).equals("1") ? true : false;

		final int fpsLimit = Integer.parseInt(legacyProperties.getProperty(LegacySettings.FPS_LIMIT, LegacySettings.FPS_LIMIT_DEFAULT));
		fpsLimitSpinner.getValueFactory().setValue(fpsLimit);
		final int pageSize = Integer.parseInt(legacyProperties.getProperty(LegacySettings.PAGE_SIZE, LegacySettings.PAGE_SIZE_DEFAULT));
		pageSizeSpinner.getValueFactory().setValue(pageSize);

		multicoreCheckbox.setSelected(multicore);
		audioMsgOffCheckBox.setSelected(audioMsgOff);
		audioproxyCheckBox.setSelected(audioProxyOff);
		timestampsCheckBox.setSelected(timestamp);

		directModeCheckBox.setSelected(disableHeadMove);
		imeCheckBox.setSelected(ime);
		noNameTagStatusCheckBox.setSelected(noNameTagStatus);
		directModeCheckBox.setSelected(directMode);
	}

	/**
	 * Does a one way binding of a {@link CheckBox} to a {@link Property}. Initially sets the value
	 * of the {@link CheckBox} according to the {@link Property Properties} value. As soon as the
	 * {@link CheckBox} value changes, the {@link Property} value will also change.
	 *
	 * @param box
	 *            the {@link CheckBox} to be set up
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

	/**
	 * Restores all settings to default. Some settings like {@link Property#DEVELOPMENT} and
	 * {@link Property#SHOW_CHANGELOG} won't be reset, since the user can't change those anyways.
	 */
	@FXML
	private void restoreDefaults()
	{
		ClientProperties.restorePropertyToDefault(Property.ALLOW_CLOSE_GTA);
		ClientProperties.restorePropertyToDefault(Property.ALLOW_CLOSE_SAMP);
		ClientProperties.restorePropertyToDefault(Property.ASK_FOR_NAME_ON_CONNECT);
		ClientProperties.restorePropertyToDefault(Property.NOTIFY_SERVER_ON_STARTUP);
		ClientProperties.restorePropertyToDefault(Property.REMEMBER_LAST_VIEW);
		ClientProperties.restorePropertyToDefault(Property.USE_DARK_THEME);
		ClientProperties.restorePropertyToDefault(Property.CHANGELOG_ENABLED);
		ClientProperties.restorePropertyToDefault(Property.SAMP_PATH);

		// Legacy Settigs
		changeLegacyBooleanSetting(LegacySettings.AUDIO_MESSAGE_OFF, StringUtil.stringToBoolean(LegacySettings.AUDIO_MESSAGE_OFF_DEFAULT));
		changeLegacyBooleanSetting(LegacySettings.AUDIO_PROXY_OFF, StringUtil.stringToBoolean(LegacySettings.AUDIO_PROXY_OFF_DEFAULT));
		changeLegacyBooleanSetting(LegacySettings.DIRECT_MODE, StringUtil.stringToBoolean(LegacySettings.AUDIO_PROXY_OFF_DEFAULT));
		changeLegacyBooleanSetting(LegacySettings.DISABLE_HEAD_MOVE, StringUtil.stringToBoolean(LegacySettings.DISABLE_HEAD_MOVE_DEFAULT));
		changeLegacyIntegerSetting(LegacySettings.FPS_LIMIT, Integer.parseInt(LegacySettings.FPS_LIMIT_DEFAULT));
		changeLegacyIntegerSetting(LegacySettings.PAGE_SIZE, Integer.parseInt(LegacySettings.PAGE_SIZE_DEFAULT));
		changeLegacyBooleanSetting(LegacySettings.IME, StringUtil.stringToBoolean(LegacySettings.IME_DEFAULT));
		changeLegacyBooleanSetting(LegacySettings.MULTICORE, StringUtil.stringToBoolean(LegacySettings.MULTICORE_DEFAULT));
		changeLegacyBooleanSetting(LegacySettings.TIMESTAMP, StringUtil.stringToBoolean(LegacySettings.TIMESTAMP_DEFAULT));
	}

	@Override
	public void onClose()
	{
		// Do nothing
	}
}