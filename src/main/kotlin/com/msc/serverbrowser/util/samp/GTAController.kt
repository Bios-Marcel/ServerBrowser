package com.msc.serverbrowser.util.samp

import com.github.plushaze.traynotification.animations.Animations
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder
import com.msc.serverbrowser.Client
import com.msc.serverbrowser.data.PastUsernames
import com.msc.serverbrowser.data.insallationcandidates.InstallationCandidate
import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.data.properties.SampPathProperty
import com.msc.serverbrowser.gui.View
import com.msc.serverbrowser.gui.controllers.implementations.VersionChangeController
import com.msc.serverbrowser.severe
import com.msc.serverbrowser.util.basic.HashingUtility
import com.msc.serverbrowser.util.basic.StringUtility
import com.msc.serverbrowser.util.unix.WineUtility
import com.msc.serverbrowser.util.windows.OSUtility
import com.msc.serverbrowser.util.windows.Registry
import com.msc.serverbrowser.warn
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.control.TextInputDialog
import java.io.File
import java.io.IOException
import java.security.NoSuchAlgorithmException
import java.util.Objects
import java.util.Optional

/**
 * Contains utility methods for interacting with native samp stuff.
 *
 * @author Marcel
 */
object GTAController {
    /**
     * Holds the users username.
     */
    val usernameProperty: StringProperty = SimpleStringProperty(retrieveUsernameFromRegistry() ?: "")

    /**
     * Returns the path to GTA, eventually the path will be resolved if the current OS is not Windows.
     *
     * If a custom path has been set, that will be returned instead.
     */
    val gtaPath: String?
        get() {
            val savedProperty = ClientPropertiesController.getProperty(SampPathProperty)

            if (savedProperty.isNotBlank()) {
                return if (savedProperty.endsWith(File.separator)) {
                    savedProperty
                } else {
                    savedProperty + File.separator
                }
            }

            return gtaPathFromRegistry
        }

    /**
     * Returns the path to GTA without resolving it with `winepath`, no matter which OS the client runs on.
     *
     * If a custom path has been set, that will be returned instead.
     */
    val windowsStyleGtaPath: String?
        get() {
            val savedProperty = ClientPropertiesController.getProperty(SampPathProperty)

            if (savedProperty.isNotBlank()) {
                return if (savedProperty.endsWith(File.separator)) {
                    savedProperty
                } else {
                    savedProperty + File.separator
                }
            }

            return windowsGtaPathFromRegistry
        }

    private val windowsGtaPathFromRegistry: String?
        get() {
            return Registry
                    .readString("HKCU\\Software\\SAMP", "gta_sa_exe")
                    ?.replace("gta_sa.exe", "")
        }

    private val gtaPathFromRegistry: String?
        get() {
            val retrievedPath = windowsGtaPathFromRegistry

            if (OSUtility.isWindows) {
                return retrievedPath
            }

            if (retrievedPath != null) {
                return WineUtility.convertPath(retrievedPath)
            }

            return retrievedPath
        }

    /**
     * Returns the [InstallationCandidate] value that represents the currently installed samp
     * version.
     *
     * @return [Optional] of installed versions version number or an [Optional.empty]
     */
    // GTA couldn't be found samp.dll doesn't exist, even though GTA is installed at this point.
    val installedVersion: Optional<InstallationCandidate>
        get() {
            val path = gtaPath ?: return Optional.empty()

            val file = File(path + "samp.dll")
            if (file.exists().not()) {
                return Optional.empty()
            }

            try {
                val hashSum = HashingUtility.generateChecksum(file.toString())
                return VersionChangeController.INSTALLATION_CANDIDATES.stream()
                        .filter { candidate -> candidate.sampDllChecksum.equals(hashSum, ignoreCase = true) }
                        .findFirst()
            } catch (exception: NoSuchAlgorithmException) {
                severe("Error hashing installed samp.dll", exception)
            } catch (exception: IOException) {
                severe("Error hashing installed samp.dll", exception)
            }

            return Optional.empty()
        }

    /**
     * Writes the actual username (from registry) into the past usernames list and sets the new name
     */
    fun applyUsername() {

        //TODO Implement for linux
        if (OSUtility.isWindows) {
            killSAMP()
        }

        retrieveUsernameFromRegistry()
                .takeIf { it != usernameProperty.get() }
                ?.apply(PastUsernames::addPastUsername)

        Registry.writeString("HKCU\\Software\\SAMP", "PlayerName", usernameProperty.get())
    }

    // TODO Think of a better solution
    /**
     * Returns the Username that samp has set in the registry.
     *
     * @return Username or "404 name not found"
     */
    internal fun retrieveUsernameFromRegistry() = Registry.readString("HKCU\\Software\\SAMP", "PlayerName")

    /**
     * Connects to a server, depending on if it is passworded, the user will be asked to enter a
     * password. If the server is not reachable the user can not connect.
     *
     * @param address server address
     * @param port server port
     * @param serverPassword the password to be used for this connection
     */
    fun tryToConnect(client: Client, address: String, port: Int, serverPassword: String) {
        try {
            SampQuery(address, port).use { query ->
                val serverInfo = query.basicServerInfo

                if (Objects.isNull(serverPassword) || serverPassword.isEmpty() && serverInfo.isPresent && StringUtility.stringToBoolean(serverInfo.get()[0])) {
                    val passwordOptional = promptUserForServerPassword(client)
                    passwordOptional.ifPresent { password -> SAMPLauncher.connect(client, address, port, password) }
                } else {
                    SAMPLauncher.connect(client, address, port, serverPassword)
                }
            }
        } catch (exception: IOException) {
            warn("Couldn't connect to server.", exception)

            if (askUserIfHeWantsToConnectAnyways(client)) {
                SAMPLauncher.connect(client, address, port, serverPassword)
            }
        }

    }

    private fun askUserIfHeWantsToConnectAnyways(client: Client): Boolean {
        val alert = Alert(AlertType.CONFIRMATION, Client.getString("serverMightBeOfflineConnectAnyways"), ButtonType.YES, ButtonType.NO)
        alert.title = Client.getString("connectingToServer")
        alert.initOwner(client.stage!!)
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES
    }

    /**
     * Shows a dialog prompting the user for a server password.
     *
     * @return an [Optional] containing either a string (empty or filled) or
     * [Optional.empty]
     */
    fun promptUserForServerPassword(client: Client): Optional<String> {
        val dialog = TextInputDialog()
        dialog.initOwner(client.stage!!)
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
            severe("Couldn't kill $processName", exception)
        }

    }

    /**
     * Displays a notification that states, that GTA couldn't be located and links the Settings page.
     */
    fun displayCantLocateGTANotification(client: Client) {
        val trayNotification = TrayNotificationBuilder()
                .type(NotificationTypeImplementations.ERROR)
                .title(Client.getString("cantFindGTA"))
                .message(Client.getString("locateGTAManually"))
                .animation(Animations.POPUP).build()

        trayNotification.setOnMouseClicked { _ ->
            client.loadView(View.SETTINGS)
            client.settingsController?.selectSampPathTextField()
        }
        trayNotification.showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME)
    }
}
