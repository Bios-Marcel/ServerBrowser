package com.msc.serverbrowser.gui.controllers.implementations

import com.github.plushaze.traynotification.animations.Animations
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder
import com.msc.serverbrowser.Client
import com.msc.serverbrowser.data.InstallationCandidateCache
import com.msc.serverbrowser.data.properties.AllowCachingDownloadsProperty
import com.msc.serverbrowser.data.properties.AllowCloseGtaProperty
import com.msc.serverbrowser.data.properties.AllowCloseSampProperty
import com.msc.serverbrowser.data.properties.AskForNameOnConnectProperty
import com.msc.serverbrowser.data.properties.AutomaticUpdatesProperty
import com.msc.serverbrowser.data.properties.ChangelogEnabledProperty
import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.data.properties.DownloadPreReleasesProperty
import com.msc.serverbrowser.data.properties.LanguageProperty
import com.msc.serverbrowser.data.properties.LastViewProperty
import com.msc.serverbrowser.data.properties.LegacySettingsController
import com.msc.serverbrowser.data.properties.Property
import com.msc.serverbrowser.data.properties.SampPathProperty
import com.msc.serverbrowser.data.properties.SaveLastViewProperty
import com.msc.serverbrowser.data.properties.UseDarkThemeProperty
import com.msc.serverbrowser.gui.View
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController
import com.msc.serverbrowser.util.Language
import com.msc.serverbrowser.util.UpdateUtility
import com.msc.serverbrowser.util.basic.MathUtility
import com.msc.serverbrowser.util.basic.StringUtility
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import java.text.MessageFormat
import java.util.Properties

/**
 * Defines the behaviour of the settings view and manages setting bindings.
 *
 * @author Marcel
 */
class SettingsController(val client: Client) : ViewController {
    // Information
    @FXML
    private lateinit var informationLabel: Label

    // General Settings
    @FXML
    private lateinit var sampPathTextField: TextField
    @FXML
    private lateinit var saveLastViewCheckBox: CheckBox
    @FXML
    private lateinit var languageComboBox: ComboBox<Language>

    // Appearance Settings
    @FXML
    private lateinit var darkThemeCheckBox: CheckBox

    // Permission Settings
    @FXML
    private lateinit var allowCloseSampCheckBox: CheckBox
    @FXML
    private lateinit var allowCloseGtaCheckBox: CheckBox

    // SA-MP Settings
    @FXML
    private lateinit var fpsLimitSpinner: Spinner<Int>
    @FXML
    private lateinit var pageSizeSpinner: Spinner<Int>
    @FXML
    private lateinit var audioproxyCheckBox: CheckBox
    @FXML
    private lateinit var timestampsCheckBox: CheckBox
    @FXML
    private lateinit var multicoreCheckbox: CheckBox
    @FXML
    private lateinit var audioMsgCheckBox: CheckBox
    @FXML
    private lateinit var headMoveCheckBox: CheckBox
    @FXML
    private lateinit var imeCheckBox: CheckBox
    @FXML
    private lateinit var directModeCheckBox: CheckBox
    @FXML
    private lateinit var nameTagStatusCheckBox: CheckBox

    // TODO(MSC) Connection Settings SERIOUSLY TODO TODO
    // @FXML
    // private CheckBox askForUsernameOnConnectCheckBox;

    // Update Settings
    @FXML
    private lateinit var showChangelogCheckBox: CheckBox
    @FXML
    private lateinit var enableAutomaticUpdatesCheckBox: CheckBox
    @FXML
    private lateinit var usePreReleasesCheckBox: CheckBox
    @FXML
    private lateinit var manualUpdateButton: Button

    // Downloads
    @FXML
    private lateinit var allowCachingDownloadsCheckBox: CheckBox

    override fun initialize() {
        initInformationArea()
        initPropertyComponents()
        configureSampLegacyPropertyComponents()
    }

