package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.gui.views.FilesView;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.FileUtility;

/**
 * Controls the Files view which allows you to look at your taken screenshots, your chatlogs and
 * your saved positions.
 *
 * @author Marcel
 * @since 08.07.2017
 */
public class FilesController implements ViewController {

	private final FilesView filesView;

	/**
	 * @param filesView
	 *            the view to be used by this controller
	 */
	public FilesController(final FilesView filesView) {
		this.filesView = filesView;

		filesView.setLoadChatLogsButtonAction(__ -> loadChatLog());
		filesView.setClearChatLogsButtonAction(__ -> clearChatLog());

		loadChatLog();
	}

	private void loadChatLog() {

		// Replace Color Codes TODO(MSC) Implement Color feature
		final StringBuilder newContent = new StringBuilder();

		try {
			FileUtility
					.readAllLinesTryEncodings(Paths
							.get(PathConstants.SAMP_CHATLOG), StandardCharsets.ISO_8859_1, StandardCharsets.UTF_8, StandardCharsets.US_ASCII)
					.forEach(line -> {
						final String textWithoutColorCodes = line.replaceAll("([{].{6}[}])", "");
						newContent.append(textWithoutColorCodes);
						newContent.append(System.lineSeparator());
					});

		} catch (final IOException exception) {
			Logging.error("Error loading chatlog.", exception);
		}

		filesView.setChatLogTextAreaContent(newContent.toString());
	}

	private void clearChatLog() {

		try {
			Files.deleteIfExists(Paths.get(PathConstants.SAMP_CHATLOG));
			filesView.setChatLogTextAreaContent("");
		} catch (final IOException exception) {
			new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.ERROR)
					.animation(Animations.POPUP)
					.title(Client.lang.getString("couldntClearChatLog"))
					.message(Client.lang.getString("checkLogsForMoreInformation")).build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);

			Logging.warn("Couldn't clear chatlog", exception);
		}
	}
}
