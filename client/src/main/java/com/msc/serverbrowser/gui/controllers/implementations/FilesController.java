package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 * Controls the Files view which allows you to look at your taken screenshots,
 * your chatlogs and
 * your saved positions.
 *
 * @author Marcel
 * @since 08.07.2017
 */
public class FilesController implements ViewController
{
	// Chatlogs
	@FXML
	private TextArea contentTextArea;

	@Override
	public void initialize()
	{
		loadChatLog();
	}

	@FXML
	private void loadChatLog()
	{
		try
		{
			final List<String> lines = Files.readAllLines(Paths.get(PathConstants.SAMP_CHATLOG));

			contentTextArea.clear();

			lines.forEach(line ->
			{
				final String newLine = (line + System.lineSeparator()).replaceAll("([{].{6}[}])", "");
				contentTextArea.insertText(contentTextArea.getText().length(), newLine);
			});

			// Replace Color Codes TODO(MSC) Implement Color feature
		}
		catch (final IOException exception)
		{
			Logging.log(Level.SEVERE, "Error loading chatlog.", exception);
		}
	}

	@FXML
	private void clearChatLog()
	{
		try
		{
			Files.deleteIfExists(Paths.get(PathConstants.SAMP_CHATLOG));
			contentTextArea.clear();
		}
		catch (final IOException exception)
		{
			new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.ERROR)
					.animation(Animations.POPUP)
					.title("Chatlog couldn't be cleared")
					.message("For more information, please check the logfiles.")
					.build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);

			Logging.log(Level.WARNING, "Couldn't clear chatlog", exception);
		}
	}

	@Override
	public void onClose()
	{
		// Unused
	}
}