    private fun initPropertyComponents() {
        // General Properties
        sampPathTextField.text = ClientPropertiesController.getProperty(SampPathProperty)
        sampPathTextField.textProperty().addListener { _, _, newValue -> ClientPropertiesController.setProperty(SampPathProperty, newValue.trim { it <= ' ' }) }

        setupCheckBox(saveLastViewCheckBox, SaveLastViewProperty)

        languageComboBox.items.addAll(*Language.values())
        val toSelectLanguage = Language.getByShortcut(ClientPropertiesController.getProperty(LanguageProperty)).get()
        languageComboBox.selectionModel.select(toSelectLanguage)
        languageComboBox.selectionModel.selectedItemProperty().addListener { _, _, newVal ->
            ClientPropertiesController.setProperty(LanguageProperty, newVal.shortcut)
//            Client.initLanguageFiles() TODO
            client.reloadViewIfLoaded(View.SETTINGS)
        }

        // Connection Properties
        // setupCheckBox(askForUsernameOnConnectCheckBox,
        // Property.ASK_FOR_NAME_ON_CONNECT);

        // Appearance Properties
        setupCheckBox(darkThemeCheckBox, UseDarkThemeProperty)
        darkThemeCheckBox.selectedProperty().addListener(InvalidationListener {
            val saveLastViewOld = ClientPropertiesController.getProperty(SaveLastViewProperty)
            ClientPropertiesController.setProperty(SaveLastViewProperty, true)
            ClientPropertiesController.setProperty(LastViewProperty, View.SETTINGS.id)

            client.applyTheme()

            ClientPropertiesController.setProperty(SaveLastViewProperty, saveLastViewOld)
        })

        // Permission Properties
        setupCheckBox(allowCloseSampCheckBox, AllowCloseSampProperty)
        setupCheckBox(allowCloseGtaCheckBox, AllowCloseGtaProperty)

        // Update Properties
        setupCheckBox(showChangelogCheckBox, ChangelogEnabledProperty)
        setupCheckBox(enableAutomaticUpdatesCheckBox, AutomaticUpdatesProperty)
        setupCheckBox(usePreReleasesCheckBox, DownloadPreReleasesProperty)

        // Update Properties
        setupCheckBox(allowCachingDownloadsCheckBox, AllowCachingDownloadsProperty)

        // Adding a listener to disable the update button in case an update is ongoing
        val updatingProperty = client.updateOngoingProperty
        updatingProperty.addListener { _, _, newVal -> manualUpdateButton.isDisable = newVal!! }
        manualUpdateButton.isDisable = updatingProperty.get()
    }

    private fun configureSampLegacyPropertyComponents() {
        val legacyProperties = LegacySettingsController.legacyProperties.orElse(Properties())
        initLegacySettings(legacyProperties)

        fpsLimitSpinner.valueProperty().addListener(InvalidationListener {
            val value = MathUtility.limitUpperAndLower(fpsLimitSpinner.value, 20, 90)
            changeLegacyIntegerSetting(LegacySettingsController.FPS_LIMIT, value)
        })
        pageSizeSpinner.valueProperty().addListener(InvalidationListener {
            val value = MathUtility.limitUpperAndLower(pageSizeSpinner.value, 10, 20)
            changeLegacyIntegerSetting(LegacySettingsController.PAGE_SIZE, value)
        })

        multicoreCheckbox.setOnAction { changeLegacyBooleanSetting(LegacySettingsController.MULTICORE, multicoreCheckbox.isSelected) }
        audioMsgCheckBox.setOnAction { changeLegacyBooleanSetting(LegacySettingsController.AUDIO_MESSAGE_OFF, !audioMsgCheckBox.isSelected) }
        audioproxyCheckBox.setOnAction { changeLegacyBooleanSetting(LegacySettingsController.AUDIO_PROXY_OFF, !audioproxyCheckBox.isSelected) }
        timestampsCheckBox.setOnAction { changeLegacyBooleanSetting(LegacySettingsController.TIMESTAMP, timestampsCheckBox.isSelected) }
        headMoveCheckBox.setOnAction { changeLegacyBooleanSetting(LegacySettingsController.HEAD_MOVE, !headMoveCheckBox.isSelected) }
        imeCheckBox.setOnAction { changeLegacyBooleanSetting(LegacySettingsController.IME, imeCheckBox.isSelected) }
        directModeCheckBox.setOnAction { changeLegacyBooleanSetting(LegacySettingsController.DIRECT_MODE, directModeCheckBox.isSelected) }
        nameTagStatusCheckBox.setOnAction { changeLegacyBooleanSetting(LegacySettingsController.NO_NAME_TAG_STATUS, !nameTagStatusCheckBox.isSelected) }
    }

