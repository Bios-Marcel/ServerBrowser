package com.msc.serverbrowser.util.samp;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotification;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;
import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.PastUsernames;
import com.msc.serverbrowser.data.insallationcandidates.InstallationCandidate;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.implementations.SettingsController;
import com.msc.serverbrowser.gui.controllers.implementations.VersionChangeController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.HashingUtility;
import com.msc.serverbrowser.util.basic.StringUtility;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

/**
 * Contains utility methods for interacting with native samp stuff.
 *
 * @author Marcel
 */
public final class GTAController {
	/**
	 * Holds the users username.
	 */
	public static StringProperty usernameProperty = new SimpleStringProperty(retrieveUsernameFromRegistry());

	private GTAController() {
		// Constructor to prevent instantiation
	}

	/**
	 * Writes the actual username (from registry) into the past usernames list and sets the new name
	 */
	public static void applyUsername() {
		if (!OSUtility.isWindows()) {
			return;
		}

		killSAMP();
		PastUsernames.addPastUsername(retrieveUsernameFromRegistry());
		try {
			WindowsRegistry.getInstance().writeStringValue(HKey.HKCU, "SOFTWARE\\SAMP", "PlayerName", usernameProperty.get());
		}
		catch (final RegistryException exception) {
			Logging.warn("Couldn't set username.", exception);
		}

	}

