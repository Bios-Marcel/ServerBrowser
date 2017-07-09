package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.Notifications;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
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

	private File						presentImage;
	private final ObservableList<File>	screenshots	= FXCollections.observableArrayList();

	// Saved Positions

	// Chatlogs
	@FXML
	private WebView chatLogWebView;

	@Override
	public void initialize()
	{
		screenshotComboBox.setItems(screenshots);
		updateComboBoxContent();
		loadChatLog();
	}

	@FXML
	private void loadChatLog()
	{
		try
		{
			final List<String> lines = Files.readAllLines(Paths.get(PathConstants.SAMP_CHATLOG));

			final StringBuilder htmlContent = new StringBuilder();

			htmlContent.append("<html><body>");

			lines.forEach(line ->
			{
				htmlContent.append(line);
				htmlContent.append("<br>");
			});

			htmlContent.append("</body></html>");
			String content = htmlContent.toString();

			// Replace Color Codes TODO(MSC) Implement Color feature
			content = content.replaceAll("[{].{6}[}]", "");

			chatLogWebView.getEngine().loadContent(content);
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
			Files.delete(Paths.get(PathConstants.SAMP_CHATLOG));
			loadChatLog();
		}
		catch (final IOException exception)
		{
			TrayNotificationBuilder builder = new TrayNotificationBuilder()
					.type(Notifications.ERROR)
					.animation(Animations.POPUP)
					.title("Chatlog couldn't be cleared")
					.message("For more information, please check the logfiles.");

			if (ClientProperties.getPropertyAsBoolean(Property.USE_DARK_THEME))
			{
				builder = builder.stylesheet(PathConstants.STYLESHEET_PATH + "trayDark.css");
			}

			builder.build().showAndDismiss(Duration.seconds(10));

			Logging.logger().log(Level.WARNING, "Couldn't clear chatlog", exception);
		}
	}

	private void updateComboBoxContent()
	{
		final File sampFolder = new File(PathConstants.SAMP_PATH + "\\screens");
		if (sampFolder.exists())
		{
			screenshots.setAll(sampFolder.listFiles());
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
		screenshotComboBox.setValue(presentImage);
		updateComboBoxContent();
	}

	private Optional<File> getNextScreenshot()
	{
		final File sampFolder = new File(PathConstants.SAMP_PATH + "\\screens");

		if (sampFolder.exists())
		{
			final List<File> filesInFolder = Arrays.asList(sampFolder.listFiles());

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

	private URI pathToImage(final File nextImage)
	{
		try
		{
			final URL url = new URL("file:/" + nextImage.getAbsolutePath());
			return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		}
		catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onClose()
	{
		// TODO Auto-generated method stub
	}

}
