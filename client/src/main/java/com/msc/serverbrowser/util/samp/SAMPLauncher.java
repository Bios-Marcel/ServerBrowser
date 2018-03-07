package com.msc.serverbrowser.util.samp;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.ServerConfig;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.ServerUtility;
import com.msc.serverbrowser.util.basic.OptionalUtility;
import com.msc.serverbrowser.util.windows.OSUtility;

/**
 * This classes purpose is solely to launch GTA and connect to a server.
 *
 * @author marcel
 * @since Feb 28, 2018
 */
public class SAMPLauncher {

	/**
	 * Tries connecting to a SA-MP server.
	 * <p>
	 * Also does:
	 * <ul>
	 * <li>kill GTA process</li>
	 * <li>Check if GTA can be found and display an error otherwise</li>
	 * <li>use multiple methods for connecting, in case the best one doesn't work</li>
	 * </ul>
	 * </p>
	 *
	 * @param address the IP-address / domain for the server
	 * @param port the port for the server
	 * @param serverPassword password to be used for connect to the server
	 * @return true if the connection was successful, otherwise false
	 */
	public static boolean connect(final String address, final Integer port, final String serverPassword) {
		if (ClientPropertiesController.getPropertyAsBoolean(Property.ALLOW_CLOSE_GTA)) {
			GTAController.killGTA();
		}

		final Optional<String> gtaPathOptional = GTAController.getGtaPath();

		if (!gtaPathOptional.isPresent()) {
			GTAController.displayCantLocateGTANotification();
			return false;
		}

		final String gtaPath = gtaPathOptional.get();
		final String ipAddress;

		if (ServerUtility.isValidIPAddress(address)) {
			ipAddress = address;
		}
		else {
			ipAddress = OptionalUtility.attempt(() -> InetAddress.getByName(address).getHostAddress()).orElse(address);
		}

		if (connectInternal(gtaPath, ipAddress, port, serverPassword)) {
			ServerConfig.setLastTimeJoinedForServer(address, port, Instant.now().toEpochMilli());
			return true;
		}

		GTAController.showCantConnectToServerError();
		return false;
	}

	private static boolean connectInternal(final String gtaPath, final String address, final Integer port, final String serverPassword) {

		if (connectUsingDLLInjection(gtaPath, address, port, serverPassword)) {
			return true;
		}

		if (connectUsingExecuteable(gtaPath, address, port, serverPassword)) {
			return true;
		}

		if (Objects.isNull(serverPassword) || serverPassword.isEmpty()) {
			return connectUsingWindowsProtocol(address, port);
		}

		Logging.warn("Couldn't connect to server using the protocol, since a password was used.");
		return false;
	}

	private static boolean connectUsingDLLInjection(final String gtaPath, final String address, final Integer port, final String serverPassword) {
		final ProcessBuilder builder = new ProcessBuilder();
		final List<String> arguments = buildLaunchingArguments(address, port, Optional.ofNullable(serverPassword));
		builder.command(arguments);
		builder.directory(new File(gtaPath));

		try {
			builder.start();
			return true;
		}
		catch (final Exception exception) {
			Logging.warn("Error using sampcmd.exe", exception);
		}

		return false;
	}

	private static List<String> buildLaunchingArguments(final String address, final Integer port, final Optional<String> passwordOptional) {
		final List<String> arguments = new ArrayList<>();
		arguments.add(PathConstants.SAMP_CMD);
		arguments.add("-c");
		arguments.add("-h");
		arguments.add(address);
		arguments.add("-p");
		arguments.add(port.toString());
		arguments.add("-n");

		// At this point, it should be no problem to ask for the username
		arguments.add(GTAController.retrieveUsernameFromRegistry().get());

		passwordOptional.ifPresent(password -> {
			if (!password.isEmpty()) {
				arguments.add("z");
				arguments.add(password);
			}
		});

		return arguments;
	}

	private static boolean connectUsingExecuteable(final String gtaPath, final String address, final Integer port, final String password) {
		final String addressAndPort = address + ":" + port.toString();

		try {
			Logging.info("Connecting using executeable.");
			final ProcessBuilder builder = new ProcessBuilder(gtaPath + File.separator + "samp.exe ", addressAndPort, password);
			builder.directory(new File(gtaPath));
			builder.start();
			return true;
		}
		catch (final IOException exception) {
			Logging.warn("Error connecting to server " + addressAndPort + " by manually calling the executeable", exception);
			return false;
		}
	}

	/**
	 * Connects to the given server (IP and Port) using an empty (no) password. Other than
	 * {@link GTAController#connectToServer(String)} and
	 * {@link GTAController#connectToServer(String, String)}, this method uses the
	 * <code>samp://</code> protocol to connect to make the samp launcher connect to the server.
	 *
	 * @param ipAndPort the server to connect to
	 * @return true if it was most likely successful
	 */
	private static boolean connectUsingWindowsProtocol(final String address, final Integer port) {
		if (!OSUtility.isWindows()) {
			return false;
		}

		try {
			Logging.info("Connecting using protocol.");
			final Desktop desktop = Desktop.getDesktop();

			if (desktop.isSupported(Action.BROWSE)) {
				final String addressAndPort = address + ":" + port.toString();
				desktop.browse(new URI("samp://" + addressAndPort));
				return true;
			}
		}
		catch (final IOException | URISyntaxException exception) {
			Logging.warn("Error connecting to server using the windows protocol.", exception);
		}

		return false;
	}
}