    private fun initInformationArea() {
        val builder = "SA-MP Server Browser" +
                System.lineSeparator() +
                System.lineSeparator() +
                MessageFormat.format(Client.getString("versionInfo"), UpdateUtility.VERSION)

        informationLabel.text = builder
    }

    private fun changeLegacyBooleanSetting(key: String, value: Boolean) {
        val latestOrNewProperties = LegacySettingsController.legacyProperties.orElse(Properties())
        latestOrNewProperties[key] = if (value) "1" else "0"
        LegacySettingsController.save(latestOrNewProperties)
        initLegacySettings(latestOrNewProperties)
    }

    private fun changeLegacyIntegerSetting(key: String, value: Int) {
        val latestOrNewProperties = LegacySettingsController.legacyProperties.orElse(Properties())
        latestOrNewProperties[key] = value.toString()
        LegacySettingsController.save(latestOrNewProperties)
        initLegacySettings(latestOrNewProperties)
    }

    private fun initLegacySettings(legacyProperties: Properties) {
        val multicore = StringUtility
                .stringToBoolean(legacyProperties.getProperty(LegacySettingsController.MULTICORE, LegacySettingsController.MULTICORE_DEFAULT))
        val audioMsgOff = !StringUtility
                .stringToBoolean(legacyProperties.getProperty(LegacySettingsController.AUDIO_MESSAGE_OFF, LegacySettingsController.AUDIO_MESSAGE_OFF_DEFAULT))
        val audioProxyOff = !StringUtility
                .stringToBoolean(legacyProperties.getProperty(LegacySettingsController.AUDIO_PROXY_OFF, LegacySettingsController.AUDIO_PROXY_OFF_DEFAULT))
        val timestamp = StringUtility
                .stringToBoolean(legacyProperties.getProperty(LegacySettingsController.TIMESTAMP, LegacySettingsController.TIMESTAMP_DEFAULT))
        val disableHeadMove = !StringUtility
                .stringToBoolean(legacyProperties.getProperty(LegacySettingsController.HEAD_MOVE, LegacySettingsController.DISABLE_HEAD_MOVE_DEFAULT))
        val ime = StringUtility.stringToBoolean(legacyProperties.getProperty(LegacySettingsController.IME, LegacySettingsController.IME_DEFAULT))
        val noNameTagStatus = !StringUtility
                .stringToBoolean(legacyProperties
                        .getProperty(LegacySettingsController.NO_NAME_TAG_STATUS, LegacySettingsController.NO_NAME_TAG_STATUS_DEFAULT))
        val directMode = StringUtility
                .stringToBoolean(legacyProperties.getProperty(LegacySettingsController.DIRECT_MODE, LegacySettingsController.DIRECT_MODE_DEFAULT))

        val fpsLimit = Integer.parseInt(legacyProperties.getProperty(LegacySettingsController.FPS_LIMIT, LegacySettingsController.FPS_LIMIT_DEFAULT))
        fpsLimitSpinner.valueFactory.value = fpsLimit
        val pageSize = Integer.parseInt(legacyProperties.getProperty(LegacySettingsController.PAGE_SIZE, LegacySettingsController.PAGE_SIZE_DEFAULT))
        pageSizeSpinner.valueFactory.value = pageSize

        multicoreCheckbox.isSelected = multicore
        audioMsgCheckBox.isSelected = audioMsgOff
        audioproxyCheckBox.isSelected = audioProxyOff
        timestampsCheckBox.isSelected = timestamp

        directModeCheckBox.isSelected = disableHeadMove
        imeCheckBox.isSelected = ime
        nameTagStatusCheckBox.isSelected = noNameTagStatus
        directModeCheckBox.isSelected = directMode
    }

    /**
     * Does a one way binding of a [CheckBox] to a [Property]. Initially sets the value
     * of the [CheckBox] according to the [Properties][Property] value. As soon as the
     * [CheckBox] value changes, the [Property] value will also change.
     *
     * @param box the [CheckBox] to be set up
     * @param property the [Property] that will be bound to the [CheckBox]
     */
    private fun setupCheckBox(box: CheckBox, property: Property<Boolean>) {
        box.selectedProperty().set(ClientPropertiesController.getProperty(property))
        box.selectedProperty().addListener { _, _, newValue -> ClientPropertiesController.setProperty(property, newValue) }
    }

