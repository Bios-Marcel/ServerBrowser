package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.File;
import java.io.IOException;
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
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.SAMPVersion;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.FileUtility;
import com.msc.serverbrowser.util.samp.GTAController;

import javafx.application.Platform;
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
public class VersionChangeController implements ViewController
{
	private static final String INSTALL_TEXT = "Install";

	private static Optional<SAMPVersion>	currentlyInstalling	= Optional.empty();
	private final List<Button>				buttons				= new ArrayList<>();

	@FXML private VBox buttonContainer;

	@Override
	public void initialize()
	{
		createAndSetupButtons();
		updateButtonStates();
	}

	/**
	 * Will create an {@link HBox} for every {@link SAMPVersion}, said {@link HBox} will contain a
	 * {@link Label} and a {@link Button}.
	 */
	private void createAndSetupButtons()
	{
		final SAMPVersion[] versions = SAMPVersion.values();
		for (int i = 0; i < versions.length; i++)
		{
			final SAMPVersion version = versions[i];

			final HBox versionContainer = new HBox();
			versionContainer.getStyleClass().add("installEntry");

			final Label title = new Label("SA-MP Version " + version.getVersionIdentifier());
			title.getStyleClass().add("installLabel");
			title.setMaxWidth(Double.MAX_VALUE);

			final Button installButton = new Button(INSTALL_TEXT);
			installButton.setUserData(version);
			installButton.setOnAction(__ -> installAction(installButton));
			installButton.getStyleClass().add("installButton");
			buttons.add(installButton);

			versionContainer.getChildren().add(title);
			versionContainer.getChildren().add(installButton);

			buttonContainer.getChildren().add(versionContainer);

			HBox.setHgrow(title, Priority.ALWAYS);

			if (i != versions.length - 1)
			{
				final Separator separator = new Separator(Orientation.HORIZONTAL);
				separator.getStyleClass().add("separator");
				buttonContainer.getChildren().add(separator);
			}
		}
	}

	/**
	 * Triggers the installation of the chosen {@link SAMPVersion}.
	 *
	 * @param button
	 *            the {@link Button} which was clicked.
	 */
	private void installAction(final Button button)
	{
		final SAMPVersion toInstall = (SAMPVersion) button.getUserData();
		final Optional<SAMPVersion> installedVersion = GTAController.getInstalledVersion();

		if (installedVersion.isPresent())
		{
			setAllButtonsDisabled(true);
			button.setText("Installing ...");

			GTAController.killSAMP();
			GTAController.killGTA();

			if (CacheController.isVersionCached(toInstall))
			{
				installCachedVersion(toInstall);
				finishInstalling();
			}
			else
			{
				// TODO(MSC) Check JavaFX Threading API (Task / Service)
				// Using a thread here, incase someone wants to keep using the app meanwhile
				new Thread(() ->
				{
					Optional<File> downloadedFile = Optional.empty();
					try
					{
						currentlyInstalling = Optional.of(toInstall);
						final Optional<String> gtaPath = GTAController.getGtaPath();
						final String willBeDownloaded = PathConstants.SAMP__DOWNLOAD_LOCATION
								+ toInstall.getVersionIdentifier() + ".zip";

						downloadedFile = Optional.of(FileUtility.downloadFile(willBeDownloaded, PathConstants.OUTPUT_ZIP));
						if (ClientPropertiesController.getPropertyAsBoolean(Property.ALLOW_CACHING_DOWNLOADS))
						{
							CacheController.addVersionToCache(toInstall, PathConstants.OUTPUT_ZIP);
						}
						FileUtility.unzip(PathConstants.OUTPUT_ZIP, gtaPath.get());
					}
					catch (final IOException | IllegalArgumentException exception)
					{
						Logging.log(Level.SEVERE, "Error Updating client.", exception);
					}

					downloadedFile.ifPresent(File::delete);
					finishInstalling();
				}).start();
			}
		}
		else
		{
			new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.ERROR)
					.title("GTA couldn't be located")
					.message("If this isn't correct, please head to the settings view and manually enter your GTA path.")
					.animation(Animations.POPUP)
					.build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);
		}
	}

	private static void finishInstalling()
	{
		currentlyInstalling = Optional.empty();
		Platform.runLater(() -> Client.getInstance().reloadViewIfLoaded(View.VERSION_CHANGER));
	}

	private static void installCachedVersion(final SAMPVersion cachedVersion)
	{
		try
		{
			final File cachedVersionFile = new File(PathConstants.CLIENT_CACHE + File.separator + cachedVersion.getVersionIdentifier() + ".zip");

			FileUtility.unzip(cachedVersionFile.getAbsolutePath(), GTAController.getGtaPath().get());
		}
		catch (final IOException exception)
		{
			Logging.log(Level.SEVERE, "Error while trying to install SA-MP from cache.", exception);

			new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.ERROR)
					.title("Installing SA-MP from Cache")
					.message("Error while trying to install SA-MP from cache, check logs for further information.")
					.animation(Animations.POPUP)
					.build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);
		}
	}

	/**
	 * Decides which buttons will be enabled and what text every button will have, depending on if
	 * an installation is going on and what is currently installed.
	 */
	private void updateButtonStates()
	{
		final Optional<SAMPVersion> installedVersion = GTAController.getInstalledVersion();
		final boolean ongoingInstallation = currentlyInstalling.isPresent();

		installedVersion.ifPresent(version ->
		{
			for (final Button button : buttons)
			{
				// Safe cast, because i only use this method to indicate what version this
				// button reflects, noone has access on the outside.
				final SAMPVersion buttonVersion = (SAMPVersion) button.getUserData();

				if (buttonVersion == version)
				{
					button.setText("Installed");
					button.setDisable(true);
				}
				else if (ongoingInstallation && buttonVersion == currentlyInstalling.get())
				{
					button.setText("Installing...");
					button.setDisable(true);
				}
				else
				{
					button.setText(INSTALL_TEXT);
					button.setDisable(ongoingInstallation);
				}
			}
		});
	}

	private void setAllButtonsDisabled(final boolean disabled)
	{
		buttons.forEach(button -> button.setDisable(disabled));
	}

	@Override
	public void onClose()
	{
		// Do nothing
	}
}