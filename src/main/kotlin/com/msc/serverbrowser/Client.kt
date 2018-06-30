package com.msc.serverbrowser

import com.github.plushaze.traynotification.animations.Animations
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder
import com.msc.serverbrowser.constants.PathConstants
import com.msc.serverbrowser.data.properties.AutomaticUpdatesProperty
import com.msc.serverbrowser.data.properties.ChangelogEnabledProperty
import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.data.properties.LanguageProperty
import com.msc.serverbrowser.data.properties.MaximizedProperty
import com.msc.serverbrowser.data.properties.ShowChangelogProperty
import com.msc.serverbrowser.data.properties.UseDarkThemeProperty
import com.msc.serverbrowser.gui.UncaughtExceptionHandlerController
import com.msc.serverbrowser.gui.UncaughtExceptionHandlerView
import com.msc.serverbrowser.gui.View
import com.msc.serverbrowser.gui.controllers.implementations.MainController
import com.msc.serverbrowser.gui.controllers.implementations.SettingsController
import com.msc.serverbrowser.gui.views.MainView
import com.msc.serverbrowser.logging.Logging
import com.msc.serverbrowser.util.UpdateUtility
import com.msc.serverbrowser.util.basic.FileUtility
import com.msc.serverbrowser.util.windows.OSUtility
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.util.Duration
import java.awt.Desktop
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

/**
 * This is the main class of the client.
 *
 * @author Marcel
 * @since 02.07.2017
 */
class Client : Application() {
    var stage: Stage? = null
    private val mainController: MainController

    /**
     * This property that indicates if an update check / isDownload progress is ongoing.
     */
    val updateOngoingProperty: BooleanProperty

    /**
     * @return current [SettingsController] or null.
     */
    val settingsController: SettingsController?
        get() = mainController.settingsController

    init {
        updateOngoingProperty = SimpleBooleanProperty(false)
        mainController = MainController(this, MainView())
    }

    override fun start(primaryStage: Stage) {
        loadUI(primaryStage)
        primaryStage.titleProperty().bind(mainController.titleProperty)

        checkForUpdates()
    }

    private fun loadUIAndGetController(stage: Stage): MainController {
        val scene = Scene(mainController.mainView.rootPane)
        stage.scene = scene

        applyTheme(scene)
        return mainController
    }

    /**
     * Deletes the scenes current stylesheets and applies either the dark theme or the default
     * theme.
     */
    fun applyTheme(scene: Scene) {
        scene.stylesheets.clear()
        scene.stylesheets.add(PathConstants.STYLESHEET_PATH + "mainStyleGeneral.css")

        if (ClientPropertiesController.getProperty(UseDarkThemeProperty)) {
            scene.stylesheets.add(PathConstants.STYLESHEET_PATH + "mainStyleDark.css")
            scene.stylesheets.add("/styles/trayDark.css")
        } else {
            scene.stylesheets.add(PathConstants.STYLESHEET_PATH + "mainStyleLight.css")
            scene.stylesheets.add("/styles/defaultStyle.css")
        }
    }

    /**
     * Loads the main UI.
     *
     * @param primaryStage the stage to use for displaying the UI
     */
    private fun loadUI(primaryStage: Stage) {
        stage = primaryStage

        val controller = loadUIAndGetController(primaryStage)

        TrayNotificationBuilder.setDefaultOwner(stage)

        primaryStage.icons.add(APPLICATION_ICON)
        primaryStage.isResizable = true
        primaryStage.isMaximized = ClientPropertiesController.getProperty(MaximizedProperty)

        primaryStage.setOnCloseRequest {
            controller.onClose()
            ClientPropertiesController.setProperty(MaximizedProperty, primaryStage.isMaximized)
        }

        primaryStage.show()

        if (ClientPropertiesController.getProperty(ShowChangelogProperty) && ClientPropertiesController.getProperty(ChangelogEnabledProperty)) {

            // Since the changelog has been shown after this update, it shall not be shown again,
            // unless there is another update
            ClientPropertiesController.setProperty(ShowChangelogProperty, false)

            val trayNotification = TrayNotificationBuilder()
                    .type(NotificationTypeImplementations.INFORMATION)
                    .title(getString("updated"))
                    .message(getString("clickForChangelog"))
                    .animation(Animations.SLIDE).build()

            trayNotification.setOnMouseClicked {
                OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser/releases/latest")
                trayNotification.dismiss()
            }
            trayNotification.showAndWait()
        }
    }

