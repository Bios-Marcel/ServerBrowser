package com.msc.serverbrowser.gui.controllers.implementations;

import java.util.Optional;
import java.util.Properties;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.CacheController;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.LegacySettingsController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.util.basic.StringUtility;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

/**
 * Defines the behaviour of the settings view and manages setting bindings.
 *
 * @author Marcel
 */
public class SettingsController implements ViewController
{
	// Information
	@FXML private Label informationLabel;

	// General Settings
	@FXML private TextField	sampPathTextField;
	@FXML private CheckBox	saveLastViewCheckBox;

	// Appearance Settings
	@FXML private CheckBox darkThemeCheckBox;

	// Permission Settings
	@FXML private CheckBox	allowCloseSampCheckBox;
	@FXML private CheckBox	allowCloseGtaCheckBox;

	// SA-MP Settings
	@FXML private Spinner<Integer>	fpsLimitSpinner;
	@FXML private Spinner<Integer>	pageSizeSpinner;
	@FXML private CheckBox			audioproxyCheckBox;
	@FXML private CheckBox			timestampsCheckBox;
	@FXML private CheckBox			multicoreCheckbox;
	@FXML private CheckBox			audioMsgCheckBox;
	@FXML private CheckBox			headMoveCheckBox;
	@FXML private CheckBox			imeCheckBox;
	@FXML private CheckBox			directModeCheckBox;
	@FXML private CheckBox			nameTagStatusCheckBox;

	// TODO(MSC) Connection Settings
	// @FXML
	// private CheckBox askForUsernameOnConnectCheckBox;

	// Update Settings
	@FXML private CheckBox	showChangelogCheckBox;
	@FXML private CheckBox	enableAutomaticUpdatesCheckBox;

	// Downloads
	@FXML private CheckBox allowCachingDownloadsCheckBox;

	@Override
	public void initialize()
	{
		initInformationArea();

		// General Properties
		sampPathTextField.setText(ClientPropertiesController.getPropertyAsString(Property.SAMP_PATH));
		sampPathTextField.textProperty().addListener((__, ___, newValue) ->
		{
			ClientPropertiesController.setProperty(Property.SAMP_PATH, newValue.trim());
		});

		setupCheckBox(saveLastViewCheckBox, Property.SAVE_LAST_VIEW);

		// Connection Properties
		// setupCheckBox(askForUsernameOnConnectCheckBox,
		// Property.ASK_FOR_NAME_ON_CONNECT);

		// Appearance Properties
		setupCheckBox(darkThemeCheckBox, Property.USE_DARK_THEME);
		darkThemeCheckBox.selectedProperty().addListener((__, ___, newValue) ->
		{
			final Boolean saveLastViewOld = ClientPropertiesController.getPropertyAsBoolean(Property.SAVE_LAST_VIEW);
			ClientPropertiesController.setProperty(Property.SAVE_LAST_VIEW, true);
			ClientPropertiesController.setProperty(Property.LAST_VIEW, View.SETTINGS.getId());

			Client.getInstance().applyTheme();

			ClientPropertiesController.setProperty(Property.SAVE_LAST_VIEW, saveLastViewOld);
		});

		// Permission Properties
		setupCheckBox(allowCloseSampCheckBox, Property.ALLOW_CLOSE_SAMP);
		setupCheckBox(allowCloseGtaCheckBox, Property.ALLOW_CLOSE_GTA);

		// Update Properties
		setupCheckBox(showChangelogCheckBox, Property.CHANGELOG_ENABLED);
		setupCheckBox(enableAutomaticUpdatesCheckBox, Property.AUTOMTAIC_UPDATES);

		// Update Properties
		setupCheckBox(allowCachingDownloadsCheckBox, Property.ALLOW_CACHING_DOWNLOADS);

		// SA-MP properties
		final Properties legacyProperties = LegacySettingsController.getLegacyProperties().orElse(new Properties());
		initLegacySettings(legacyProperties);

		fpsLimitSpinner.valueProperty()
				.addListener(__ -> changeLegacyIntegerSetting(LegacySettingsController.FPS_LIMIT, fpsLimitSpinner.getValue()));
		pageSizeSpinner.valueProperty()
				.addListener(__ -> changeLegacyIntegerSetting(LegacySettingsController.PAGE_SIZE, pageSizeSpinner.getValue()));

		multicoreCheckbox
				.setOnAction(__ -> changeLegacyBooleanSetting(LegacySettingsController.MULTICORE, multicoreCheckbox.isSelected()));
		audioMsgCheckBox
				.setOnAction(__ -> changeLegacyBooleanSetting(LegacySettingsController.AUDIO_MESSAGE_OFF, !audioMsgCheckBox.isSelected()));
		audioproxyCheckBox
				.setOnAction(__ -> changeLegacyBooleanSetting(LegacySettingsController.AUDIO_PROXY_OFF, !audioproxyCheckBox.isSelected()));
		timestampsCheckBox
				.setOnAction(__ -> changeLegacyBooleanSetting(LegacySettingsController.TIMESTAMP, timestampsCheckBox.isSelected()));
		headMoveCheckBox
				.setOnAction(__ -> changeLegacyBooleanSetting(LegacySettingsController.HEAD_MOVE, !headMoveCheckBox.isSelected()));
		imeCheckBox
				.setOnAction(__ -> changeLegacyBooleanSetting(LegacySettingsController.IME, imeCheckBox.isSelected()));
		directModeCheckBox
				.setOnAction(__ -> changeLegacyBooleanSetting(LegacySettingsController.DIRECT_MODE, directModeCheckBox.isSelected()));
		nameTagStatusCheckBox
				.setOnAction(__ -> changeLegacyBooleanSetting(LegacySettingsController.NO_NAME_TAG_STATUS, !nameTagStatusCheckBox.isSelected()));
	}

