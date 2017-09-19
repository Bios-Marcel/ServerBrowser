package com.msc.serverbrowser.gui.components;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.logging.Logging;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Special class used to show image previews.
 *
 * @author Marcel
 * @since 18.09.2017
 */
public class ImagePreview extends VBox
{
	/**
	 * @param fileImage
	 *            the {@link File} of the image to preview
	 * @param information
	 *            textual information that will appear beneath the picture
	 */
	public ImagePreview(final File fileImage, final String information)
	{
		setupComponents(fileImage, information);

		if (Desktop.isDesktopSupported())
		{
			registerActions(fileImage);
		}
	}

	private void setupComponents(final File fileImage, final String information)
	{
		final Image image = new Image(fileImage.toURI().toString(), 180, 180, true, false);

		setStyle("-fx-border-width: 1.0; -fx-border-color: gray; -fx-border-insets: -5.0;");
		setPrefWidth(180);
		setPrefHeight(180);

		final ImageView imageView = new ImageView(image);
		imageView.setPreserveRatio(true);
		imageView.setSmooth(false);
		imageView.fitWidthProperty().bind(prefWidthProperty());

		final StackPane imageContainer = new StackPane(imageView);
		getChildren().add(imageContainer);

		VBox.setVgrow(imageContainer, Priority.ALWAYS);

		final Label informationLabel = new Label(information);
		getChildren().add(informationLabel);
	}

	private void registerActions(final File fileImage)
	{
		final Desktop desktop = Desktop.getDesktop();
		setOnMouseClicked(clicked ->
		{
			if (clicked.getButton() == MouseButton.PRIMARY)
			{
				if (desktop.isSupported(Action.OPEN))
				{
					try
					{
						desktop.open(fileImage);
					}
					catch (final IOException exception)
					{
						Logging.logger().log(Level.WARNING, "Error trying to open image.", exception);
						new TrayNotificationBuilder()
								.animation(Animations.POPUP)
								.message("Error trying to open image '" + fileImage.getName() + "'.")
								.title("Opening image")
								.build()
								.showAndDismiss(Duration.seconds(10));
					}
				}
			}
		});
	}
}
