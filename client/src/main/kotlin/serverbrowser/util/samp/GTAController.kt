package serverbrowser.util.samp

import com.github.plushaze.traynotification.animations.Animations
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder
import com.github.sarxos.winreg.HKey
import com.github.sarxos.winreg.RegistryException
import com.github.sarxos.winreg.WindowsRegistry
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.control.TextInputDialog
import serverbrowser.Client
import serverbrowser.data.PastUsernames
import serverbrowser.data.insallationcandidates.InstallationCandidate
import serverbrowser.data.properties.ClientPropertiesController
import serverbrowser.data.properties.SampPathProperty
import serverbrowser.gui.View
import serverbrowser.gui.controllers.implementations.VersionChangeController
import serverbrowser.logging.Logging
import serverbrowser.util.basic.HashingUtility
import serverbrowser.util.basic.StringUtility
import serverbrowser.util.windows.OSUtility
import java.io.File
import java.io.IOException
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Contains utility methods for interacting with native samp stuff.
 *
 * @author Marcel
 */
object GTAController {
    /**
     * Holds the users username.
     */
    val usernameProperty: StringProperty = SimpleStringProperty(retrieveUsernameFromRegistry().orElse(""))

    /**
     * Returns the GTA path.
     *
     * @return [Optional] of GTA path or an empty [Optional] if GTA couldn't be found
     */
    val gtaPath: Optional<String>
        get() {
            if (!OSUtility.isWindows) {
                return Optional.empty()
            }

            val path = gtaPathFromRegistry
            if (path.isPresent) {
                return path
            }

            val property = ClientPropertiesController.getProperty(SampPathProperty)
            return if (Objects.isNull(property) || property.isEmpty()) {
                Optional.empty()
            } else Optional.of(if (property.endsWith(File.separator)) property else property + File.separator)

        }

