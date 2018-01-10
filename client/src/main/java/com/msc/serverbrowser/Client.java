package com.msc.serverbrowser;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.kohsuke.github.GHRelease;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotification;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.gui.View;
import com.msc.serverbrowser.gui.controllers.implementations.MainController;
import com.msc.serverbrowser.gui.views.MainView;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.UpdateUtility;
import com.msc.serverbrowser.util.basic.ArrayUtility;
import com.msc.serverbrowser.util.basic.FileUtility;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
	public static final Image	APPLICATION_ICON	= new Image(Client.class.getResourceAsStream(PathConstants.APP_ICON_PATH));
	/**
	 * Name of the application, as displayed to people.
	 */
	public static final String	APPLICATION_NAME	= "SA-MP Server Browser";

	/**
	 * Default Dismiss-{@link Duration} that is used for TrayNotifications.
	 */
	public static final Duration DEFAULT_TRAY_DISMISS_TIME = Duration.seconds(10);

	private static Client			instance;
	private Stage					stage;
	private MainController	mainController;

	/**
	 * RessourceBundle which contains all the loclized strings.
	 */
	public static ResourceBundle lang;

	/**
	 * This property that indicates if an update check / download progress is
	 * ongoing.
	 */
	public final BooleanProperty updatingProperty = new SimpleBooleanProperty(false);

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
		loadUI(primaryStage);

		// Only update if not in development mode
		if (!ClientPropertiesController.getPropertyAsBoolean(Property.DEVELOPMENT))
		{
			if (new File(PathConstants.SAMPEX_TEMP_JAR).exists())
			{
				finishUpdate();
			}
			else if (ClientPropertiesController.getPropertyAsBoolean(Property.AUTOMTAIC_UPDATES))
			{
				checkForUpdates();
			}
		}
	}

	/**
	 * Sets the current {@link Client} stage as the clients owner.
	 *
	 * @param alert
	 *            the alert to set the owner in
	 */
	public static void insertAlertOwner(final Alert alert)
	{
		alert.initOwner(getInstance().stage);
	}

	private MainController loadUIAndGetController()
	{
		final MainView mainView = new MainView();
		mainController = new MainController(mainView);
		mainController.initialize();
		final Scene scene = new Scene(mainView.getRootPane());
		stage.setScene(scene);

		applyTheme();
		return mainController;
	}

	/**
	 * Deletes the scenes current stylesheets and reapplies either the dark theme or
	 * the default
	 * theme.
	 */
	public void applyTheme()
	{
		// Retrieving the current scene, assuring it ain't null.
		final Scene scene = Objects.requireNonNull(stage.getScene());

		scene.getStylesheets().clear();
		scene.getStylesheets().add(PathConstants.STYLESHEET_PATH + "mainStyleGeneral.css");

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

		if (ClientPropertiesController.getPropertyAsBoolean(Property.SHOW_CHANGELOG)
				&& ClientPropertiesController.getPropertyAsBoolean(Property.CHANGELOG_ENABLED))
		{

			// Since the changelog has been shown after this update, it shall not be shown again,
			// unless there is another update
			ClientPropertiesController.setProperty(Property.SHOW_CHANGELOG, false);

			final TrayNotification trayNotification = new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.INFORMATION)
					.title(Client.lang.getString("updated"))
					.message(Client.lang.getString("clickForChangelog"))
					.animation(Animations.SLIDE).build();

			trayNotification.setOnMouseClicked(__ ->
			{
				OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser/releases/latest");
				trayNotification.dismiss();
			});
			trayNotification.showAndWait();
		}
	}

	/**
	 * Creates files and folders that are necessary for the application to run
	 * properly and migrates old xml data.
	 */
	private static void createFolderStructure()
	{
		final File sampexFolder = new File(PathConstants.SAMPEX_PATH);
		sampexFolder.mkdirs();

		try
		{
			Files.copy(Client.class.getResourceAsStream("/com/msc/serverbrowser/tools/sampcmd.exe"), Paths
					.get(PathConstants.SAMP_CMD), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (final IOException exception)
		{
			Logging.log(Level.WARNING, "Error copying SAMP CMD to sampex folder.", exception);
		}

		final File clientCacheFolder = new File(PathConstants.CLIENT_CACHE);
		clientCacheFolder.mkdirs();
	}

	/**
	 * Compares the local version number to the one lying on the server. If an
	 * update is available
	 * the user will be asked if he wants to update.
	 */
	public void checkForUpdates()
	{
		Logging.log(Level.INFO, "Check for updates.");

		if (!updatingProperty.get())
		{
			mainController.progressProperty().set(0.0);
			mainController.setGlobalProgressText("Checking for updates");

			new Thread(() ->
			{
				updatingProperty.set(true);
				try
				{
					if (UpdateUtility.isUpToDate())
					{
						Logging.log(Level.INFO, "Client is up to date.");
					}
					else
					{
						Platform.runLater(() ->
						{
							mainController.progressProperty().set(0.1);
							mainController.setGlobalProgressText("Downloading update");
						});
						Logging.log(Level.INFO, "Downloading update.");
						downloadUpdate();
						Logging.log(Level.INFO, "Download of the updated has been finished.");
						Platform.runLater(() -> displayUpdateNotification());
					}
				}
				catch (final IOException exception)
				{

					Logging.log(Level.WARNING, "Couldn't check for newer version.", exception);
					Platform.runLater(() -> displayCantRetrieveUpdate());
				}

				Platform.runLater(() ->
				{
					mainController.setGlobalProgressText("");
					mainController.progressProperty().set(0);
				});
				updatingProperty.set(false);
			}).start();
		}
	}

	private static void displayUpdateNotification()
	{
		final TrayNotification trayNotification = new TrayNotificationBuilder().title(Client.lang.getString("updateInstalled"))
				.message(Client.lang.getString("clickToRestart"))
				.animation(Animations.SLIDE).build();

		trayNotification.setOnMouseClicked(__ ->
		{
			trayNotification.dismiss();
			finishUpdate();
		});
		trayNotification.showAndWait();
	}

	private static void displayCantRetrieveUpdate()
	{
		final TrayNotification trayNotification = new TrayNotificationBuilder().message(Client.lang.getString("couldntRetrieveUpdate"))
				.animation(Animations.POPUP)
				.type(NotificationTypeImplementations.ERROR).title(Client.lang.getString("updating")).build();

		trayNotification.setOnMouseClicked(clicked ->
		{
			OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser/releases/latest");
			trayNotification.dismiss();
		});

		trayNotification.showAndWait();
	}

	/**
	 * Adds nodes to the Clients bottom bar.
	 *
	 * @param nodes
	 *            the node that will be added
	 */
	public void addItemsToBottomBar(final Node... nodes)
	{
		mainController.addItemsToBottomBar(nodes);
	}

	/**
	 * Downloads the latest version and restarts the client.
	 */
	private void downloadUpdate()
	{
		try
		{
			final Optional<GHRelease> releaseOptional = UpdateUtility.getRelease();

			if (releaseOptional.isPresent())
			{
				final GHRelease release = releaseOptional.get();
				final String updateUrl = release.getAssets().get(0).getBrowserDownloadUrl();
				final URI url = new URI(updateUrl);
				FileUtility.downloadFile(url.toURL(), PathConstants.SAMPEX_TEMP_JAR, mainController
						.progressProperty(), (int) release.getAssets().get(0).getSize());
			}
		}
		catch (final IOException | URISyntaxException exception)
		{
			Logging.log(Level.SEVERE, "Couldn't retrieve update.", exception);
		}
	}

	private static void finishUpdate()
	{
		try
		{
			FileUtility.copyOverwrite(PathConstants.SAMPEX_TEMP_JAR, PathConstants.OWN_JAR.getPath());
			ClientPropertiesController.setProperty(Property.SHOW_CHANGELOG, true);
			Files.delete(Paths.get(PathConstants.SAMPEX_TEMP_JAR));
			selfRestart();
		}
		catch (final IOException exception)
		{
			Logging.log(Level.SEVERE, "Failed to update.", exception);
			final TrayNotification notification = new TrayNotificationBuilder().title(Client.lang.getString("applyingUpdate"))
					.message(Client.lang.getString("couldntApplyUpdate"))
					.type(NotificationTypeImplementations.ERROR).build();

			notification.setOnMouseClicked(__ ->
			{
				try
				{
					Desktop.getDesktop().open(new File(PathConstants.SAMPEX_LOG));
				}
				catch (final IOException couldntOpenlogfile)
				{
					Logging.log(Level.WARNING, "Error opening logfile.", couldntOpenlogfile);
				}
			});

		}
	}

	/**
	 * Restarts the application.
	 */
	private static void selfRestart()
	{
		if (!PathConstants.OWN_JAR.getName().endsWith(".jar"))
		{// The application wasn't run with a jar file, but in an ide
			return;
		}

		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final List<String> command = new ArrayList<>();

		command.add(javaBin);
		command.add("-jar");
		command.add(PathConstants.OWN_JAR.getPath());

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
	 * Programs entry point, it also does specific things when passed specific arguments.
	 *
	 * @param args
	 *            evaluated by {@link #readApplicationArguments}
	 * @throws IOException
	 *             if there was an error while loading language files
	 * @throws FileNotFoundException
	 *             if language files don't exist
	 */
	public static void main(final String[] args) throws FileNotFoundException, IOException
	{
		createFolderStructure();

		final Locale locale = new Locale(ClientPropertiesController.getPropertyAsString(Property.LANGUAGE));
		lang = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", locale);

		readApplicationArguments(args);
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> Logging.log(Level.SEVERE, "Uncaught exception in thread: " + t, e));
		Application.launch(args);
	}

	private static void readApplicationArguments(final String[] args)
	{
		final boolean containsDevelopmentFlag = ArrayUtility.contains(args, "-d");
		ClientPropertiesController.setProperty(Property.DEVELOPMENT, containsDevelopmentFlag);
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
	 * Loads a specific view.
	 *
	 * @param view
	 *            the view to be loaded
	 */
	public void loadView(final View view)
	{
		mainController.loadView(view);
	}

	/**
	 * Reloads the active view, if it is the given one.
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
