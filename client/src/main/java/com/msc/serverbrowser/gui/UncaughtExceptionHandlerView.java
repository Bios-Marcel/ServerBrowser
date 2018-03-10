package com.msc.serverbrowser.gui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import com.msc.serverbrowser.Client;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Simple view for showing runtime errors, it contains the error message, the stacktrace and a
 * github issue creation hyperlink.
 *
 * @author Marcel
 * @since 06.03.2018
 */
public class UncaughtExceptionHandlerView {
	private static final String	TITLE	= "An error occurred during program execution";
	private final Parent		root;
	private Optional<Stage>		stage	= Optional.empty();

	/**
	 * @param controller the view controller
	 * @param cause the {@link Throwable} instance that requires a error dialog
	 */
	public UncaughtExceptionHandlerView(final UncaughtExceptionHandlerController controller, final Throwable cause) {
		final ImageView icon = new ImageView(new Image(this.getClass().getResourceAsStream("/com/msc/serverbrowser/icons/error.png")));
		final Label title = new Label(TITLE);
		title.setFont(Font.font(title.getFont().getFamily(), FontWeight.BOLD, 22.0));
		title.setMaxHeight(Double.MAX_VALUE);
		final HBox header = new HBox(15.0, icon, title);

		final Label messageHeader = new Label("Error message:");
		messageHeader.setFont(Font.font(messageHeader.getFont().getFamily(), FontWeight.BOLD, 12.0));
		messageHeader.setMaxHeight(Double.MAX_VALUE);

		final Label message = new Label(cause.getMessage());

		final Label stackTraceHeader = new Label("Full stacktrace:");
		stackTraceHeader.setFont(Font.font(stackTraceHeader.getFont().getFamily(), FontWeight.BOLD, 12.0));
		stackTraceHeader.setMaxHeight(Double.MAX_VALUE);

		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		cause.printStackTrace(printWriter);
		final String stackTraceContent = stringWriter.toString();
		final TextArea stackTrace = new TextArea(stackTraceContent);
		stackTrace.setEditable(false);
		stackTrace.setMaxHeight(Double.MAX_VALUE);

		final Label whatDoHeader = new Label("What can i do?");
		whatDoHeader.setFont(Font.font(whatDoHeader.getFont().getFamily(), FontWeight.BOLD, 12.0));
		whatDoHeader.setMaxHeight(Double.MAX_VALUE);

		final Hyperlink whatDo = new Hyperlink("Create an issue on Github.");
		whatDo.setUnderline(true);
		whatDo.setOnAction(__ -> controller.onOpenGithubIssue(message.getText(), stackTrace.getText()));

		final VBox information = new VBox(5.0, messageHeader, message, whatDoHeader, whatDo, stackTraceHeader, stackTrace);
		information.setPadding(new Insets(0.0, 30.0, 0.0, 35.0));
		VBox.setVgrow(stackTrace, Priority.ALWAYS);
		final Insets internalContentInsets = new Insets(0.0, 0.0, 0.0, 15.0);
		VBox.setMargin(stackTrace, internalContentInsets);
		VBox.setMargin(message, internalContentInsets);
		VBox.setMargin(whatDo, internalContentInsets);

		final VBox content = new VBox(10.0, header, information);
		content.setPadding(new Insets(5.0));
		VBox.setVgrow(information, Priority.ALWAYS);

		final Button closeButton = new Button("Close");
		closeButton.setOnAction(__ -> stage.ifPresent(Stage::close));
		final ButtonBar buttonBar = new ButtonBar();
		buttonBar.setPadding(new Insets(5.0));
		buttonBar.getButtons().add(closeButton);

		root = new VBox(5.0, content, buttonBar);
		VBox.setVgrow(content, Priority.ALWAYS);
	}

	/**
	 * Shows the previously initialized View in a new {@link Stage}.
	 */
	public void show() {
		final Stage dialogStage = new Stage();
		stage = Optional.of(dialogStage);

		dialogStage.getIcons().add(Client.APPLICATION_ICON);
		dialogStage.setTitle(Client.APPLICATION_NAME + " - " + TITLE);
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		final Scene scene = new Scene(root, 600.0, 400.0);
		dialogStage.setScene(scene);
		dialogStage.showAndWait();

		stage = Optional.empty();
	}
}
