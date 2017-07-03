package com.msc.serverbrowser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

import com.msc.sampbrowser.entities.SampServer;
import com.msc.sampbrowser.interfaces.DataServiceInterface;
import com.msc.sampbrowser.interfaces.UpdateServiceInterface;
import com.msc.sampbrowser.util.Hashing;
import com.msc.serverbrowser.constants.Paths;
import com.msc.serverbrowser.data.Favourites;
import com.msc.serverbrowser.data.PastUsernames;
import com.msc.serverbrowser.data.properties.ClientProperties;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.data.rmi.CustomRMIClientSocketFactory;
import com.msc.serverbrowser.gui.controllers.implementations.MainController;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.FileUtility;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Client extends Application
{
	/**
	 * Default public IP, can be changed on startup using <code>-s</code> / <code>-server</code>
	 * followed by a domain, IP or hostname.
	 */
	private static String serverToConnectTo = "ts3.sa-mpservers.com";

	/**
	 * Application icon that can be used everywhere where necessary.
	 */
	public static final Image	APPLICATION_ICON	= new Image(Client.class.getResourceAsStream("/com/msc/serverbrowser/icons/icon.png"));
	public static final String	APPLICATION_NAME	= "SA-MP Client Extension";

	public static Registry registry;

	public static DataServiceInterface		remoteDataService;
	public static UpdateServiceInterface	remoteUpdateService;

	private Stage stage;

	private static Client instance;

	public static Client getInstance()
	{
		return instance;
	}

	@Override
	public void start(final Stage primaryStage)
	{
		instance = this;
		initClient();
		establishConnection();
		loadUI(primaryStage);

		new Thread(() -> checkVersion()).start();
	}

	/*
	 * + Establishes the connection with the rmi server.
	 */
	public void establishConnection()
	{
		if (Objects.isNull(remoteDataService) || Objects.isNull(remoteUpdateService))
		{
			try
			{
				registry = LocateRegistry.getRegistry(serverToConnectTo, 1099, new CustomRMIClientSocketFactory());
				remoteDataService = (DataServiceInterface) registry.lookup(DataServiceInterface.INTERFACE_NAME);
				remoteUpdateService = (UpdateServiceInterface) registry.lookup(UpdateServiceInterface.INTERFACE_NAME);

				if (ClientProperties.getPropertyAsBoolean(Property.NOTIFY_SERVER_ON_STARTUP))
				{
					remoteDataService.tellServerThatYouUseTheApp(Locale.getDefault().toString());
				}
			}
			catch (RemoteException | NotBoundException exception)
			{
				Logging.logger().log(Level.SEVERE, "Couldn't connect to RMI Server.", exception);
				Platform.runLater(() -> displayNoConnectionDialog());
			}
		}
	}

	/**
	 * @return {@link #stage}
	 */
	public Stage getStage()
	{
		return stage;
	}

	public void loadUI()
	{
		loadUIAndGetController();
	}

	private ViewController loadUIAndGetController()
	{
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/com/msc/serverbrowser/views/Main.fxml"));
		final MainController controller = new MainController();
		loader.setController(controller);
		try
		{
			final Parent root = loader.load();
			final Scene scene = new Scene(root);

			if (ClientProperties.getPropertyAsBoolean(Property.USE_DARK_THEME))
			{
				scene.getStylesheets().add(getClass().getResource("/com/msc/serverbrowser/views/stylesheets/mainStyleDark.css").toExternalForm());
			}
			else
			{
				scene.getStylesheets().add(getClass().getResource("/com/msc/serverbrowser/views/stylesheets/mainStyle.css").toExternalForm());
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

		final ViewController controller = loadUIAndGetController();

		primaryStage.getIcons().add(APPLICATION_ICON);
		primaryStage.setTitle(APPLICATION_NAME);
		primaryStage.setMaximized(ClientProperties.getPropertyAsBoolean(Property.MAXIMIZED));
		primaryStage.setFullScreen(ClientProperties.getPropertyAsBoolean(Property.FULLSCREEN));
		// Usually true by default, but on unix systems that use openjfx, it is false by default
		primaryStage.setResizable(true);

		primaryStage.setOnCloseRequest(close ->
		{
			controller.onClose();
			ClientProperties.setProperty(Property.MAXIMIZED, primaryStage.isMaximized());
			ClientProperties.setProperty(Property.FULLSCREEN, primaryStage.isFullScreen());
		});

		primaryStage.show();

		primaryStage.setMinWidth(800);
		primaryStage.setMinHeight(400);

		if (ClientProperties.getPropertyAsBoolean(Property.SHOW_CHANGELOG))
		{
			final Alert alert = new Alert(AlertType.INFORMATION);
			setupDialog(alert);
			alert.setTitle(APPLICATION_NAME + "- Changelog");
			alert.setHeaderText("Your client has been updated | Changelog");

			final StringBuilder updateText = new StringBuilder();
			updateText.append("- New Dark Theme (Can be activated on settings page)");
			updateText.append(System.lineSeparator());
			updateText.append("- Bug Fix where adding servers that can't be reached leaded to nothing happening");
			updateText.append(System.lineSeparator());
			updateText.append("- Refactoring of Layout");

			alert.setContentText(updateText.toString());
			alert.show();
			ClientProperties.setProperty(Property.SHOW_CHANGELOG, false);
		}
	}

	public void displayNoConnectionDialog()
	{
		final Alert alert = new Alert(AlertType.ERROR);
		setupDialog(alert);
		alert.setTitle("Connecting to server");
		alert.setHeaderText("Server connection could not be established");
		alert.setContentText("The server connection doesn't seeem to be established, try again later, for more information check the log files.");
		alert.showAndWait();
	}

	// TODO(MSC) Mit DialogBuilder oder so ersetzen
	public static void setupDialog(final Alert alert)
	{
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(APPLICATION_ICON);
		final ObservableList<String> clientStylesheets = getInstance().getStage().getScene().getStylesheets();
		alert.getDialogPane().getStylesheets().addAll(clientStylesheets);
		alert.initOwner(getInstance().getStage());
		alert.initModality(Modality.APPLICATION_MODAL);
	}

	/**
	 * Creates files and folders that are necessary for the application to run properly and migrates
	 * old xml data.
	 */
	private void initClient()
	{
		final File sampexFolder = new File(Paths.SAMPEX_PATH);

		if (!sampexFolder.exists())
		{
			sampexFolder.mkdir();
		}

		final File oldFavouritesFile = new File(Paths.SAMPEX_PATH + File.separator + "favourites.xml");

		// Migration from XML to SQLLite
		if (oldFavouritesFile.exists())
		{
			for (final SampServer server : Favourites.getFavouritesFromXML())
			{
				Favourites.addServerToFavourites(server);
			}
			oldFavouritesFile.delete();
		}

		final File oldPastUsernamesFile = new File(Paths.SAMPEX_PATH + File.separator + "pastusernames.xml");

		if (oldPastUsernamesFile.exists())
		{
			for (final String username : PastUsernames.getPastUsernamesFromXML())
			{
				PastUsernames.addPastUsername(username);
			}
			oldPastUsernamesFile.delete();
		}
	}

	/**
	 * Compares the local version number to the one lying on the server. If an update is availbable
	 * the user will be asked if he wants to update.
	 */
	private void checkVersion()
	{
		if (Objects.nonNull(remoteDataService))
		{// Connection with server was not sucessful
			try
			{
				final String localVersion = Hashing.verifyChecksum(getOwnJarFile().toString());
				final String remoteVersion = remoteUpdateService.getLatestVersionChecksum();

				if (!localVersion.equals(remoteVersion))
				{
					Platform.runLater(() ->
					{
						final Alert alert = new Alert(AlertType.CONFIRMATION);
						setupDialog(alert);
						alert.setTitle("Launching Application");
						alert.setHeaderText("Update required");
						alert.setContentText("The launcher needs an update. Not updating the client might lead to problems. Click 'OK' to update and 'Cancel' to not update.");

						alert.showAndWait()
								.filter(ButtonType.OK::equals)
								.ifPresent(__ -> updateLauncher());
					});
				}
			}
			catch (final FileNotFoundException notFound)
			{
				Logging.logger().log(Level.INFO, "Couldn't retrieve Update Info, the client is most likely being run in an ide.", notFound);
			}
			catch (final NoSuchAlgorithmException nonExistentAlgorithm)
			{
				Logging.logger().log(Level.INFO, "The used Hashing-Algorithm doesan't exist.", nonExistentAlgorithm);
			}
			catch (final IOException updateException)
			{
				Logging.logger().log(Level.SEVERE, "Couldn't retrieve Update Info.", updateException);
			}
		}
	}

	/**
	 * Downloads the latest version and restarts the client.
	 */
	private void updateLauncher()
	{
		try
		{
			final URI url = new URI(remoteUpdateService.getLatestVersionURL());
			FileUtility.downloadFile(url.toString(), getOwnJarFile().getPath().toString());
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
	private File getOwnJarFile()
	{
		return new File(System.getProperty("java.class.path")).getAbsoluteFile();
	}

	/**
	 * Restarts the application.
	 */
	private void selfRestart()
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

	public static void main(final String[] args)
	{
		if (args.length >= 2)
		{
			for (int i = 0; i < args.length; i++)
			{
				final String arg = args[i];
				if (arg.equals("-s") || arg.equals("-server"))
				{
					serverToConnectTo = args[i + 1];
				}
			}
		}

		Application.launch(args);
	}
}
