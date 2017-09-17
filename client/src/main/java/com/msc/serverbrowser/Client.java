package com.msc.serverbrowser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.logging.Level;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotification;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.controllers.implementations.MainController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.FileUtil;
import com.msc.serverbrowser.util.UpdateUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * @since 02.07.2017
 */
public class Client extends Application
{
	/**
	 * Application icon that can be used everywhere where necessary.
	 */
	public static final Image	APPLICATION_ICON	= new Image(Client.class.getResourceAsStream("/com/msc/serverbrowser/icons/icon.png"));
	/**
	 * Name of the application, as displayed to people.
	 */
	public static final String	APPLICATION_NAME	= "SA-MP Client Extension";

	/**
	 * Windows Registry.
	 */
	public static Registry registry;

	private Stage stage;

	private static Client instance;

	/**
	 * @return the clients singleton instance
	 */
	public static Client getInstance()
	{
		return instance;
	}

	@Override
	public void start(final Stage primaryStage)
	{
		instance = this;
		initClient();
		loadUI(primaryStage);
		new Thread(() -> checkVersion()).start();
	}

	/**
	 * Loads the UI as if the Client has just been started.
	 */
	public void loadUI()
	{
		loadUIAndGetController();
	}

	private MainController loadUIAndGetController()
	{
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource(PathConstants.VIEW_PATH + "Main.fxml"));
		final MainController controller = new MainController();
		loader.setController(controller);
		try
		{
			final Parent root = loader.load();
			final Scene scene = new Scene(root);

			if (ClientProperties.getPropertyAsBoolean(Property.USE_DARK_THEME))
			{
				scene.getStylesheets().add(PathConstants.STYLESHEET_PATH + "mainStyleDark.css");
				scene.getStylesheets().add("/styles/trayDark.css");
			}
			else
			{
				scene.getStylesheets().add(PathConstants.STYLESHEET_PATH + "mainStyle.css");
				scene.getStylesheets().add("/styles/defaultStyle.css");
			}

			stage.setScene(scene);
		}
		catch (final Exception exception)
		{
			Logging.logger().log(Level.SEVERE, "Couldn't load UI", exception);
			System.exit(0);
		}