    /**
     * Should only be used if necessary.
     *
     * @return String of the GTA Path or null.
     */
    private val gtaPathFromRegistry: Optional<String>
        get() {
            try {
                return Optional.ofNullable(WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "gta_sa_exe").replace("gta_sa.exe", ""))
            } catch (exception: RegistryException) {
                Logging.warn("Couldn't retrieve GTA path.", exception)
                return Optional.empty()
            }

        }

    /**
     * Returns the [InstallationCandidate] value that represents the currently installed samp
     * version.
     *
     * @return [Optional] of installed versions version number or an [Optional.empty]
     */
    // GTA couldn't be found
    // samp.dll doesn't exist, even though GTA is installed at this point.
    val installedVersion: Optional<InstallationCandidate>
        get() {
            val path = gtaPath
            if (!path.isPresent) {
                return Optional.empty()
            }

            val file = File(path.get() + "samp.dll")
            if (!file.exists()) {
                return Optional.empty()
            }

            try {
                val hashsum = HashingUtility.generateChecksum(file.toString())
                return VersionChangeController.INSTALLATION_CANDIDATES.stream()
                        .filter { candidate -> candidate.sampDllChecksum.equals(hashsum, ignoreCase = true) }
                        .findFirst()
            } catch (exception: NoSuchAlgorithmException) {
                Logging.error("Error hashing installed samp.dll", exception)
            } catch (exception: IOException) {
                Logging.error("Error hashing installed samp.dll", exception)
            }

            return Optional.empty()
        }

    /**
     * Writes the actual username (from registry) into the past usernames list and sets the new name
     */
    fun applyUsername() {
        if (!OSUtility.isWindows) {
            // TODO Localize
            val alert = Alert(AlertType.ERROR, "The feature you are trying to use is not available on your operating system.", ButtonType.OK)
            Client.insertAlertOwner(alert)
            alert.title = "Apply username"
            alert.showAndWait()
            return
        }

        killSAMP()
        retrieveUsernameFromRegistry()
                .filter { name -> name != usernameProperty.get() }
                .ifPresent({ PastUsernames.addPastUsername(it) })

        try {
            WindowsRegistry.getInstance().writeStringValue(HKey.HKCU, "SOFTWARE\\SAMP", "PlayerName", usernameProperty.get())
        } catch (exception: RegistryException) {
            Logging.warn("Couldn't set username.", exception)
        }

    }

    // TODO Think of a better solution
    /**
     * Returns the Username that samp has set in the registry.
     *
     * @return Username or "404 name not found"
     */
    internal fun retrieveUsernameFromRegistry(): Optional<String> {
        if (!OSUtility.isWindows) {
            return Optional.empty()
        }

        try {
            return Optional.ofNullable(WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "PlayerName"))
        } catch (exception: RegistryException) {
            Logging.warn("Couldn't retrieve Username from registry.", exception)
            return Optional.empty()
        }

    }

    /**
     * Connects to a server, depending on if it is passworded, the user will be asked to enter a
     * password. If the server is not reachable the user can not connect.
     *
     * @param address server address
     * @param port server port
     * @param serverPassword the password to be used for this connection
     */
    fun tryToConnect(address: String, port: Int?, serverPassword: String) {
        try {
            SampQuery(address, port!!).use { query ->
                val serverInfo = query.basicServerInfo

                if (Objects.isNull(serverPassword) || serverPassword.isEmpty() && serverInfo.isPresent && StringUtility.stringToBoolean(serverInfo.get()[0])) {
                    val passwordOptional = promptUserForServerPassword()
                    passwordOptional.ifPresent { password -> SAMPLauncher.connect(address, port, password) }
                } else {
                    SAMPLauncher.connect(address, port, serverPassword)
                }
            }
        } catch (exception: IOException) {
            Logging.warn("Couldn't connect to server.", exception)

            if (askUserIfHeWantsToConnectAnyways()) {
                SAMPLauncher.connect(address, port, serverPassword)
            }
        }

    }

    private fun askUserIfHeWantsToConnectAnyways(): Boolean {
        val alert = Alert(AlertType.CONFIRMATION, Client.getString("serverMightBeOfflineConnectAnyways"), ButtonType.YES, ButtonType.NO)
        alert.title = Client.getString("connectingToServer")
        Client.insertAlertOwner(alert)
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES
    }

    /**
     * Shows a dialog prompting the user for a server password.
     *
     * @return an [Optional] containing either a string (empty or filled) or
     * [Optional.empty]
     */
    fun promptUserForServerPassword(): Optional<String> {
        val dialog = TextInputDialog()
        Client.insertAlertOwner(dialog)
        dialog.title = Client.getString("connectToServer")
        dialog.headerText = Client.getString("enterServerPasswordMessage")

        return dialog.showAndWait()
    }

    /**
     * Shows a TrayNotification that states, that connecting to the server wasn't possible.
     */
    fun showCantConnectToServerError() {
        TrayNotificationBuilder()
                .type(NotificationTypeImplementations.ERROR)
                .title(Client.getString("cantConnect"))
                .message(Client.getString("addressNotValid"))
                .animation(Animations.POPUP)
                .build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME)
    }

    /**
     * Kills SA-MP using the command line.
     */
    fun killSAMP() {
        kill("samp.exe")
    }

    /**
     * Kills GTA using the command line.
     */
    fun killGTA() {
        kill("gta_sa.exe")
    }

    /**
     * Kills a process with a given name.
     *
     * @param processName the name that determines what processes will be killed
     */
    private fun kill(processName: String) {
        if (!OSUtility.isWindows) {
            return
        }

        try {
            Runtime.getRuntime().exec("taskkill /F /IM $processName")
        } catch (exception: IOException) {
            Logging.error("Couldn't kill $processName", exception)
        }

    }

    /**
     * Displays a notification that states, that GTA couldn't be located and links the Settings page.
     */
    fun displayCantLocateGTANotification() {
        val trayNotification = TrayNotificationBuilder()
                .type(NotificationTypeImplementations.ERROR)
                .title(Client.getString("cantFindGTA"))
                .message(Client.getString("locateGTAManually"))
                .animation(Animations.POPUP).build()

        trayNotification.setOnMouseClicked { _ ->
            val clientInstance = Client.instance
            clientInstance!!.loadView(View.SETTINGS)
            clientInstance.settingsController?.selectSampPathTextField()
        }
        trayNotification.showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME)
    }
}// Constructor to prevent instantiation