    @FXML
    private fun onClickManualUpdate() {
        client.checkForUpdates()
    }

    @FXML
    private fun onClickClearDownloadCache() {

        val cacheSuccessfullyCleared = InstallationCandidateCache.clearVersionCache()

        if (cacheSuccessfullyCleared) {
            TrayNotificationBuilder()
                    .type(NotificationTypeImplementations.SUCCESS)
                    .message("Cache has been successfully cleared.")
                    .title("Clearing cache")
                    .animation(Animations.POPUP)
                    .build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME)
        } else {
            TrayNotificationBuilder()
                    .type(NotificationTypeImplementations.ERROR)
                    .message("Couldn't clear cache.")
                    .animation(Animations.POPUP)
                    .title("Clearing cache")
                    .build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME)
        }
    }

    /**
     * Restores all settings to default. Some settings like [com.msc.serverbrowser.data.properties.ShowChangelogProperty]
     * won't be reset, since the user can't change those anyways.
     */
    @FXML
    private fun onClickRestore() {
        val alert = Alert(AlertType.CONFIRMATION, Client.getString("sureYouWantToRestoreSettings"), ButtonType.YES, ButtonType.NO)
        alert.title = Client.getString("restoreSettingsToDefault")
        alert.initOwner(getStage())

        val result = alert.showAndWait()

        result.ifPresent { button ->
            if (button == ButtonType.YES) {
                restoreApplicationSettings()
                restoreLegacySettings(Client.getString("sureYouWantToRestoreLegacySettingsAswell"))

                reloadSettingsView()
            }
        }
    }

    /**
     * Reapplies the current theme and reloads the settings view.
     */
    private fun reloadSettingsView() {
        // Reapply theme, since it might have been changed
        client.applyTheme()
        // Assure the view that the displays the correct data.
        client.reloadViewIfLoaded(View.SETTINGS)
    }

    private fun restoreApplicationSettings() {
        ClientPropertiesController.restorePropertyToDefault(AskForNameOnConnectProperty)
        ClientPropertiesController.restorePropertyToDefault(SampPathProperty)
        ClientPropertiesController.restorePropertyToDefault(LanguageProperty)
        ClientPropertiesController.restorePropertyToDefault(SaveLastViewProperty)
        ClientPropertiesController.restorePropertyToDefault(UseDarkThemeProperty)
        ClientPropertiesController.restorePropertyToDefault(AllowCloseGtaProperty)
        ClientPropertiesController.restorePropertyToDefault(AllowCloseSampProperty)
        ClientPropertiesController.restorePropertyToDefault(ChangelogEnabledProperty)
        ClientPropertiesController.restorePropertyToDefault(AutomaticUpdatesProperty)
        ClientPropertiesController.restorePropertyToDefault(AllowCachingDownloadsProperty)
        ClientPropertiesController.restorePropertyToDefault(DownloadPreReleasesProperty)
    }

    @FXML
    private fun restoreLegacySettings() {
        if (restoreLegacySettings(Client.getString("sureYouWantToRestoreLegacySettings"))) {
            reloadSettingsView()
        }
    }

    private fun restoreLegacySettings(alertTest: String): Boolean {
        val alert = Alert(AlertType.CONFIRMATION, alertTest, ButtonType.YES, ButtonType.NO)
        alert.title = Client.getString("restoreLegacySettingsToDefault")
        alert.initOwner(getStage())

        val result = alert.showAndWait()

        if (result.isPresent && result.get() == ButtonType.YES) {
            LegacySettingsController.restoreLegacySettings()
            return true
        }
        return false
    }

    private fun getStage() = informationLabel.scene.window

    /**
     * Selects the [TextField] which contains the SA-MP / GTA path.
     */
    fun selectSampPathTextField() {
        // HACK Not quite sure why Platform#runLater is necessary here.
        Platform.runLater { sampPathTextField.requestFocus() }
    }

    override fun onClose() {
        // Do nothing
    }
}