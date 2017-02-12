package controllers;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import util.FileUtility;
import util.GTA;

public class VersionChangeController implements ViewController
{
	public static final String		OUTPUT_ZIP	= System.getProperty("user.home") + File.separator + "sampex" + File.separator + "temp.zip";

	private static String			installing	= "";

	@FXML
	private Button					buttonZeroThreeSeven;

	@FXML
	private Button					buttonZeroZ;

	@FXML
	private Button					buttonZeroX;

	@FXML
	private Button					buttonZeroD;

	@FXML
	private Button					buttonZeroE;

	@FXML
	private Button					buttonZeroC;

	@FXML
	private Button					buttonZeroA;

	private final MainController	mainController;

	public VersionChangeController(final MainController mainController)
	{
		this.mainController = mainController;
	}

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
				return null;
			}
		}
	}

	@Override
	public void init()
	{
		final String version = GTA.getInstalledVersion();

		final Button versionButton = getButtonForVersion(version);

		if (Objects.nonNull(versionButton))
		{
			versionButton.setDisable(true);
			versionButton.setText("Installed");
		}

		if (!installing.equals(""))
		{
			final Button installingButton = getButtonForVersion(installing);

			if (Objects.nonNull(installingButton))
			{
				installingButton.setText("Installing ...");
				disableAllButtons();
			}
		}

	}

	@FXML
	public void clickVersion(final ActionEvent e)
	{
		final Button clicked = (Button) e.getTarget();

		disableAllButtons();
		clicked.setText("Installing ...");

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

	private void startVersionChanging(final String version)
	{
		installing = version;
		final Thread thread = new Thread(() ->
		{
			try
			{
				FileUtility.downloadUsingNIO("http://ts3.das-chat.xyz/sampversion/" + version + ".zip", OUTPUT_ZIP);
				final File file = new File(OUTPUT_ZIP);
				FileUtility.unZipIt(file, GTA.getGtaPath());
				file.delete();
				installing = "";
				Platform.runLater(() ->
				{
					if (mainController != null)
					{
						mainController.refreshVersionChangerViewIfDisplayed();
					}
				});

			}
			catch (IOException | IllegalArgumentException e)
			{
				e.printStackTrace();
			}
		});

		thread.start();
	}

	// Codeah died for this code, RIP

	private void disableAllButtons()
	{
		buttonZeroThreeSeven.setDisable(true);
		buttonZeroZ.setDisable(true);
		buttonZeroX.setDisable(true);
		buttonZeroE.setDisable(true);
		buttonZeroD.setDisable(true);
		buttonZeroC.setDisable(true);
		buttonZeroA.setDisable(true);
	}
}