    /**
     * Compares the local version number to the one lying on the server. If an update is available
     * the user will be asked if he wants to update.
     */
    fun checkForUpdates() {
        Logging.info("Checking for updates.")

        // Only update if not in development mode
        if (isDevelopmentModeActivated) {
            return
        }

        if (updateOngoingProperty.get()) {
            // If an update is ongoing already, then we won't start another.
            return
        }

        if (ClientPropertiesController.getProperty(AutomaticUpdatesProperty)) {
            mainController.progressProperty().set(0.0)
            mainController.setGlobalProgressText(getString("checkingForUpdates"))

            Thread {
                updateOngoingProperty.set(true)

                try {
                    if (UpdateUtility.isUpToDate) {
                        //If the client think that it isn't up to date, we won't download an update nor use an already downloaded one.
                        Logging.info("Client is up to date.")
                    } else {
                        Logging.info("An update is available.")
                        //If there is already a launcher update on this client, we apply that first.
                        if (File(PathConstants.SAMPEX_TEMP_JAR).exists()) {
                            finishUpdate()
                        } else {
                            Platform.runLater {
                                mainController.progressProperty().set(0.1)
                                mainController.setGlobalProgressText(getString("downloadingUpdate"))
                            }
                            Logging.info("Downloading update.")
                            downloadUpdate()
                            Logging.info("Download of the updated has been finished.")
                            Platform.runLater { displayUpdateNotification() }
                        }
                    }
                } catch (exception: IOException) {
                    Logging.warn("Couldn't check for newer version.", exception)
                    Platform.runLater { displayCantRetrieveUpdate() }
                }

                Platform.runLater {
                    mainController.setGlobalProgressText("")
                    mainController.progressProperty().set(0.0)
                }
                updateOngoingProperty.set(false)
            }.start()
        }
    }

    /**
     * Downloads the latest version and restarts the client.
     */
    private fun downloadUpdate() {
        try {
            val releaseOptional = UpdateUtility.release

            if (releaseOptional.isPresent) {
                val release = releaseOptional.get()
                val updateUrl = release.assets[0].browserDownloadUrl
                val url = URI(updateUrl)
                FileUtility.downloadFile(url.toURL(), PathConstants.SAMPEX_TEMP_JAR, mainController
                        .progressProperty(), release.assets[0].size.toInt().toDouble())
            }
        } catch (exception: IOException) {
            Logging.error("Couldn't retrieve update.", exception)
        } catch (exception: URISyntaxException) {
            Logging.error("Couldn't retrieve update.", exception)
        }

    }

    /**
     * Loads a specific view.
     *
     * @param view the view to be loaded
     */
    fun loadView(view: View) {
        mainController.loadView(view)
    }

    /**
     * Reloads the active view, if it is the given one.
     *
     * @param view the view to reload
     */
    fun reloadViewIfLoaded(view: View) {
        if (mainController.activeView == view) {
            mainController.reloadView()
        }
    }

    /**
     * Loads the [settings view][View.SETTINGS] and selects the [TextField] which
     * contains the SA-MP / GTA path.
     */
    fun selectSampPathTextField() {
        if (mainController.activeView != View.SETTINGS) {
            loadView(View.SETTINGS)
        }
        mainController.settingsController?.selectSampPathTextField()
    }

    private fun displayUpdateNotification() {
        val trayNotification = TrayNotificationBuilder().title(getString("updateInstalled"))
                .message(getString("clickToRestart"))
                .animation(Animations.SLIDE).build()

        trayNotification.setOnMouseClicked {
            trayNotification.dismiss()
            finishUpdate()
        }
        trayNotification.showAndWait()
    }

    private fun displayCantRetrieveUpdate() {
        val trayNotification = TrayNotificationBuilder().message(getString("couldntRetrieveUpdate"))
                .animation(Animations.POPUP)
                .type(NotificationTypeImplementations.ERROR).title(getString("updating")).build()

        trayNotification.setOnMouseClicked { _ ->
            OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser/releases/latest")
            trayNotification.dismiss()
        }

        trayNotification.showAndWait()
    }

