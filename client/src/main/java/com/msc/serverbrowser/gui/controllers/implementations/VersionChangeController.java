package com.msc.serverbrowser.gui.controllers.implementations;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.insallationcandidates.InstallationCandidate;
import com.msc.serverbrowser.data.insallationcandidates.Installer;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.util.samp.GTAController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
	private final String	INSTALL_TEXT	= Client.getString("install");
	private final String	INSTALLED_TEXT	= Client.getString("installed");
	private final String	INSTALLING_TEXT	= Client.getString("installing");
	private final String	SAMP_VERSION	= Client.getString("sampVersion");

	private static Optional<InstallationCandidate>		currentlyInstalling	= Optional.empty();
	private final Map<InstallationCandidate, Button>	buttons				= new HashMap<>();

	/**
	 * Contains all available Installation candidates
	 */
	public static final ObservableList<InstallationCandidate> INSTALLATION_CANDIDATES = FXCollections.observableArrayList();

	@FXML
	private VBox buttonContainer;

	/*
	 * Adding all usable InstallationCandidates, but this could probably be made in a more desirable
	 * way.
	 */
	static {
		INSTALLATION_CANDIDATES
				.add(new InstallationCandidate("BCCDB297464BD382625635BE25585DF07A8FA6668BC0015650708E3EB4FFCD4B", "0.3.DL R1", "http://forum.sa-mp.com/files/03DL/sa-mp-0.3.DL-R1-install.exe", false, true, "FDDBEF743914D6A4A8D9B9895F219864BBB238A447BF36B8A8D652E303D78ACB"));
		INSTALLATION_CANDIDATES
				.add(new InstallationCandidate("65ACB8C9CB0D6723BB9D151BA182A02EC6F7766E2225E66EB51FF1CA95454B43", "0.3.8-RC4-4", "http://forum.sa-mp.com/files/038RC/sa-mp-0.3.8-RC4-4-install.exe", false, true, "69D590121ACA9CB04E71355405B9DC2CEBA681AD940AA43AC1F274129918ABB5"));
		INSTALLATION_CANDIDATES
				.add(new InstallationCandidate("DE07A850590A43D83A40F9251741C07D3D0D74A217D5A09CB498A32982E8315B", "0.3.7 R2", "http://files.sa-mp.com/sa-mp-0.3.7-R2-install.exe", false, true, "8F37CC4E7B4E1201C52B981E4DDF70D30937E5AD1F699EC6FC43ED006FE9FD58"));
		INSTALLATION_CANDIDATES
				.add(new InstallationCandidate("0382C4468E00BEDFE0188EA819BF333A332A4F0D36E6FC07B11B79F4B6D93E6A", "0.3z R2", "http://files.sa-mp.com/sa-mp-0.3z-R2-install.exe", false, true, "2DD1BEB2FB1630A44CE5C47B80685E984D3166F8D93EE5F98E35CC072541919E"));
		INSTALLATION_CANDIDATES
				.add(new InstallationCandidate("23B630CC5C922EE4AA4EF9E93ED5F7F3F9137ACA32D5BCAD6A0C0728D4A17CC6", "0.3x R2", "http://files.sa-mp.com/sa-mp-0.3x-R2-install.exe", false, true, "F75929EC22DF9492219D226C2BDFCDDB5C4351AE71F4B36589B337B10F4656CD"));
		INSTALLATION_CANDIDATES
				.add(new InstallationCandidate("54E1494661962302C8166B1B747D8ED86C69F26FA3E0C5456C129F998883B410", "0.3e", "http://files.sa-mp.com/sa-mp-0.3e-install.exe", false, true, "83C4145E36DF63AF40877969C2D9F97A4B9AF780DB4EF7C0E82861780BF59D5C"));
		INSTALLATION_CANDIDATES
				.add(new InstallationCandidate("D97D6D4750512653B157EDEBC7D5960A4FD7B1E55E04A9ACD86687900A9804BC", "0.3d R2", "http://files.sa-mp.com/sa-mp-0.3d-R2-install.exe", false, true, "FFE1B66DAB76FF300C8563451106B130054E01671CEFDEBE709AD8DC0D3A319A"));
		INSTALLATION_CANDIDATES
				.add(new InstallationCandidate("6A584102E655202871D2158B2659C5B5581AB48ECFB21D330861484AE0CB3043", "0.3c R3", "http://files.sa-mp.com/sa-mp-0.3c-R3-install.exe", false, true, "8CCD7A22B3BF24F00EC32F55AE28CA8F50C48B03504C98BD4FAC3EF661163B4C"));
		INSTALLATION_CANDIDATES
				.add(new InstallationCandidate("23901473FB98F9781B68913F907F3B7A88D9D96BBF686CC65AD1505E400BE942", "0.3a", "http://files.sa-mp.com/sa-mp-0.3a-install.exe", false, true, "690FFF2E9433FF36788BF4F4EA9D8C601EB42471FD1210FE1E0CFFC4F54D7D9D"));
	}

	@Override
	public void initialize() {
		createAndSetupButtons();
		updateButtonStates();
	}

	/**
	 * Will create a {@link HBox} for every {@link InstallationCandidate}, said {@link HBox} will
	 * contain a {@link Label} and a {@link Button}.
	 */
	private void createAndSetupButtons() {
		for (final InstallationCandidate candidate : INSTALLATION_CANDIDATES) {
			final HBox versionContainer = new HBox();

			if (!buttonContainer.getChildren().isEmpty()) {
				buttonContainer.getChildren().add(new Separator());
			}

			versionContainer.getStyleClass().add("installEntry");

			final Label title = new Label(MessageFormat.format(SAMP_VERSION, candidate.getName()));
			title.getStyleClass().add("installLabel");
			title.setMaxWidth(Double.MAX_VALUE);

			final Button installButton = new Button(INSTALL_TEXT);
			installButton.setOnAction(__ -> installSamp(installButton, candidate));
			installButton.getStyleClass().add("installButton");
			buttons.put(candidate, installButton);

			versionContainer.getChildren().add(title);
			versionContainer.getChildren().add(installButton);

			buttonContainer.getChildren().add(versionContainer);

			HBox.setHgrow(title, Priority.ALWAYS);
		}
	}

	/**
	 * <p>
	 * Triggers the installation of the chosen {@link InstallationCandidate}.
	 * </p>
	 *
	 * @param button the {@link Button} which was clicked.
	 * @param toInstall {@link InstallationCandidate} which will be installed
	 */
	private void installSamp(final Button button, final InstallationCandidate toInstall) {
		if (GTAController.getGtaPath().isPresent()) {
			final Optional<InstallationCandidate> installedVersion = GTAController.getInstalledVersion();

			if (!installedVersion.isPresent() || !installedVersion.get().equals(toInstall)) {
				setAllButtonsDisabled(true);
				button.setText(INSTALLING_TEXT);

				GTAController.killGTA();
				GTAController.killSAMP();

				/*
				 * TODO(MSC) Check JavaFX Threading API (Task / Service) Using a thread here, in
				 * case someone wants to keep using the application meanwhile
				 */
				new Thread(() -> {
					Installer.installViaInstallationCandidate(toInstall);
					finishInstalling();
				}).start();
			}
		}
		else {
			Client.getInstance().selectSampPathTextField();
		}
	}

	private static void finishInstalling() {
		currentlyInstalling = Optional.empty();
		Platform.runLater(() -> Client.getInstance().reloadViewIfLoaded(View.VERSION_CHANGER));
	}

	/**
	 * Decides which buttons will be enabled and what text every button will have, depending on if
	 * an installation is going on and what is currently installed.
	 */
	private void updateButtonStates() {
		final Optional<InstallationCandidate> installedVersion = GTAController.getInstalledVersion();
		final boolean ongoingInstallation = currentlyInstalling.isPresent();

		for (final Entry<InstallationCandidate, Button> entry : buttons.entrySet()) {
			final Button button = entry.getValue();
			final InstallationCandidate buttonVersion = entry.getKey();

			if (installedVersion.isPresent() && installedVersion.get() == buttonVersion) {
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
	}

	private void setAllButtonsDisabled(final boolean disabled) {
		buttons.forEach((key, value) -> value.setDisable(disabled));
	}

	@Override
	public void onClose() {
		// Do nothing
	}
}