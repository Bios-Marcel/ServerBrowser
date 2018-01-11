package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.CacheController;
import com.msc.serverbrowser.data.insallationcandidates.InstallationCandidate;
import com.msc.serverbrowser.data.insallationcandidates.SourceType;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.FileUtility;
import com.msc.serverbrowser.util.samp.GTAController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * @since 02.07.2017
 */
public class VersionChangeController implements ViewController {
	private final String	INSTALL_TEXT	= Client.lang.getString("install");
	private final String	INSTALLED_TEXT	= Client.lang.getString("installed");
	private final String	INSTALLING_TEXT	= Client.lang.getString("installing");
	private final String	SAMP_VERSION	= Client.lang.getString("sampVersion");

	private static Optional<InstallationCandidate>	currentlyInstalling	= Optional.empty();
	private final List<Button>						buttons				= new ArrayList<>();

	/**
	 * Contains all available Installation candidates
	 */
	public static final ObservableList<InstallationCandidate> INSTALLATION_CANDIDATES = FXCollections.observableArrayList();

	@FXML private VBox buttonContainer;

	/**
	 * Adding all usable InstallationCandidates, but this could probably be made in a more
	 * desirable way.
	 */
	static {
		final String site = PathConstants.SAMP_DOWNLOAD_LOCATION;

		INSTALLATION_CANDIDATES.add(new InstallationCandidate("de07a850590a43d83a40f9251741c07d3d0d74a217d5a09cb498a32982e8315b", "0.3.7", site
				+ "0.3.7.zip", false, SourceType.INTERNET, "4E414B25BA6FCC9E756821809C7189D2FE41C24CDE8B919D4C6F1DC8A4780CA3"));
		INSTALLATION_CANDIDATES.add(new InstallationCandidate("0382c4468e00bedfe0188ea819bf333a332a4f0d36e6fc07b11b79f4b6d93e6a", "0.3z", site
				+ "0.3z.zip", false, SourceType.INTERNET, "9ECD672DC16C24EF445AA1B411CB737832362B2632ACDA60BCC66358D4D85AD3"));
		INSTALLATION_CANDIDATES.add(new InstallationCandidate("23b630cc5c922ee4aa4ef9e93ed5f7f3f9137aca32d5bcad6a0c0728d4a17cc6", "0.3x", site
				+ "0.3x.zip", false, SourceType.INTERNET, "B0D3FE71D9F7FF39D18468F6FCD506B8D1B28267EC81D7616E886B9A238400EC"));
		INSTALLATION_CANDIDATES.add(new InstallationCandidate("54e1494661962302c8166b1b747d8ed86c69f26fa3e0c5456c129f998883b410", "0.3e", site
				+ "0.3e.zip", false, SourceType.INTERNET, "13E2F31718C24ADE07E3E8E79D644957589C1584022FA2F87895A1B7298F1C25"));
		INSTALLATION_CANDIDATES.add(new InstallationCandidate("d97d6d4750512653b157edebc7d5960a4fd7b1e55e04a9acd86687900a9804bc", "0.3d", site
				+ "0.3d.zip", false, SourceType.INTERNET, "356E78D14221D74793349A9C306720CDF9D1B2EC94172A27D85163818CBDE63C"));
		INSTALLATION_CANDIDATES.add(new InstallationCandidate("6a584102e655202871d2158b2659c5b5581ab48ecfb21d330861484ae0cb3043", "0.3c", site
				+ "0.3c.zip", false, SourceType.INTERNET, "F5C1A0EDF562F188365038D97A28F950AFF8CA56C7362F9DC813FDC2BDE3B8F6"));
		INSTALLATION_CANDIDATES.add(new InstallationCandidate("23901473fb98f9781b68913f907f3b7a88d9d96bbf686cc65ad1505e400be942", "0.3a", site
				+ "0.3a.zip", false, SourceType.INTERNET, "C860D1032BBD9DCC9DF9E0E4E89611D5F12C967E29BE138CCBCC3ECB3303C2BF"));
	}

	@Override
	public void initialize() {
		createAndSetupButtons();
		updateButtonStates();
	}

	/**
	 * Will create an {@link HBox} for every {@link SAMPVersion}, said {@link HBox}
	 * will contain a {@link Label} and a {@link Button}.
	 */
	private void createAndSetupButtons() {
		for (final InstallationCandidate candidate : INSTALLATION_CANDIDATES) {
			final HBox versionContainer = new HBox();

			if (!buttonContainer.getChildren().isEmpty()) {
				final Separator separator = new Separator(Orientation.HORIZONTAL);
				separator.getStyleClass().add("separator");
				buttonContainer.getChildren().add(separator);
			}

			versionContainer.getStyleClass().add("installEntry");

			final Label title = new Label(MessageFormat.format(SAMP_VERSION, candidate.getName()));
			title.getStyleClass().add("installLabel");
			title.setMaxWidth(Double.MAX_VALUE);

			final Button installButton = new Button(INSTALL_TEXT);
			installButton.setUserData(candidate);
			installButton.setOnAction(__ -> installAction(installButton));
			installButton.getStyleClass().add("installButton");
			buttons.add(installButton);

			versionContainer.getChildren().add(title);
			versionContainer.getChildren().add(installButton);

			buttonContainer.getChildren().add(versionContainer);

			HBox.setHgrow(title, Priority.ALWAYS);
		}
	}

