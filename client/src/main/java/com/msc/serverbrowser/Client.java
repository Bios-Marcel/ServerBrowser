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
import com.msc.serverbrowser.gui.controllers.implementations.SettingsController;
import com.msc.serverbrowser.gui.views.MainView;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.UpdateUtility;
import com.msc.serverbrowser.util.basic.ArrayUtility;
import com.msc.serverbrowser.util.basic.FileUtility;
import com.msc.serverbrowser.util.basic.OptionalUtility;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * This is the main class of the client.
 *
 * @author Marcel
 * @since 02.07.2017
 */
public final class Client extends Application {
	private static boolean developmentMode = false;

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

	private static Client	instance;
	private Stage			stage;
	private MainController	mainController;

	/**
	 * RessourceBundle which contains all the localized strings.
	 */
	private static ResourceBundle languageBundle;

	/**
	 * This property that indicates if an update check / download progress is ongoing.
	 */
	public final BooleanProperty updateOngoingProperty = new SimpleBooleanProperty(false);

	/**
	 * @return the clients singleton instance
	 */
	public static Client getInstance() {
		return instance;
	}

	@Override
	public void start(final Stage primaryStage) {
		instance = this;
		loadUI(primaryStage);

		// Only update if not in development mode
		if (!Client.isDevelopmentModeActivated()) {
			if (new File(PathConstants.SAMPEX_TEMP_JAR).exists()) {
				finishUpdate();
			}
			else if (ClientPropertiesController.getPropertyAsBoolean(Property.AUTOMTAIC_UPDATES)) {
				checkForUpdates();
			}
		}
	}

	/**
	 * Sets the current {@link Client} stage as the clients owner.
	 *
	 * @param alert the alert to set the owner in
	 */
	public static void insertAlertOwner(final Dialog<?> alert) {
		alert.initOwner(getInstance().stage);
	}

	private MainController loadUIAndGetController() {
		final MainView mainView = new MainView();
		mainController = new MainController(mainView);
		mainController.initialize();
		final Scene scene = new Scene(mainView.getRootPane());
		stage.setScene(scene);

		applyTheme();
		return mainController;
	}

	/**
	 * Deletes the scenes current stylesheets and applies either the dark theme or the default
	 * theme.
	 */
	public void applyTheme() {
		// Retrieving the current scene, assuring it ain't null.
		final Scene scene = Objects.requireNonNull(stage.getScene());

		scene.getStylesheets().clear();
		scene.getStylesheets().add(PathConstants.STYLESHEET_PATH + "mainStyleGeneral.css");

		if (ClientPropertiesController.getPropertyAsBoolean(Property.USE_DARK_THEME)) {
			scene.getStylesheets().add(PathConstants.STYLESHEET_PATH + "mainStyleDark.css");
			scene.getStylesheets().add("/styles/trayDark.css");
		}
		else {
			scene.getStylesheets().add(PathConstants.STYLESHEET_PATH + "mainStyleLight.css");
			scene.getStylesheets().add("/styles/defaultStyle.css");
		}
	}

	/**
	 * Loads the main UI.
	 *
	 * @param primaryStage the stage to use for displaying the UI
	 */
	private void loadUI(final Stage primaryStage) {
		stage = primaryStage;

		final MainController controller = loadUIAndGetController();

		TrayNotificationBuilder.setDefaultOwner(stage);

		primaryStage.getIcons().add(APPLICATION_ICON);
		primaryStage.setResizable(true);
		primaryStage.setMaximized(ClientPropertiesController.getPropertyAsBoolean(Property.MAXIMIZED));

		primaryStage.setOnCloseRequest(close -> {
			controller.onClose();
			ClientPropertiesController.setProperty(Property.MAXIMIZED, primaryStage.isMaximized());
		});

		primaryStage.show();

		if (ClientPropertiesController.getPropertyAsBoolean(Property.SHOW_CHANGELOG)
				&& ClientPropertiesController.getPropertyAsBoolean(Property.CHANGELOG_ENABLED)) {

			// Since the changelog has been shown after this update, it shall not be shown again,
			// unless there is another update
			ClientPropertiesController.setProperty(Property.SHOW_CHANGELOG, false);

			final TrayNotification trayNotification = new TrayNotificationBuilder()
					.type(NotificationTypeImplementations.INFORMATION)
					.title(Client.getString("updated"))
					.message(Client.getString("clickForChangelog"))
					.animation(Animations.SLIDE).build();

			trayNotification.setOnMouseClicked(__ -> {
				OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser/releases/latest");
				trayNotification.dismiss();
			});
			trayNotification.showAndWait();
		}
	}

