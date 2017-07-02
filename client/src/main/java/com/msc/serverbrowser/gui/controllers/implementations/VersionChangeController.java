package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.constants.Paths;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.FileUtility;
import com.msc.serverbrowser.util.GTA;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import net.lingala.zip4j.exception.ZipException;

/**
 * @since 02.07.2017
 */
public class VersionChangeController implements ViewController
{
	private static final String	NOT_INSTALLING	= "NOT_INSTALLING";
	private static final String	OUTPUT_ZIP		= Paths.SAMPEX_PATH + File.separator + "temp.zip";

	private static String installing = NOT_INSTALLING;

	@FXML
	private Button	buttonZeroThreeSeven;
	@FXML
	private Button	buttonZeroZ;
	@FXML
	private Button	buttonZeroX;
	@FXML
	private Button	buttonZeroD;
	@FXML
	private Button	buttonZeroE;
	@FXML
	private Button	buttonZeroC;
	@FXML
	private Button	buttonZeroA;

	private Button getButtonForVersion(final String version)
	{
		switch (version)
		{
			case "0.3.7":
			{
				return buttonZeroThreeSeven;
			}
			case "0.3z":
			{
				return buttonZeroZ;
			}
			case "0.3x":
			{
				return buttonZeroX;
			}
			case "0.3e":
			{
				return buttonZeroE;
			}
			case "0.3d":
			{
				return buttonZeroD;
			}
			case "0.3c":
			{
				return buttonZeroC;
			}
			case "0.3a":
			{
				return buttonZeroA;
			}
			default:
			{
				throw new IllegalArgumentException("The passed version (" + version + ") doesn't exist or isn't supported.");
			}
		}
	}

	@Override
	public void initialize()
	{
		final Optional<String> versionOptional = GTA.getInstalledVersion();
		versionOptional.ifPresent(version ->
		{
			setAllButtonsDisabled(false);
			final Button versionButton = getButtonForVersion(version);

			if (Objects.nonNull(versionButton))
			{
				versionButton.setDisable(true);
				versionButton.setText("Installed");
			}

			if (!installing.equals(NOT_INSTALLING))
			{
				final Button installingButton = getButtonForVersion(installing);

				if (Objects.nonNull(installingButton))
				{
					installingButton.setText("Installing ...");
					setAllButtonsDisabled(true);
				}
			}
		});

	}

	@FXML
	private void clickVersion(final ActionEvent e)
	{
		final Button clicked = (Button) e.getTarget();

		if (clicked.equals(buttonZeroThreeSeven))
		{
			startVersionChanging("0.3.7");
		}
		else if (clicked.equals(buttonZeroZ))
		{
			startVersionChanging("0.3z");
		}
		else if (clicked.equals(buttonZeroX))
		{
			startVersionChanging("0.3x");
		}
		else if (clicked.equals(buttonZeroE))
		{
			startVersionChanging("0.3e");
		}
		else if (clicked.equals(buttonZeroD))
		{
			startVersionChanging("0.3d");
		}
		else if (clicked.equals(buttonZeroC))
		{
			startVersionChanging("0.3c");
		}
		else if (clicked.equals(buttonZeroA))
		{
			startVersionChanging("0.3a");
		}
	}

	private void startVersionChanging(final String versionToBeInstalled)
	{
		installing = versionToBeInstalled;
		final Optional<String> installedVersion = GTA.getInstalledVersion();

		if (installedVersion.isPresent())
		{
			GTA.killSamp();
			GTA.killGTA();

			final Button oldVersionButton = getButtonForVersion(installedVersion.get());
			final Button newVersionButton = getButtonForVersion(versionToBeInstalled);

			setAllButtonsDisabled(true);
			newVersionButton.setText("Installing ...");

			final Thread thread = new Thread(() ->
			{
				Optional<File> downloadedFile = Optional.empty();
				try
				{
					final Optional<String> gtaPath = GTA.getGtaPath();
					downloadedFile = Optional.of(FileUtility.downloadFile("http://164.132.193.101/sampversion/" + versionToBeInstalled + ".zip", OUTPUT_ZIP));
					FileUtility.unzip(OUTPUT_ZIP, gtaPath.get());

					updateInstallationState(newVersionButton, oldVersionButton);
				}
				catch (final ZipException | IOException | IllegalArgumentException exception)
				{
					Logging.logger().log(Level.SEVERE, "Error Updating client.", exception);

					updateInstallationState(oldVersionButton, newVersionButton);
				}
				finally
				{
					installing = NOT_INSTALLING;
					downloadedFile.ifPresent(File::delete);
				}
			});

			thread.start();
		}
		else
		{
			final Alert alert = new Alert(AlertType.ERROR);
			Client.setupDialog(alert);
			alert.setTitle("Installing SA-MP Version " + versionToBeInstalled);
			alert.setHeaderText("GTA couldn't be located");
			alert.setContentText("It seems like you don't have GTA installed.");
			alert.showAndWait();
		}
	}

	private void updateInstallationState(final Button buttonToSetAsInstalled, final Button buttonToSetAsCanbeInstalled)
	{
		Platform.runLater(() ->
		{
			setAllButtonsDisabled(false);

			buttonToSetAsInstalled.setDisable(true);
			buttonToSetAsInstalled.setText("Installed");

			buttonToSetAsCanbeInstalled.setDisable(false);
			buttonToSetAsCanbeInstalled.setText("Install");
		});
	}

	private void setAllButtonsDisabled(final boolean enabled)
	{
		buttonZeroThreeSeven.setDisable(enabled);
		buttonZeroZ.setDisable(enabled);
		buttonZeroX.setDisable(enabled);
		buttonZeroE.setDisable(enabled);
		buttonZeroD.setDisable(enabled);
		buttonZeroC.setDisable(enabled);
		buttonZeroA.setDisable(enabled);
	}

	@Override
	public void onClose()
	{
		// Do nothing
	}
}
