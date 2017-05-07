package application;

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
import java.util.Objects;
import java.util.logging.Level;

import data.Favourites;
import data.PastUsernames;
import data.properties.ClientProperties;
import data.properties.PropertyIds;
import data.rmi.CustomRMIClientSocketFactory;
import entities.SampServer;
import gui.controllers.implementations.MainController;
import interfaces.DataServiceInterface;
import interfaces.UpdateServiceInterface;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logging.Logging;
import util.FileUtility;
import util.Hashing;
import util.windows.OSInfo;

public class Client extends Application
{
	private static final Image applicationIcon = new Image(Client.class.getResourceAsStream("/icons/icon.png"));

	public static final String APPLICATION_NAME = "SA-MP Client Extension";

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

		checkOperatingSystemCompatibility();

		initClient();

		establishConnection();

		loadUI(primaryStage);

		checkVersion();
	}

	/*
	 * + Establishes the connection with the rmi server.
	 */
	private void establishConnection()
	{
		try
		{
			registry = LocateRegistry.getRegistry("164.132.193.101", 1099, new CustomRMIClientSocketFactory());
			remoteDataService = (DataServiceInterface) registry.lookup(DataServiceInterface.INTERFACE_NAME);
			remoteUpdateService = (UpdateServiceInterface) registry.lookup(UpdateServiceInterface.INTERFACE_NAME);
		}
		catch (RemoteException | NotBoundException e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't connect to RMI Server.", e);
			displayNoConnectionDialog();
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
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/views/Main.fxml"));
		final MainController controller = new MainController();
		loader.setController(controller);
		try
		{
			final Parent root = loader.load();
			final Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/views/stylesheets/mainStyle.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.getIcons().add(applicationIcon);
			primaryStage.setTitle(APPLICATION_NAME);
			primaryStage.show();
			primaryStage.setMinWidth(primaryStage.getWidth());
			primaryStage.setMinHeight(primaryStage.getHeight());
			primaryStage.setMaximized(ClientProperties.getPropertyAsBoolean(PropertyIds.MAXIMIZED));
			primaryStage.setFullScreen(ClientProperties.getPropertyAsBoolean(PropertyIds.FULLSCREEN));
			primaryStage.setOnCloseRequest(close ->
			{
				controller.onClose();
				ClientProperties.setProperty(PropertyIds.MAXIMIZED, primaryStage.isMaximized());
				ClientProperties.setProperty(PropertyIds.FULLSCREEN, primaryStage.isFullScreen());
			});

			stage = primaryStage;
		}
		catch (final Exception e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't load UI", e);
			System.exit(0);
		}
	}

	/**
	 * Checks if the operating system is windows, if not, the application will shutdown.
	 */
	private void checkOperatingSystemCompatibility()
	{
		if (!OSInfo.isWindows())
		{
			final Alert alert = new Alert(AlertType.WARNING);
			setAlertIcon(alert);
			alert.setTitle("Launching Application");
			alert.setHeaderText("Operating System not supported");
			alert.setContentText("You seem to be not using windows, sorry, but this application does not support other systems than Windows.");
			alert.showAndWait();
			System.exit(0);
		}
	}

	public void displayNoConnectionDialog()
	{
		final Alert alert = new Alert(AlertType.ERROR);
		setAlertIcon(alert);
		alert.initOwner(stage);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setTitle("Connecting to server");
		alert.setHeaderText("Server connection couldd not be established");
		alert.setContentText("The server connection doesn't seeem to be established, try again later, for more information check the log files.");
		alert.showAndWait();
	}

	private void setAlertIcon(final Alert alert)
	{
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(applicationIcon);
	}

	/**
	 * Creates files and folders that are necessary for the application to run properly.
	 */
	private void initClient()
	{
		File file = new File(System.getProperty("user.home") + File.separator + "sampex");

		if (!file.exists())
		{
			file.mkdir();
		}

		file = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "favourites.xml");

		// Migration from XML to SQLLite
		if (file.exists())
		{
			for (final SampServer server : Favourites.getFavouritesFromXML())
			{
				Favourites.addServerToFavourites(server);
			}
			file.delete();
		}

		file = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "pastusernames.xml");

		if (file.exists())
		{
			for (final String username : PastUsernames.getPastUsernamesFromXML())
			{
				PastUsernames.addPastUsername(username);
			}
			file.delete();
		}
	}

	/**
	 * Compares the local version number to the one lying on the server. If an update is availbable
	 * the user will be asked if he wants to update.
	 */
	private void checkVersion()
	{
		if (Objects.nonNull(remoteDataService))
		{
			try
			{
				final String localVersion = Hashing.verifyChecksum(getOwnJarFile().toString());
				final String remoteVersion = remoteDataService.getLatestVersionChecksum();

				if (!localVersion.equals(remoteVersion))
				{
					final Alert alert = new Alert(AlertType.CONFIRMATION);
					setAlertIcon(alert);
					alert.setTitle("Launching Application");
					alert.setHeaderText("Update required");
					alert.setContentText("The launcher needs an update. Not updating the client might lead to problems. Click 'OK' to update and 'Cancel' to not update.");

					alert.showAndWait().ifPresent(result ->
					{
						if (result == ButtonType.OK)
						{
							updateLauncher();
						}
					});
				}
			}
			catch (final FileNotFoundException notFound)
			{
				Logging.logger.log(Level.INFO, "Couldn't retrieve Update Info, the client is most likely being run in an ide.");
			}
			catch (final NoSuchAlgorithmException nonExistentAlgorithm)
			{
				Logging.logger.log(Level.INFO, "The used Hashing-Algorithm doesan't exist.", nonExistentAlgorithm);
			}
			catch (final IOException updateException)
			{
				Logging.logger.log(Level.SEVERE, "Couldn't retrieve Update Info.", updateException);
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
			selfRestart();

		}
		catch (final IOException | URISyntaxException e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't retrieve update.", e);
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
		catch (final IOException e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't selfrestart.", e);
		}
	}

	public static void main(final String[] args)
	{
		Application.launch(args);
	}
}