	/**
	 * Triggers the installation of the chosen {@link SAMPVersion}.
	 *
	 * @param button
	 *            the {@link Button} which was clicked.
	 */
	private void installAction(final Button button) {
		final InstallationCandidate toInstall = (InstallationCandidate) button.getUserData();
		final Optional<InstallationCandidate> installedVersion = GTAController.getInstalledVersion();

		if (installedVersion.isPresent()) {
			setAllButtonsDisabled(true);
			button.setText(INSTALLING_TEXT);

			GTAController.killSAMP();
			GTAController.killGTA();

			/*
			 * TODO Marcel 09.01.2018 I will keep the caching in here for a while, even though
			 * that'd mean
			 * duplicated all local installation candidates.
			 */

			if (CacheController.isVersionCached(toInstall)) {
				installCachedVersion(toInstall);
				finishInstalling();
			}
			else {
				// TODO(MSC) Check JavaFX Threading API (Task / Service)
				// Using a thread here, incase someone wants to keep using the app meanwhile
				new Thread(() -> {
					Optional<File> downloadedFile = Optional.empty();
					try {
						currentlyInstalling = Optional.of(toInstall);
						final Optional<String> gtaPath = GTAController.getGtaPath();

						switch (toInstall.getSourceType()) {
							case FILE_SYSTEM:
								FileUtility.unzip(new File(toInstall.getUrl()).toString(), gtaPath.get());
								break;
							case INTERNET:
								downloadedFile = Optional.of(FileUtility.downloadFile(toInstall.getUrl(), PathConstants.OUTPUT_ZIP));
								if (ClientPropertiesController.getPropertyAsBoolean(Property.ALLOW_CACHING_DOWNLOADS)) {
									CacheController.addVersionToCache(toInstall, PathConstants.OUTPUT_ZIP);
								}
								FileUtility.unzip(PathConstants.OUTPUT_ZIP, gtaPath.get());
								break;
							case RESSOURCE:
								// TODO (Marcel 10.01.2018): I am not quite sure, if i ever wanna
								// ship with a samp version already installed.
								break;
						}
					}
					catch (final IOException | IllegalArgumentException exception) {
						Logging.log(Level.SEVERE, "Error Updating client.", exception);
					}

					downloadedFile.ifPresent(File::delete);
					finishInstalling();
				}).start();
			}
		}
		else {
			GTAController.displayCantLocateGTANotification();
		}
	}

	private static void finishInstalling() {
		currentlyInstalling = Optional.empty();
		Platform.runLater(() -> Client.getInstance().reloadViewIfLoaded(View.VERSION_CHANGER));
	}

	private static void installCachedVersion(final InstallationCandidate cachedVersion) {
		try {
			final File cachedVersionFile = new File(PathConstants.CLIENT_CACHE + File.separator + cachedVersion.getName() + "_"
					+ cachedVersion.getSampDLLChecksum() + ".zip");

			FileUtility.unzip(cachedVersionFile.getAbsolutePath(), GTAController.getGtaPath().get());
		}
		catch (final IOException exception) {
			Logging.log(Level.SEVERE, "Error while trying to install SA-MP from cache.", exception);

			new TrayNotificationBuilder().type(NotificationTypeImplementations.ERROR).title(Client.lang.getString("installingSampFromCache"))
					.message(Client.lang.getString("errorInstallingSampFromCache")).animation(Animations.POPUP).build()
					.showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);
		}
	}

	/**
	 * Decides which buttons will be enabled and what text every button will have,
	 * depending on if
	 * an installation is going on and what is currently installed.
	 */
	private void updateButtonStates() {
		final Optional<InstallationCandidate> installedVersion = GTAController.getInstalledVersion();
		final boolean ongoingInstallation = currentlyInstalling.isPresent();

		installedVersion.ifPresent(version -> {
			for (final Button button : buttons) {
				// Safe cast, because i only use this method to indicate what version this
				// button reflects, noone has access on the outside.
				final InstallationCandidate buttonVersion = (InstallationCandidate) button.getUserData();

				if (buttonVersion == version) {
					button.setText(INSTALLED_TEXT);
					button.setDisable(true);
				}
				else if (ongoingInstallation && buttonVersion == currentlyInstalling.get()) {
					button.setText(INSTALLING_TEXT);
					button.setDisable(true);
				}
				else {
					button.setText(INSTALL_TEXT);
					button.setDisable(ongoingInstallation);
				}
			}
		});
	}

	private void setAllButtonsDisabled(final boolean disabled) {
		buttons.forEach(button -> button.setDisable(disabled));
	}

	@Override
	public void onClose() {
		// Do nothing
	}
}