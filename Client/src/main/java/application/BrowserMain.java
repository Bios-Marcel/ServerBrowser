package application;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.server.RMISocketFactory;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;

import data.Favourites;
import data.PastUsernames;
import data.SQLDatabase;
import data.SampServer;
import gui.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import logging.Logging;
import util.FileUtility;
import util.windows.OSInfo;

public class BrowserMain extends Application
{
	public static final String	APPLICATION_NAME	= "SA-MP Client Extension";

	private static final String	VERSION				= "1.1.01";

	@Override
	public void start(final Stage primaryStage)
	{
		checkOperatingSystemCompatibility();

		checkVersion();

		prepareData();

		setRMISocketFactory();

		loadUI(primaryStage);
	}

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
			primaryStage.setScene(scene);
			primaryStage.getScene().getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
			primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icons/icon.png")));
			primaryStage.setTitle(APPLICATION_NAME);
			primaryStage.show();
			primaryStage.setMinWidth(primaryStage.getWidth());
			primaryStage.setMinHeight(primaryStage.getHeight());
			primaryStage.setIconified(false);
			primaryStage.setMaximized(false);
			controller.init();

			primaryStage.setOnCloseRequest(close ->
			{
				controller.onClose();
			});
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
			alert.setTitle("Launching Application");
			alert.setHeaderText("Operating System not supported");
			alert.setContentText("You seem to be not using windows, sorry, but this application does not support other systems than Windows.");
			alert.showAndWait();
			System.exit(0);
		}
	}

	/**
	 * Creates files and folders that are necessary for the application to run properly.
	 */
	private static void prepareData()
	{
		File file = new File(System.getProperty("user.home") + File.separator + "sampex");

		if (!file.exists())
		{
			file.mkdir();
		}

		SQLDatabase.init();

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
	 * Compares the local version number to the one lying on the server. If an update is
	 * availbable the user will be asked if he wants to update.
	 */
	private static void checkVersion()
	{
		try
		{
			final URI url = new URI("http://ts3.das-chat.xyz/sampversion/launcher/version.info");
			try (final Scanner s = new Scanner(url.toURL().openStream()))
			{
				if (!VERSION.equals(s.nextLine()))
				{
					final Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Launching Application");
					alert.setHeaderText("Update required");
					alert.setContentText("The launcher needs an update. Not updating the client might lead to problems. Click 'OK' to update and 'Cancel' to not update.");

					final Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == ButtonType.OK)
					{
						updateLauncher();
					}
				}
			}
		}
		catch (final Exception e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't retrieve Update / Update Info.", e);
		}
	}

	/**
	 * Downloads the latest version and restarts the client.
	 */
	private static void updateLauncher()
	{
		try
		{
			final URI url = new URI("http://ts3.das-chat.xyz/sampversion/launcher/launcher.jar");
			FileUtility.downloadUsingNIO(url.toString(), getOwnJarFile().getPath().toString());
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
		{
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

	private static void setRMISocketFactory()
	{
		try
		{
			RMISocketFactory.setSocketFactory(new RMISocketFactory()
			{
				@Override
				public Socket createSocket(final String host, final int port) throws IOException
				{
					final Socket socket = new Socket();
					socket.setSoTimeout(1500);
					socket.setSoLinger(false, 0);
					socket.connect(new InetSocketAddress(host, port), 1500);
					return socket;
				}

				@Override
				public ServerSocket createServerSocket(final int port) throws IOException
				{
					return new ServerSocket(port);
				}
			});
		}
		catch (final IOException e)
		{
			Logging.logger.log(Level.WARNING, "Couldb't set custom RMI Socket Factory.", e);
		}
	}

	public static void main(final String[] args)
	{
		launch(args);
	}
}
