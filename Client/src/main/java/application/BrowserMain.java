package application;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.server.RMISocketFactory;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;

import controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import logging.Logging;
import util.FileUtility;
import windows.OSInfo;

public class BrowserMain extends Application
{
	public static final String	APPLICATION_NAME	= "SA-MP Client Extension";

	private static final String	VERSION				= "1.0.11";

	@Override
	public void start(Stage primaryStage)
	{
		if (!OSInfo.isWindows())
		{
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Launching Application");
			alert.setHeaderText("Operating System not supported");
			alert.setContentText("You seem to be not using windows, sorry, but this application does not support other systems than Windows.");
			alert.showAndWait();
			Runtime.getRuntime().exit(0);
		}

		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/views/Main.fxml"));
		MainController controller = new MainController();
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
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void createFilesAndFolders()
	{
		File file = new File(System.getProperty("user.home") + File.separator + "sampex");

		if (!file.exists())
		{
			file.mkdir();
		}

		file = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "favourites.xml");

		if (!file.exists())
		{
			try
			{
				file.createNewFile();
				Files.write(Paths.get(file.getPath()), new String("<servers/>").getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		file = new File(System.getProperty("user.home") + File.separator + "sampex" + File.separator + "pastusernames.xml");

		if (!file.exists())
		{
			try
			{
				file.createNewFile();
				Files.write(Paths.get(file.getPath()), new String("<usernames/>").getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private static void checkVersion()
	{
		try
		{
			URI url = new URI("http://ts3.das-chat.xyz/sampversion/launcher/version.info");
			Scanner s = new Scanner(url.toURL().openStream());
			String versionLatest = s.nextLine();
			if (!versionLatest.equals(VERSION))
			{
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Launching Application");
				alert.setHeaderText("Update required");
				alert.setContentText("The launcher will now update and restart itself, don't do anything.");
				alert.show();
				updateLauncher();
			}
			s.close();
		}
		catch (Exception e)
		{
			System.out.println("Couldn't retrieve Update / Update Info.");
		}
	}

	private static void updateLauncher()
	{
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		try
		{
			File f = new File(System.getProperty("java.class.path"));
			File currentJar = f.getAbsoluteFile();
			/* is it a jar file? */
			if (!currentJar.getName().endsWith(".jar"))
			{
				return;
			}

			/* Build command: java -jar application.jar */
			final ArrayList<String> command = new ArrayList<>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());

			final ProcessBuilder builder = new ProcessBuilder(command);

			try
			{
				URI url = new URI("http://ts3.das-chat.xyz/sampversion/launcher/launcher.jar");
				FileUtility.downloadUsingNIO(url.toString(), currentJar.getPath().toString());
				builder.start();
				System.exit(0);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		catch (URISyntaxException e1)
		{
			e1.printStackTrace();
		}

	}

	private static void setRMISocketFactory()
	{
		try
		{
			RMISocketFactory.setSocketFactory(new RMISocketFactory()
			{
				@Override
				public Socket createSocket(String host, int port) throws IOException
				{
					Socket socket = new Socket();
					socket.setSoTimeout(1500);
					socket.setSoLinger(false, 0);
					socket.connect(new InetSocketAddress(host, port), 1500);
					return socket;
				}

				@Override
				public ServerSocket createServerSocket(int port) throws IOException
				{
					return new ServerSocket(port);
				}
			});
		}
		catch (IOException e)
		{
			Logging.logger.log(Level.WARNING, "Couldb't set custom RMI Socket Factory.", e);
		}
	}

	public static void main(String[] args)
	{
		checkVersion();

		createFilesAndFolders();

		setRMISocketFactory();

		launch(args);
	}
}
