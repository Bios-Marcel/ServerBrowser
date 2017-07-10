package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.Notifications;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.StringUtil;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Controls the Files view which allows you to look at your taken screenshots, your chatlogs and
 * your saved positions.
 *
 * @author Marcel
 * @since 08.07.2017
 */
public class FilesController implements ViewController
{
	// Screenshots
	@FXML
	private StackPane		imageContainer;
	@FXML
	private ComboBox<File>	screenshotComboBox;
	@FXML
	private Label			takenValue;
	@FXML
	private Label			sizeValue;

	private File						presentImage;
	private final ObservableList<File>	screenshots	= FXCollections.observableArrayList();

	/**
	 * Compares {@link File files} depending against their last modified date.
	 */
	private static final Comparator<File> fileComparator = (fileOne, fileTwo) -> new Long(fileOne.lastModified()).compareTo(new Long(fileTwo.lastModified()));

	// Saved Positions

	// Chatlogs
	@FXML
	private TextArea contentTextArea;

	@Override
	public void initialize()
	{
		screenshotComboBox.setItems(screenshots);
		updateComboBoxContent();
		loadChatLog();
		nextImage();
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
		catch (@SuppressWarnings("unused") final IOException exception)
		{
			// TODO (MSC) Literally anything
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
					.type(Notifications.ERROR)
					.animation(Animations.POPUP)
					.title("Chatlog couldn't be cleared")
					.message("For more information, please check the logfiles.")
					.build().showAndDismiss(Duration.seconds(10));

			Logging.logger().log(Level.WARNING, "Couldn't clear chatlog", exception);
		}
	}

	private void updateComboBoxContent()
	{
		final File sampFolder = new File(PathConstants.SAMP_PATH + "\\screens");
		if (sampFolder.exists())
		{
			screenshots.setAll(sampFolder.listFiles());
			screenshots.sort(fileComparator);
		}
	}

	@FXML
	private void chooseImage()
	{
		final Optional<File> optionalImage = Optional.ofNullable(screenshotComboBox.getValue());
		setImageAndReload(optionalImage);
	}

	@FXML
	private void previousImage()
	{
		final Optional<File> optionalImage = getPreviousScreenshot();
		setImageAndReload(optionalImage);
	}

	@FXML
	private void nextImage()
	{
		final Optional<File> optionalImage = getNextScreenshot();
		setImageAndReload(optionalImage);
	}

	private void setImageAndReload(final Optional<File> optionalImage)
	{
		optionalImage.ifPresent(image ->
		{
			presentImage = image;
			loadPresentImage();
		});
	}

	private void loadPresentImage()
	{
		imageContainer.setStyle("-fx-background-image: url(\"" + pathToImage(presentImage.getAbsoluteFile()) + "\");");
		sizeValue.setText(StringUtil.humanReadableByteCount(presentImage.length()));
		final Instant timeAsInstant = Instant.ofEpochMilli(presentImage.lastModified());
		final LocalDateTime localDateTime = LocalDateTime.ofInstant(timeAsInstant, ZoneId.systemDefault());
		final String localDateTimeFormatted = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(localDateTime);
		takenValue.setText(localDateTimeFormatted);

		Platform.runLater(() ->
		{// Getting IndexOutOfBound Exceptions otherwise
			updateComboBoxContent();
			screenshotComboBox.setValue(presentImage);
		});
	}

	private Optional<File> getNextScreenshot()
	{
		final File sampFolder = new File(PathConstants.SAMP_PATH + "\\screens");

		if (sampFolder.exists())
		{
			final List<File> filesInFolder = Arrays.asList(sampFolder.listFiles());
			filesInFolder.sort(fileComparator);
			final int index = filesInFolder.indexOf(presentImage);

			File nextImage;
			if (filesInFolder.size() > index + 1)
			{
				nextImage = filesInFolder.get(index + 1);
			}
			else
			{
				nextImage = filesInFolder.get(0);
			}

			return Optional.of(nextImage);
		}

		return Optional.empty();

	}

	private Optional<File> getPreviousScreenshot()
	{
		final File sampFolder = new File(PathConstants.SAMP_PATH + "\\screens");

		if (sampFolder.exists())
		{
			final List<File> filesInFolder = Arrays.asList(sampFolder.listFiles());
			filesInFolder.sort(fileComparator);
			final int index = filesInFolder.indexOf(presentImage);

			File nextImage;
			if (index >= 1)
			{
				nextImage = filesInFolder.get(index - 1);
			}
			else
			{
				nextImage = filesInFolder.get(filesInFolder.size() - 1);
			}

			return Optional.of(nextImage);
		}

		return Optional.empty();
	}

	/**
	 * Creates a {@link URI} pointing to the passed {@link File}.
	 *
	 * @param nextImage
	 *            the {@link File} to create a {@link URI} for
	 * @return {@link URI} pointing to the passed {@link File}
	 */
	private URI pathToImage(final File nextImage)
	{
		try
		{
			final URL url = new URL("file:/" + nextImage.getAbsolutePath());
			return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		}
		catch (final Exception exception)
		{
			// TODO(MSC) Investigate, if this is a good idea.
			throw new RuntimeException(exception);
		}
	}

	@Override
	public void onClose()
	{
		// TODO Auto-generated method stub
	}
}
