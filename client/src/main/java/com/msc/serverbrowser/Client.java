package com.msc.serverbrowser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotification;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.implementations.MainController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.UpdateUtility;
import com.msc.serverbrowser.util.basic.FileUtility;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * This is the main class of the client.
 *
 * @author Marcel
 * @since 02.07.2017
 */
public final class Client extends Application
{
	/**
	 * Application icon that can be used everywhere where necessary.
	 */
	public static final Image		APPLICATION_ICON			= new Image(
			Client.class.getResourceAsStream(PathConstants.APPLICATION_ICON_PATH));
	/**
	 * Name of the application, as displayed to people.
	 */
	public static final String		APPLICATION_NAME			= "SA-MP Client Extension";

	/**
	 * Default Dismiss-{@link Duration} that is used for TrayNotifications.
	 */
	public static final Duration	DEFAULT_TRAY_DISMISS_TIME	= Duration.seconds(10);

	private static Client			instance;
	private Stage					stage;
	private MainController			mainController;

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
	 * Reloads the UI keeps the size correct.
	 */
	public void reloadUI()
	{
		final boolean wasMaximized = stage.isMaximized();

		double width = 0;
		double height = 0;

		if (wasMaximized)
		{// Demaximize to remaximize later for a correct layout
			stage.setMaximized(false);
		}
		else
		{
			width = stage.getWidth();
			height = stage.getHeight();
		}

		loadUIAndGetController();

		if (wasMaximized)
		{
			stage.setMaximized(true);
		}
		else
		{
			stage.setWidth(width);
			stage.setHeight(height);
		}
	}

	private MainController loadUIAndGetController()
	{
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource(PathConstants.VIEW_PATH + "Main.fxml"));
		mainController = new MainController();
		loader.setController(mainController);
		try
		{
			final Parent root = loader.load();
			final Scene scene = new Scene(root);

			if (ClientPropertiesController.getPropertyAsBoolean(Property.USE_DARK_THEME))
			{
				scene.getStylesheets().add(PathConstants.STYLESHEET_PATH + "mainStyleDark.css");
				scene.getStylesheets().add("/styles/trayDark.css");
			}
			else
			{
				scene.getStylesheets().add(PathConstants.STYLESHEET_PATH + "mainStyleLight.css");
				scene.getStylesheets().add("/styles/defaultStyle.css");
			}

			stage.setScene(scene);
		}
		catch (final IOException exception)
		{
			Logging.log(Level.SEVERE, "Couldn't load UI", exception);
			Platform.exit();
		}

		return mainController;
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
		primaryStage.setMaximized(ClientPropertiesController.getPropertyAsBoolean(Property.MAXIMIZED));
		primaryStage.setFullScreen(ClientPropertiesController.getPropertyAsBoolean(Property.FULLSCREEN));
		primaryStage.setResizable(true);

		primaryStage.setOnCloseRequest(close ->
		{
			controller.onClose();
			ClientPropertiesController.setProperty(Property.MAXIMIZED, primaryStage.isMaximized());
			ClientPropertiesController.setProperty(Property.FULLSCREEN, primaryStage.isFullScreen());
		});

		primaryStage.show();

		final boolean showChanelog = ClientPropertiesController.getPropertyAsBoolean(Property.SHOW_CHANGELOG);
		final boolean changelogEnabled = ClientPropertiesController.getPropertyAsBoolean(Property.CHANGELOG_ENABLED);

		if (showChanelog && changelogEnabled)
		{
			final TrayNotification trayNotification = new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.INFORMATION)
					.title("Your client has been updated")
					.message("Click here to see the latest changelog.")
					.animation(Animations.SLIDE)
					.build();

			trayNotification.setOnMouseClicked(__ ->
			{
				ClientPropertiesController.setProperty(Property.SHOW_CHANGELOG, false);
				OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser/releases/latest");
				trayNotification.dismiss();
			});
			trayNotification.showAndWait();
		}
	}

	/**
	 * Displays a dialog that tells the user that the server connection couldn't be
	 * established.
	 */
	public static void displayNoConnectionDialog()
	{
		new TrayNotificationBuilder()
				.type(NotificationTypeImplementations.ERROR)
				.title("Server connection could not be established")
				.message(
						"The server connection doesn't seeem to be established, try again later, for more information check the log files.")
				.animation(Animations.SLIDE)
				.build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);
	}

	/**
	 * Creates files and folders that are necessary for the application to run
	 * properly and migrates
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
	 * Compares the local version number to the one lying on the server. If an
	 * update is available
	 * the user will be asked if he wants to update.
	 */
	private static void checkVersion()
	{
		try
		{
			if (!UpdateUtility.isUpToDate())
			{
				Platform.runLater(() -> displayUpdateNotification());
			}
		}
		catch (final IOException exception)
		{
			Logging.log(Level.WARNING, "Couldn't check for newer version.", exception);
			Platform.runLater(() -> displayCantRetrieveUpdate());
		}

	}

	private static void displayUpdateNotification()
	{
		final TrayNotification trayNotification = new TrayNotificationBuilder()
				.title("Update Available")
				.message("Click here to update to the latest version.")
				.animation(Animations.SLIDE)
				.build();

		trayNotification.setOnMouseClicked(__ ->
		{
			updateLauncher();
			trayNotification.dismiss();
		});
		trayNotification.showAndWait();
	}

	private static void displayCantRetrieveUpdate()
	{
		final TrayNotification trayNotification = new TrayNotificationBuilder()
				.message("Latest version informations couldn't be retrieved, click to update manually.")
				.animation(Animations.POPUP)
				.title("Updating")
				.build();

		trayNotification.setOnMouseClicked(clicked ->
		{
			OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser/releases/latest");
			trayNotification.dismiss();
		});

		trayNotification.showAndWait();
	}

	/**
	 * Downloads the latest version and restarts the client.
	 */
	private static void updateLauncher()
	{
		try
		{
			final String updateUrl = UpdateUtility.getLatestVersionURL();
			final URI url = new URI(updateUrl);
			final String targetLocation = getOwnJarFile().getPath().toString();

			FileUtility.downloadFile(url.toString(), targetLocation);
			final String latestTag = UpdateUtility.getLatestTagName().get();
			ClientPropertiesController.setProperty(Property.SHOW_CHANGELOG, true);
			ClientPropertiesController.setProperty(Property.LAST_TAG_NAME, latestTag);
			selfRestart();
		}
		catch (final IOException | URISyntaxException exception)
		{
			Logging.log(Level.SEVERE, "Couldn't retrieve update.", exception);
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
		final File currentJar = getOwnJarFile();

		if (!currentJar.getName().endsWith(".jar"))
		{// The application wasn't run with a jar file, but in an ide
			return;
		}

		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final List<String> command = new ArrayList<>();

		command.add(javaBin);
		command.add("-jar");
		command.add(currentJar.getPath());

		try
		{
			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
			Platform.exit();
		}
		catch (final IOException exception)
		{
			Logging.log(Level.SEVERE, "Couldn't selfrestart.", exception);
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

	/**
	 * Reloads the active view, incase it is the given one.
	 *
	 * @param view
	 *            the view to reload
	 */
	public void reloadViewIfLoaded(final View view)
	{
		if (mainController.getActiveView() == view)
		{
			mainController.reloadView();
		}
	}
}
