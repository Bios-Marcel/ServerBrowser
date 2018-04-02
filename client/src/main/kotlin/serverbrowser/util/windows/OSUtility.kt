package serverbrowser.util.windows

import serverbrowser.logging.Logging
import serverbrowser.util.basic.StringUtility
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

/**
 * @author Marcel
 * @since 19.09.2017
 */
object OSUtility {
    /**
     * Preserved os name, since it won't change anyways and reading from a variable is faster.
     */
    private val OS = System.getProperty("os.name").toLowerCase()

    /**
     * @return true if the system is windows (most likely), otherwise false
     */
    val isWindows: Boolean
        get() = OS.startsWith("windows")

    /**
     * Opens a website using the default browser. It will automatically apply http:// in front of the
     * url if not existent already.
     *
     * @param urlAsString website to visit
     */
    fun browse(urlAsString: String) {
        try {
            val fixedUrl = StringUtility.fixUrlIfNecessary(urlAsString)
            val url = URL(fixedUrl)
            browse(URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref))
        } catch (exception: IOException) {
            Logging.warn("Couldn't visit website '$urlAsString'", exception)
        } catch (exception: URISyntaxException) {
            Logging.warn("Couldn't visit website '$urlAsString'", exception)
        }

    }

    /**
     * Opens a website using the default browser.
     *
     * @param uri Website which shall be visited
     */
    fun browse(uri: URI) {
        if (Desktop.isDesktopSupported()) {
            /*
			 * HACK Workaround for Unix, since the Desktop Class seems to freeze the application
			 * unless the call is threaded.
			 */
            Thread {
                try {
                    Desktop.getDesktop().browse(uri)
                } catch (exception: IOException) {
                    Logging.warn("Couldn't visit website '$uri'", exception)
                }
            }.start()
        }
    }
}// Constructor to prevent instantiation
