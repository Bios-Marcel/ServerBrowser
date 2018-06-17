package com.msc.serverbrowser.util.samp

import com.msc.serverbrowser.Client
import com.msc.serverbrowser.constants.PathConstants
import com.msc.serverbrowser.data.ServerConfig
import com.msc.serverbrowser.data.properties.AllowCloseGtaProperty
import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.logging.Logging
import com.msc.serverbrowser.util.ServerUtility
import com.msc.serverbrowser.util.windows.OSUtility
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.URI
import java.net.URISyntaxException
import java.net.UnknownHostException
import java.time.Instant
import java.util.ArrayList
import java.util.Objects
import java.util.Optional

/**
 * This classes purpose is solely to launch GTA and connect to a server.
 *
 * @author marcel
 * @since Feb 28, 2018
 */
object SAMPLauncher {

    /**
     * Tries connecting to a SA-MP server.
     *
     * Also does:
     *
     *  * kill GTA process
     *  * Check if GTA can be found and display an error otherwise
     *  * use multiple methods for connecting, in case the best one doesn't work
     *
     * @param address the IP-address / domain for the server
     * @param port the port for the server
     * @param serverPassword password to be used for connect to the server
     * @return true if the connection was successful, otherwise false
     */
    fun connect(client: Client, address: String, port: Int, serverPassword: String): Boolean {
        if (ClientPropertiesController.getProperty(AllowCloseGtaProperty)) {
            GTAController.killGTA()
        }

        val gtaPath = GTAController.gtaPath

        if (gtaPath == null) {
            GTAController.displayCantLocateGTANotification(client)
            return false
        }

        val ipAddress =
                if (ServerUtility.isValidIPAddress(address)) {
                    address
                } else {
                    try {
                        InetAddress.getByName(address).hostAddress
                    } catch (exception: UnknownHostException) {
                        address
                    }
                }

        if (connectInternal(gtaPath, ipAddress, port, serverPassword)) {
            ServerConfig.setLastTimeJoinedForServer(address, port, Instant.now().toEpochMilli())
            return true
        }

        GTAController.showCantConnectToServerError()
        return false
    }

    private fun connectInternal(gtaPath: String, address: String, port: Int, serverPassword: String): Boolean {

        if (connectUsingDLLInjection(gtaPath, address, port, serverPassword)) {
            return true
        }

        if (connectUsingExecutable(gtaPath, address, port, serverPassword)) {
            return true
        }

        if (Objects.isNull(serverPassword) || serverPassword.isEmpty()) {
            return connectUsingWindowsProtocol(address, port)
        }

        Logging.warn("Couldn't connect to server using the protocol, since a password was used.")
        return false
    }

    private fun connectUsingDLLInjection(gtaPath: String, address: String, port: Int, serverPassword: String): Boolean {
        val builder = ProcessBuilder()
        val arguments = buildLaunchingArguments(address, port, Optional.ofNullable(serverPassword))
        builder.command(arguments)
        builder.directory(File(gtaPath))

        try {
            builder.start()
            return true
        } catch (exception: Exception) {
            Logging.warn("Error using sampcmd.exe", exception)
        }

        return false
    }

    private fun buildLaunchingArguments(address: String, port: Int, passwordOptional: Optional<String>): List<String> {
        val arguments = ArrayList<String>()
        if (OSUtility.isWindows.not()) {
            arguments.add("wine")
        }
        arguments.add(PathConstants.SAMP_CMD)
        arguments.add("-c")
        arguments.add("-h")
        arguments.add(address)
        arguments.add("-p")
        arguments.add(port.toString())
        arguments.add("-n")

        // At this point, it should be no problem to ask for the username
        arguments.add(GTAController.retrieveUsernameFromRegistry()!!)

        passwordOptional.ifPresent { password ->
            if (!password.isEmpty()) {
                arguments.add("-z")
                arguments.add(password)
            }
        }

        return arguments
    }

    private fun connectUsingExecutable(gtaPath: String, address: String, port: Int, password: String): Boolean {
        val addressAndPort = address + ":" + port.toString()

        try {
            Logging.info("Connecting using executable.")
            val arguments: MutableList<String> = mutableListOf()
            if (OSUtility.isWindows.not()) {
                arguments.add("wine")
            }
            arguments.add(gtaPath + "samp.exe ")
            arguments.add(addressAndPort)
            arguments.add(password)
            val builder = ProcessBuilder(arguments)
            builder.directory(File(gtaPath))
            builder.start()
            return true
        } catch (exception: IOException) {
            Logging.warn("Error connecting to server $addressAndPort by manually calling the executable", exception)
            return false
        }

    }

    /**
     * Connects to the given server (IP and Port) using an empty (no) password. This method uses the
     * `samp://` protocol to connect to make the samp launcher connect to the server.
     *
     * @param address the address of the server to connect to
     * @param port the port on which the SA-MP server runs
     * @return true if it was most likely successful
     */
    private fun connectUsingWindowsProtocol(address: String, port: Int): Boolean {
        //TODO Try implementing for linux
        if (!OSUtility.isWindows) {
            return false
        }

        try {
            Logging.info("Connecting using protocol.")
            val desktop = Desktop.getDesktop()

            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                val addressAndPort = address + ":" + port.toString()
                desktop.browse(URI("samp://$addressAndPort"))
                return true
            }
        } catch (exception: IOException) {
            Logging.warn("Error connecting to server using the windows protocol.", exception)
        } catch (exception: URISyntaxException) {
            Logging.warn("Error connecting to server using the windows protocol.", exception)
        }

        return false
    }
}