	// TODO Think of a better solution
	/**
	 * Returns the Username that samp has set in the registry.
	 *
	 * @return Username or "404 name not found"
	 */
	static String retrieveUsernameFromRegistry() {
		if (!OSUtility.isWindows()) {
			return "You are on Linux ;D";
		}

		try {
			return WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "PlayerName");
		}
		catch (final RegistryException exception) {
			Logging.warn("Couldn't retrieve Username from registry.", exception);
			return "404 Name not found";
		}
	}

	/**
	 * Returns the GTA path.
	 *
	 * @return {@link Optional} of GTA path or an empty {@link Optional} if GTA couldn't be found
	 */
	public static Optional<String> getGtaPath() {
		if (!OSUtility.isWindows()) {
			return Optional.empty();
		}

		final Optional<String> path = getGtaPathFromRegistry();
		if (path.isPresent()) {
			return path;
		}

		final String property = ClientPropertiesController.getPropertyAsString(Property.SAMP_PATH);
		if (Objects.isNull(property) || property.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(property.endsWith(File.separator) ? property : property + File.separator);
	}

	/**
	 * Should only be used if necessary.
	 *
	 * @return String of the GTA Path or null.
	 */
	private static Optional<String> getGtaPathFromRegistry() {
		try {
			return Optional.ofNullable(WindowsRegistry.getInstance().readString(HKey.HKCU, "SOFTWARE\\SAMP", "gta_sa_exe").replace("gta_sa.exe", ""));
		}
		catch (final RegistryException exception) {
			Logging.warn("Couldn't retrieve GTA path.", exception);
			return Optional.empty();
		}
	}

	/**
	 * Returns the {@link InstallationCandidate} value that represents the currently installed samp
	 * version.
	 *
	 * @return {@link Optional} of installed versions version number or an {@link Optional#empty()}
	 */
	public static Optional<InstallationCandidate> getInstalledVersion() {
		final Optional<String> path = getGtaPath();
		if (!path.isPresent()) {
			// GTA couldn't be found
			return Optional.empty();
		}

		final File file = new File(path.get() + "samp.dll");
		if (!file.exists()) {
			// samp.dll doesn't exist, even though GTA is installed at this point.
			return Optional.empty();
		}

		try {
			final String hashsum = HashingUtility.generateChecksum(file.toString());
			return VersionChangeController.INSTALLATION_CANDIDATES.stream()
					.filter(candidate -> candidate.getSampDLLChecksum().equalsIgnoreCase(hashsum))
					.findFirst();
		}
		catch (NoSuchAlgorithmException | IOException exception) {
			Logging.error("Error hashing installed samp.dll", exception);
		}

		return Optional.empty();
	}

	/**
	 * Connects to a server, depending on if it is passworded, the user will be asked to enter a
	 * password. If the server is not reachable the user can not connect.
	 *
	 * @param address server address
	 * @param port server port
	 * @param serverPassword the password to be used for this connection
	 */
	public static void tryToConnect(final String address, final Integer port, final String serverPassword) {
		try (final SampQuery query = new SampQuery(address, port)) {
			final Optional<String[]> serverInfo = query.getBasicServerInfo();

			if (Objects.isNull(serverPassword) || serverPassword.isEmpty() && serverInfo.isPresent() && StringUtility.stringToBoolean(serverInfo.get()[0])) {
				final Optional<String> passwordOptional = askForServerPassword();
				passwordOptional.ifPresent(password -> SAMPLauncher.connect(address, port, password));
			}
			else {
				SAMPLauncher.connect(address, port, serverPassword);
			}
		}
		catch (final IOException exception) {
			Logging.warn("Couldn't connect to server.", exception);

			final Optional<ButtonType> decision = askUserIfHeWantsToConnectAnyways();
			decision.ifPresent(button -> {
				if (button == ButtonType.YES) {
					SAMPLauncher.connect(address, port, serverPassword);
				}
			});
		}
	}

	private static Optional<ButtonType> askUserIfHeWantsToConnectAnyways() {
		final Alert alert = new Alert(AlertType.CONFIRMATION, Client.getString("serverMightBeOfflineConnectAnyways"), ButtonType.YES, ButtonType.NO);
		alert.setTitle(Client.getString("connectingToServer"));
		Client.insertAlertOwner(alert);
		return alert.showAndWait();
	}

	public static Optional<String> askForServerPassword() {
		final TextInputDialog dialog = new TextInputDialog();
		Client.insertAlertOwner(dialog);
		dialog.setTitle(Client.getString("connectToServer"));
		dialog.setHeaderText(Client.getString("enterServerPasswordMessage"));

		return dialog.showAndWait();
	}

	/**
	 * Shows a TrayNotification that states, that connecting to the server wasn't possible.
	 */
	public static void showCantConnectToServerError() {
		new TrayNotificationBuilder()
				.type(NotificationTypeImplementations.ERROR)
				.title(Client.getString("cantConnect"))
				.message(Client.getString("addressNotValid"))
				.animation(Animations.POPUP)
				.build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);
	}

	/**
	 * Kills SA-MP using the command line.
	 */
	public static void killSAMP() {
		kill("samp.exe");
	}

	/**
	 * Kills GTA using the command line.
	 */
	public static void killGTA() {
		kill("gta_sa.exe");
	}

	/**
	 * Kills a process with a given name.
	 *
	 * @param processName the name that determines what processes will be killed
	 */
	private static void kill(final String processName) {
		if (!OSUtility.isWindows()) {
			return;
		}

		try {
			Runtime.getRuntime().exec("taskkill /F /IM " + processName);
		}
		catch (final IOException exception) {
			Logging.error("Couldn't kill " + processName, exception);
		}
	}

	/**
	 * Displays a notifcation that states, that GTA couldn't be located and links the Settings page.
	 */
	public static void displayCantLocateGTANotification() {
		final TrayNotification trayNotification = new TrayNotificationBuilder()
				.type(NotificationTypeImplementations.ERROR)
				.title(Client.getString("cantFindGTA"))
				.message(Client.getString("locateGTAManually"))
				.animation(Animations.POPUP).build();

		trayNotification.setOnMouseClicked(__ -> {
			final Client clientInstance = Client.getInstance();
			clientInstance.loadView(View.SETTINGS);
			clientInstance.getSettingsController().ifPresent(SettingsController::selectSampPathTextField);
		});
		trayNotification.showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);
	}
}