    private fun finishUpdate() {
        try {
            Logging.info("Applying update")
            FileUtility.copyOverwrite(PathConstants.SAMPEX_TEMP_JAR, PathConstants.OWN_JAR.path)
            ClientPropertiesController.setProperty(ShowChangelogProperty, true)
            Files.delete(Paths.get(PathConstants.SAMPEX_TEMP_JAR))
            selfRestart()
        } catch (exception: IOException) {
            Logging.error("Failed to update.", exception)
            val notification = TrayNotificationBuilder().title(getString("applyingUpdate"))
                    .message(getString("couldntApplyUpdate"))
                    .type(NotificationTypeImplementations.ERROR).build()

            notification.setOnMouseClicked {
                try {
                    Desktop.getDesktop().open(File(PathConstants.SAMPEX_LOG))
                } catch (couldntOpenlogfile: IOException) {
                    Logging.warn("Error opening logfile.", couldntOpenlogfile)
                }
            }

        }

    }

    /**
     *
     *
     * TODO BROKEN WHEN STARTED WITH INSTALLER.
     *
     * Restarts the application.
     */
    private fun selfRestart() {
        if (!PathConstants.OWN_JAR.name.endsWith(".jar")) {
            // The application wasn't run with a jar file, but in an ide.
            return
        }

        val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
        val command = ArrayList<String>()

        command.add(javaBin)
        command.add("-jar")
        command.add(PathConstants.OWN_JAR.path)

        try {
            val builder = ProcessBuilder(command)
            builder.start()
            Platform.exit()
        } catch (exception: IOException) {
            Logging.error("Couldn't selfrestart.", exception)
        }

    }

    companion object {
        /**
         * Application icon that can be used everywhere where necessary.
         */
        val APPLICATION_ICON = Image(Client::class.java.getResourceAsStream(PathConstants.APP_ICON_PATH))
        /**
         * Name of the application, as displayed to people.
         */
        const val APPLICATION_NAME = "SA-MP Server Browser"

        /**
         * Default Dismiss-[Duration] that is used for TrayNotifications.
         */
        val DEFAULT_TRAY_DISMISS_TIME: Duration = Duration.seconds(10.0)

        /**
         * @return true if the development mode is activated, otherwise false
         */
        var isDevelopmentModeActivated: Boolean = false
            private set

        /**
         * ResourceBundle which contains all the localized strings.
         *
         * Using `lateinit`, since a method is used to avoid code duplication.
         */
        lateinit var languageResourceBundle: ResourceBundle

        init {
            createFolderStructure()
            applyCurrentLanguage()
        }

        fun applyCurrentLanguage() {
            val locale = Locale(ClientPropertiesController.getProperty(LanguageProperty))
            Locale.setDefault(locale)
            languageResourceBundle = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", locale)
        }

        /**
         * Programs entry point, it also does specific things when passed specific arguments.
         *
         * @param args evaluated by [.readApplicationArguments]
         * @throws IOException if there was an error while loading language files
         * @throws FileNotFoundException if language files don't exist
         */
        @JvmStatic
        fun main(args: Array<String>) {
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                Logging.error("Uncaught exception in thread: $thread", exception)
                Platform.runLater { UncaughtExceptionHandlerView(UncaughtExceptionHandlerController(), exception).show() }
            }

            readApplicationArguments(args)

            Application.launch(Client::class.java, *args)
        }

        /**
         * Creates files and folders that are necessary for the application to run properly and migrates
         * old xml data.
         */
        private fun createFolderStructure() {
            val sampexFolder = File(PathConstants.SAMPEX_PATH)
            sampexFolder.mkdirs()

            try {
                Files.copy(Client::class.java.getResourceAsStream("/com/msc/serverbrowser/tools/sampcmd.exe"), Paths
                        .get(PathConstants.SAMP_CMD), StandardCopyOption.REPLACE_EXISTING)
            } catch (exception: IOException) {
                Logging.warn("Error copying SAMP CMD to sampex folder.", exception)
            }

            val clientCacheFolder = File(PathConstants.CLIENT_CACHE)
            clientCacheFolder.mkdirs()
        }

        private fun readApplicationArguments(args: Array<String>) {
            isDevelopmentModeActivated = args.contains("-d")
        }

        /**
         * @param key they key to retrieve the value for
         * @return the value for the given key, using the [.languageBundle] resource bundle
         */
        fun getString(key: String): String {
            return try {
                languageResourceBundle.getString(key)
            } catch (e: MissingResourceException) {
                "Invalid key"
            }
        }
    }
}