	private void initInformationArea()
	{
		final StringBuilder builder = new StringBuilder(40);

		builder.append("SA-MP Server Browser")
				.append(System.lineSeparator())
				.append(System.lineSeparator())
				.append("Version: ")
				.append(ClientPropertiesController.getPropertyAsString(Property.LAST_TAG_NAME));

		informationLabel.setText(builder.toString());
	}

	private void changeLegacyBooleanSetting(final String key, final Boolean value)
	{
		final Properties latestOrNewProperties = LegacySettingsController.getLegacyProperties()
				.orElse(new Properties());
		latestOrNewProperties.put(key, value ? "1" : "0");
		LegacySettingsController.save(latestOrNewProperties);
		initLegacySettings(latestOrNewProperties);
	}

	private void changeLegacyIntegerSetting(final String key, final Integer value)
	{
		final Properties latestOrNewProperties = LegacySettingsController.getLegacyProperties()
				.orElse(new Properties());
		latestOrNewProperties.put(key, value.toString());
		LegacySettingsController.save(latestOrNewProperties);
		initLegacySettings(latestOrNewProperties);
	}

	private void initLegacySettings(final Properties legacyProperties)
	{
		final boolean multicore = StringUtility.stringToBoolean(legacyProperties
				.getProperty(LegacySettingsController.MULTICORE, LegacySettingsController.MULTICORE_DEFAULT));
		final boolean audioMsgOff = !StringUtility.stringToBoolean(legacyProperties
				.getProperty(LegacySettingsController.AUDIO_MESSAGE_OFF, LegacySettingsController.AUDIO_MESSAGE_OFF_DEFAULT));
		final boolean audioProxyOff = !StringUtility.stringToBoolean(legacyProperties
				.getProperty(LegacySettingsController.AUDIO_PROXY_OFF, LegacySettingsController.AUDIO_PROXY_OFF_DEFAULT));
		final boolean timestamp = StringUtility.stringToBoolean(legacyProperties
				.getProperty(LegacySettingsController.TIMESTAMP, LegacySettingsController.TIMESTAMP_DEFAULT));
		final boolean disableHeadMove = !StringUtility.stringToBoolean(legacyProperties
				.getProperty(LegacySettingsController.HEAD_MOVE, LegacySettingsController.DISABLE_HEAD_MOVE_DEFAULT));
		final boolean ime = StringUtility.stringToBoolean(legacyProperties
				.getProperty(LegacySettingsController.IME, LegacySettingsController.IME_DEFAULT));
		final boolean noNameTagStatus = !StringUtility.stringToBoolean(legacyProperties
				.getProperty(LegacySettingsController.NO_NAME_TAG_STATUS, LegacySettingsController.NO_NAME_TAG_STATUS_DEFAULT));
		final boolean directMode = StringUtility.stringToBoolean(legacyProperties
				.getProperty(LegacySettingsController.DIRECT_MODE, LegacySettingsController.DIRECT_MODE_DEFAULT));

		final int fpsLimit = Integer
				.parseInt(legacyProperties.getProperty(LegacySettingsController.FPS_LIMIT, LegacySettingsController.FPS_LIMIT_DEFAULT));
		fpsLimitSpinner.getValueFactory().setValue(fpsLimit);
		final int pageSize = Integer
				.parseInt(legacyProperties.getProperty(LegacySettingsController.PAGE_SIZE, LegacySettingsController.PAGE_SIZE_DEFAULT));
		pageSizeSpinner.getValueFactory().setValue(pageSize);

		multicoreCheckbox.setSelected(multicore);
		audioMsgCheckBox.setSelected(audioMsgOff);
		audioproxyCheckBox.setSelected(audioProxyOff);
		timestampsCheckBox.setSelected(timestamp);

		directModeCheckBox.setSelected(disableHeadMove);
		imeCheckBox.setSelected(ime);
		nameTagStatusCheckBox.setSelected(noNameTagStatus);
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
		box.selectedProperty().set(ClientPropertiesController.getPropertyAsBoolean(property));
		box.selectedProperty().addListener((__, ___, newValue) ->
		{
			ClientPropertiesController.setProperty(property, newValue);
		});
	}