	/**
	 * Creates files and folders that are necessary for the application to run properly and migrates
	 * old xml data.
	 */
	private static void createFolderStructure() {
		final File sampexFolder = new File(PathConstants.SAMPEX_PATH);
		sampexFolder.mkdirs();

		try {
			Files.copy(Client.class.getResourceAsStream("/com/msc/serverbrowser/tools/sampcmd.exe"), Paths
					.get(PathConstants.SAMP_CMD), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (final IOException exception) {
			Logging.warn("Error copying SAMP CMD to sampex folder.", exception);
		}

		final File clientCacheFolder = new File(PathConstants.CLIENT_CACHE);
		clientCacheFolder.mkdirs();
	}

	/**
	 * Compares the local version number to the one lying on the server. If an update is available
	 * the user will be asked if he wants to update.
	 */
	public void checkForUpdates() {
		Logging.info("Checking for updates.");

		if (updateOngoingProperty.get()) {
			// If an update is ongoing already, then we won't start another.
			return;
		}

		mainController.progressProperty().set(0.0);
		mainController.setGlobalProgressText(Client.getString("checkingForUpdates"));

		new Thread(() -> {
			updateOngoingProperty.set(true);
			try {
				if (UpdateUtility.isUpToDate()) {
					Logging.info("Client is up to date.");
				}
				else {
					Platform.runLater(() -> {
						mainController.progressProperty().set(0.1);
						mainController.setGlobalProgressText(Client.getString("downloadingUpdate"));
					});
					Logging.info("Downloading update.");
					downloadUpdate();
					Logging.info("Download of the updated has been finished.");
					Platform.runLater(() -> displayUpdateNotification());
				}
			}
			catch (final IOException exception) {

				Logging.warn("Couldn't check for newer version.", exception);
				Platform.runLater(() -> displayCantRetrieveUpdate());
			}

			Platform.runLater(() -> {
				mainController.setGlobalProgressText("");
				mainController.progressProperty().set(0);
			});
			updateOngoingProperty.set(false);
		}).start();
	}

	private static void displayUpdateNotification() {
		final TrayNotification trayNotification = new TrayNotificationBuilder().title(Client.getString("updateInstalled"))
				.message(Client.getString("clickToRestart"))
				.animation(Animations.SLIDE).build();

		trayNotification.setOnMouseClicked(__ -> {
			trayNotification.dismiss();
			finishUpdate();
		});
		trayNotification.showAndWait();
	}

	private static void displayCantRetrieveUpdate() {
		final TrayNotification trayNotification = new TrayNotificationBuilder().message(Client.getString("couldntRetrieveUpdate"))
				.animation(Animations.POPUP)
				.type(NotificationTypeImplementations.ERROR).title(Client.getString("updating")).build();

		trayNotification.setOnMouseClicked(clicked -> {
			OSUtility.browse("https://github.com/Bios-Marcel/ServerBrowser/releases/latest");
			trayNotification.dismiss();
		});

		trayNotification.showAndWait();
	}

	/**
	 * Adds nodes to the Clients bottom bar.
	 *
	 * @param nodes the node that will be added
	 */
	public void addItemsToBottomBar(final Node... nodes) {
		mainController.addItemsToBottomBar(nodes);
	}

	/**
	 * Downloads the latest version and restarts the client.
	 */
	private void downloadUpdate() {
		try {
			final Optional<GHRelease> releaseOptional = UpdateUtility.getRelease();

			if (releaseOptional.isPresent()) {
				final GHRelease release = releaseOptional.get();
				final String updateUrl = release.getAssets().get(0).getBrowserDownloadUrl();
				final URI url = new URI(updateUrl);
				FileUtility.downloadFile(url.toURL(), PathConstants.SAMPEX_TEMP_JAR, mainController
						.progressProperty(), (int) release.getAssets().get(0).getSize());
			}
		}
		catch (final IOException | URISyntaxException exception) {
			Logging.error("Couldn't retrieve update.", exception);
		}
	}

	private static void finishUpdate() {
		try {
			FileUtility.copyOverwrite(PathConstants.SAMPEX_TEMP_JAR, PathConstants.OWN_JAR.getPath());
			ClientPropertiesController.setProperty(Property.SHOW_CHANGELOG, true);
			Files.delete(Paths.get(PathConstants.SAMPEX_TEMP_JAR));
			selfRestart();
		}
		catch (final IOException exception) {
			Logging.error("Failed to update.", exception);
			final TrayNotification notification = new TrayNotificationBuilder().title(Client.getString("applyingUpdate"))
					.message(Client.getString("couldntApplyUpdate"))
					.type(NotificationTypeImplementations.ERROR).build();

			notification.setOnMouseClicked(__ -> {
				try {
					Desktop.getDesktop().open(new File(PathConstants.SAMPEX_LOG));
				}
				catch (final IOException couldntOpenlogfile) {
					Logging.warn("Error opening logfile.", couldntOpenlogfile);
				}
			});

		}
	}

	/**
	 * <p>
	 * TODO BROKEN WHEN STARTED WITH INSTALLER.
	 * </p>
	 * Restarts the application.
	 */
	private static void selfRestart() {
		if (!PathConstants.OWN_JAR.getName().endsWith(".jar")) {
			// The application wasn't run with a jar file, but in an ide.
			return;
		}

		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final List<String> command = new ArrayList<>();

		command.add(javaBin);
		command.add("-jar");
		command.add(PathConstants.OWN_JAR.getPath());

		try {
			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
			Platform.exit();
		}
		catch (final IOException exception) {
			Logging.error("Couldn't selfrestart.", exception);
		}
	}

	/**
	 * Programs entry point, it also does specific things when passed specific arguments.
	 *
	 * @param args evaluated by {@link #readApplicationArguments}
	 * @throws IOException if there was an error while loading language files
	 * @throws FileNotFoundException if language files don't exist
	 */
	public static void main(final String[] args) throws FileNotFoundException, IOException {
		createFolderStructure();
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> Logging.error("Uncaught exception in thread: " + t, e));
		initLanguageFiles();
		readApplicationArguments(args);
		Application.launch(args);
	}

	/**
	 * Reads the ressource bundle for the currently chosen language.
	 */
	public static void initLanguageFiles() {
		final Locale locale = new Locale(ClientPropertiesController.getPropertyAsString(Property.LANGUAGE));
		languageBundle = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", locale);
	}

	private static void readApplicationArguments(final String[] args) {
		developmentMode = ArrayUtility.contains(args, "-d");

	}

	/**
	 * @return true if the development mode is activated, otherwise false
	 */
	public static boolean isDevelopmentModeActivated() {
		return developmentMode;
	}

	/**
	 * Sets the Applications title.
	 *
	 * @param title the title to set
	 */
	public void setTitle(final String title) {
		stage.setTitle(title);
	}

	/**
	 * Loads a specific view.
	 *
	 * @param view the view to be loaded
	 */
	public void loadView(final View view) {
		mainController.loadView(view);
	}

	/**
	 * Reloads the active view, if it is the given one.
	 *
	 * @param view the view to reload
	 */
	public void reloadViewIfLoaded(final View view) {
		if (mainController.getActiveView() == view) {
			mainController.reloadView();
		}
	}

	/**
	 * Loads the {@link View#SETTINGS settings view} and selects the {@link TextField} which
	 * contains the SA-MP / GTA path.
	 */
	public void selectSampPathTextField() {
		if (mainController.getActiveView() != View.SETTINGS) {
			loadView(View.SETTINGS);
		}
		mainController.getSettingsController().ifPresent(SettingsController::selectSampPathTextField);
	}

	/**
	 * @return an {@link Optional} of the current {@link SettingsController}.
	 */
	public Optional<SettingsController> getSettingsController() {
		return mainController.getSettingsController();
	}

	/**
	 * @return {@link #languageBundle the resourcebundle containing the currently loaded language}
	 */
	public static ResourceBundle getLangaugeResourceBundle() {
		return languageBundle;
	}

	/**
	 * @param key they key to retrieve the value for
	 * @return the value for the given key, using the {@link #languageBundle} resource bundle
	 */
	public static String getString(final String key) {
		return OptionalUtility.attempt(() -> languageBundle.getString(key)).orElse("Invalid Key");
	}
}
