package com.msc.serverbrowser.util.samp

import com.msc.serverbrowser.Client
import com.msc.serverbrowser.constants.PathConstants
import com.msc.serverbrowser.data.ServerConfig
import com.msc.serverbrowser.data.properties.AllowCloseGtaProperty
import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.info
import com.msc.serverbrowser.util.ServerUtility
import com.msc.serverbrowser.util.unix.WineUtility
import com.msc.serverbrowser.util.windows.OSUtility
import com.msc.serverbrowser.warn
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.URI
import java.net.URISyntaxException
import java.net.UnknownHostException
import java.time.Instant
import java.util.*

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
     *  * Check if GTA can be found and display an severe otherwise
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
            info("Connection to $address:$port via DLL injection successful.")
            return true
        }

        if (connectUsingExecutable(gtaPath, address, port, serverPassword)) {
            info("Connection to $address:$port by calling the sa-mp executable successful.")
            return true
        }

        if (Objects.isNull(serverPassword) || serverPassword.isEmpty()) {
            info("Connection to $address:$port by calling the sa-mp windows protocol successful.")
            return connectUsingWindowsProtocol(address, port)
        }

        warn("Couldn't connect to server using the protocol, since a password was used.")
        return false
    }

    private fun connectUsingDLLInjection(gtaPath: String, address: String, port: Int, serverPassword: String): Boolean {
        val arguments = buildLaunchingArguments(address, port, Optional.ofNullable(serverPassword))
        val builder = if (OSUtility.isWindows) {
            ProcessBuilder(arguments)
        } else {
            WineUtility.createWineRunner(arguments)
        }

        builder.directory(File(gtaPath))

        try {
            builder.start()
            return true
        } catch (exception: Exception) {
            warn("Error using sampcmd.exe", exception)
        }

        return false
    }

    private fun buildLaunchingArguments(address: String, port: Int, passwordOptional: Optional<String>): List<String> {
        val arguments = mutableListOf<String>()

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
            info("Connecting using executable.")
            val arguments = mutableListOf<String>()

            arguments.add(gtaPath + "samp.exe ")
            arguments.add(addressAndPort)
            arguments.add(password)

            val builder = if (OSUtility.isWindows) {
                ProcessBuilder(arguments)
            } else {
                WineUtility.createWineRunner(arguments)
            }

            builder.directory(File(gtaPath))
            builder.start()
            return true
        } catch (exception: IOException) {
            warn("Error connecting to server $addressAndPort by manually calling the executable", exception)
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
            info("Connecting using protocol.")
            val desktop = Desktop.getDesktop()

            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                val addressAndPort = address + ":" + port.toString()
                desktop.browse(URI("samp://$addressAndPort"))
                return true
            }
        } catch (exception: IOException) {
            warn("Error connecting to server using the windows protocol.", exception)
        } catch (exception: URISyntaxException) {
            warn("Error connecting to server using the windows protocol.", exception)
        }

        return false
    }
}