	@FXML
	private void onClickManualUpdate()
	{
		Client.checkForUpdates();
	}

	@FXML
	private void onClickClearDownloadCache()
	{
		CacheController.clearVersionCache();
	}

	/**
	 * Restores all settings to default. Some settings like {@link Property#DEVELOPMENT} and
	 * {@link Property#SHOW_CHANGELOG} won't be reset, since the user can't change those anyways.
	 */
	@FXML
	private void onClickRestore()
	{
		final Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure, that you want to reset all you settings?", ButtonType.YES, ButtonType.NO);
		alert.setTitle("Reset settings");
		Client.insertAlertOwner(alert);

		final Optional<ButtonType> result = alert.showAndWait();

		result.ifPresent(button ->
		{
			if (button == ButtonType.YES)
			{
				restoreApplicationSettings();
				LegacySettingsController.restoreLegacySettings();

				// Reapply theme, since it might have been changed
				Client.getInstance().applyTheme();
				// Assure the view that the displays the correct data.
				Client.getInstance().reloadViewIfLoaded(View.SETTINGS);
			}
		});
	}

	private void restoreApplicationSettings()
	{
		ClientPropertiesController.restorePropertyToDefault(Property.ALLOW_CLOSE_GTA);
		ClientPropertiesController.restorePropertyToDefault(Property.ALLOW_CLOSE_SAMP);
		ClientPropertiesController.restorePropertyToDefault(Property.ASK_FOR_NAME_ON_CONNECT);
		ClientPropertiesController.restorePropertyToDefault(Property.SAVE_LAST_VIEW);
		ClientPropertiesController.restorePropertyToDefault(Property.USE_DARK_THEME);
		ClientPropertiesController.restorePropertyToDefault(Property.CHANGELOG_ENABLED);
		ClientPropertiesController.restorePropertyToDefault(Property.SAMP_PATH);
		ClientPropertiesController.restorePropertyToDefault(Property.ALLOW_CACHING_DOWNLOADS);
	}

	@Override
	public void onClose()
	{
		// Do nothing
	}
}