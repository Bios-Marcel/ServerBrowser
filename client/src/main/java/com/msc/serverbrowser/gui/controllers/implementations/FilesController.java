package com.msc.serverbrowser.gui.controllers.implementations;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.gui.views.FilesView;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.FileUtility;
import com.msc.serverbrowser.util.basic.StringUtility;

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

		filesView.getShowColorsProperty().addListener(__ -> loadChatLog());
		filesView.getShowColorsAsTextProperty().addListener(__ -> loadChatLog());
		filesView.getShowTimesIfAvailableProperty().addListener(__ -> loadChatLog());

		filesView.getLineFilterProperty().addListener(__ -> loadChatLog());

		loadChatLog();
	}

	private void loadChatLog() {

		final Boolean darkThemeInUse = ClientPropertiesController.getPropertyAsBoolean(Property.USE_DARK_THEME);
		final String gray = "#333131";
		final String white = "#FFFFFF";
		final StringBuilder newContent = new StringBuilder("<html><body style='background-color: ")
				.append(darkThemeInUse ? gray : white)
				.append("; color: ")
				.append(darkThemeInUse ? white : gray)
				.append("'>");

		try {
			final Path path = Paths.get(PathConstants.SAMP_CHATLOG);
			final String filterProperty = filesView.getLineFilterProperty().getValueSafe().toLowerCase();

			FileUtility.readAllLinesTryEncodings(path, ISO_8859_1, UTF_8, US_ASCII)
					.stream()
					.filter(((Predicate<String>) String::isEmpty).negate())
					.filter(line -> line.toLowerCase().contains(filterProperty))
					.map(StringUtility::escapeHTML)
					.map(this::processSampChatlogTimestamps)
					.map(this::processSampColorCodes)
					.map(line -> line + "<br/>")
					.forEach(newContent::append);
		}
		catch (final IOException exception) {
			Logging.error("Error loading chatlog.", exception);
		}

		filesView.setChatLogTextAreaContent(newContent.toString());
	}

	private String processSampChatlogTimestamps(final String line) {
		if (filesView.getShowTimesIfAvailableProperty().get()) {
			return line;
		}

		final String timeRegex = "\\[(?:(?:([01]?\\d|2[0-3]):)?([0-5]?\\d):)?([0-5]?\\d)\\]";
		if (line.length() >= 10 && line.substring(0, 10).matches(timeRegex)) {
			return line.replaceFirst(timeRegex, "");
		}

		return line;
	}

	private String processSampColorCodes(final String line) {
		final boolean showColorsAsText = filesView.getShowColorsAsTextProperty().get();
		final boolean showColors = filesView.getShowColorsProperty().get();
		if (showColorsAsText && !showColors) {
			return line;
		}

		final String colorRegex = "([{](.{6})[}])";

		if (showColors) {
			String fixedLine = "<span>" + line.replace("{000000}", "{FFFFFF}");
			final Matcher colorCodeMatcher = Pattern.compile(colorRegex).matcher(fixedLine);
			while (colorCodeMatcher.find()) {

				final String replacementColorCode = "#" + colorCodeMatcher.group(2);
				final String color = colorCodeMatcher.group(1);
				String replacement = "</span><span style='color:" + replacementColorCode + ";'>";
				if (showColorsAsText) {
					replacement += color;
				}
				fixedLine = fixedLine.replace(color, replacement);
			}

			return fixedLine + "</span>";
		}

		return line.replaceAll(colorRegex, "");
	}

	private void clearChatLog() {

		try {
			Files.deleteIfExists(Paths.get(PathConstants.SAMP_CHATLOG));
			filesView.setChatLogTextAreaContent("");
		}
		catch (final IOException exception) {
			new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.ERROR)
					.animation(Animations.POPUP)
					.title(Client.getString("couldntClearChatLog"))
					.message(Client.getString("checkLogsForMoreInformation")).build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);

			Logging.warn("Couldn't clear chatlog", exception);
		}
	}
}