		return controller;
	}

	/**
	 * Loads the main UI.
	 *
	 * @param primaryStage
	 *            the stage to use for displaying the UI
	 */
	private void loadUI(final Stage primaryStage)
	{
		stage = primaryStage;

		final MainController controller = loadUIAndGetController();

		TrayNotificationBuilder.setDefaultOwner(stage);

		primaryStage.getIcons().add(APPLICATION_ICON);
		primaryStage.setMaximized(ClientProperties.getPropertyAsBoolean(Property.MAXIMIZED));
		primaryStage.setFullScreen(ClientProperties.getPropertyAsBoolean(Property.FULLSCREEN));
		primaryStage.setResizable(true);

		final Scene primaryScene = primaryStage.getScene();

		primaryStage.setOnCloseRequest(close ->

		{
			controller.onClose();
			ClientProperties.setProperty(Property.MAXIMIZED, primaryStage.isMaximized());
			ClientProperties.setProperty(Property.FULLSCREEN, primaryStage.isFullScreen());
		});

		primaryStage.show();

		// Set the preferred with of the root container to size the window correctly
		final Pane root = (Pane) primaryScene.getRoot();
		root.setPrefHeight(480);
		root.setPrefWidth(785);

		if (ClientProperties.getPropertyAsBoolean(Property.SHOW_CHANGELOG) && ClientProperties.getPropertyAsBoolean(Property.SHOW_CHANGELOG_AFTER_UPDATE))
		{
			final TrayNotificationBuilder builder = new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.INFORMATION)
					.title("Your client has been updated")
					.message("Click here to see the latest changelog.")
					.animation(Animations.SLIDE);

			final TrayNotification notification = builder.build();
			notification.setOnMouseClicked(__ -> showChangelog());
			notification.showAndWait();
		}
	}

	/**
	 * @deprecated TODO(MSC) Replace current changelog
	 *             <p>
	 *             Options:
	 *             <ul>
	 *             <li>Completly new dialog</li>
	 *             <li>Open textfile</li>
	 *             <li>Open Webpage (Github Release)</li>
	 *             <li>Show mardown formatted file</li>
	 *             </ul>
	 *             </p>
	 */
	@Deprecated
	private void showChangelog()
	{
		final Alert alert = new Alert(AlertType.INFORMATION);
		setupDialog(alert);
		alert.setTitle(APPLICATION_NAME + "- Changelog");
		alert.setHeaderText("Your client has been updated | Changelog");

		final StringBuilder updateText = new StringBuilder();

		updateText.append("- More configurable SA-MP legacy settings");
		updateText.append(System.lineSeparator() + System.lineSeparator());

		alert.setContentText(updateText.toString());
		alert.show();
		ClientProperties.setProperty(Property.SHOW_CHANGELOG, false);
	}

	/**
	 * Displays a dialog that tells the user that the server connection couldn't be established.
	 */
	public static void displayNoConnectionDialog()
	{
		new TrayNotificationBuilder()
				.type(NotificationTypeImplementations.ERROR)
				.title("Server connection could not be established")
				.message("The server connection doesn't seeem to be established, try again later, for more information check the log files.")
				.animation(Animations.SLIDE)
				.build().showAndDismiss(Duration.seconds(10));
	}

	// TODO(MSC) Delete as soon as the Changelog dialog is removed
	/**
	 * <p>
	 * Sets up a dialog; performs the following actions:
	 * </p>
	 * <ul>
	 * <li>sets stylesheets</li>
	 * <li>sets the owner stage</li>
	 * <li>sets the modality</li>
	 * <li>sets the icon</li>
	 * </ul>
	 *
	 * @param alert
	 *            the {@link Alert} that will be set up
	 */
	private void setupDialog(final Alert alert)
	{
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(APPLICATION_ICON);
		final ObservableList<String> clientStylesheets = stage.getScene().getStylesheets();
		alert.getDialogPane().getStylesheets().addAll(clientStylesheets);
		alert.initOwner(stage);
		alert.initModality(Modality.APPLICATION_MODAL);
	}

	/**
	 * Creates files and folders that are necessary for the application to run properly and migrates
	 * old xml data.
	 */
	private static void initClient()
	{
		final File sampexFolder = new File(PathConstants.SAMPEX_PATH);

		if (!sampexFolder.exists())
		{
			sampexFolder.mkdir();
		}
	}

	/**
	 * Compares the local version number to the one lying on the server. If an update is available
	 * the user will be asked if he wants to update.
	 */
	private static void checkVersion()
	{

		if (!UpdateUtil.isUpToDate())
		{
			Platform.runLater(() ->
			{
				final TrayNotification notification = new TrayNotificationBuilder()
						.title("Update Available")
						.message("Click here to update to the latest version. Not updating might lead to problems.")
						.animation(Animations.SLIDE)
						.build();

				notification.setOnMouseClicked(__ -> updateLauncher());
				notification.showAndWait();
			});
		}

	}

	/**
	 * Downloads the latest version and restarts the client.
	 */
	private static void updateLauncher()
	{
		try
		{
			final String updateUrl = UpdateUtil.getLatestVersionURL();
			final URI url = new URI(updateUrl);
			FileUtil.downloadFile(url.toString(), getOwnJarFile().getPath().toString());
			ClientProperties.setProperty(Property.SHOW_CHANGELOG, true);
			selfRestart();
		}
		catch (final IOException | URISyntaxException exception)
		{
			Logging.logger().log(Level.SEVERE, "Couldn't retrieve update.", exception);
		}
	}

	/**
	 * @return a File pointing to the applications own jar file
	 */
	private static File getOwnJarFile()
	{
		return new File(System.getProperty("java.class.path")).getAbsoluteFile();
	}

	/**
	 * Restarts the application.
	 */
	private static void selfRestart()
	{
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final File currentJar = getOwnJarFile();

		if (!currentJar.getName().endsWith(".jar"))
		{// The application wasn't run with a jar file, but in an ide
			return;
		}

		final ArrayList<String> command = new ArrayList<>();
		command.add(javaBin);
		command.add("-jar");
		command.add(currentJar.getPath());

		final ProcessBuilder builder = new ProcessBuilder(command);

		try
		{
			builder.start();
			System.exit(0);
		}
		catch (final IOException exception)
		{
			Logging.logger().log(Level.SEVERE, "Couldn't selfrestart.", exception);
		}
	}

	/**
	 * Programs entry point, it also intitializes specific when passed as args.
	 *
	 * @param args
	 *            used to determine what backend to connect to
	 */
	public static void main(final String[] args)
	{
		Application.launch(args);
	}

	/**
	 * Sets the Applications title.
	 *
	 * @param title
	 *            the title to set
	 */
	public void setTitle(final String title)
	{
		stage.setTitle(title);
	}
}
